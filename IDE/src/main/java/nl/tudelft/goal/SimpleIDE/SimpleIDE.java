/**
 * GOAL interpreter that facilitates developing and executing GOAL multi-agent
 * programs. Copyright (C) 2011 K.V. Hindriks, W. Pasman
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */

package nl.tudelft.goal.SimpleIDE;

import goal.OSXAdapter;
import goal.preferences.LoggingPreferences;
import goal.tools.LaunchManager;
import goal.tools.errorhandling.Resources;
import goal.tools.errorhandling.Warning;
import goal.tools.errorhandling.WarningStrings;
import goal.tools.errorhandling.exceptions.GOALCommandCancelledException;
import goal.tools.errorhandling.exceptions.GOALException;
import goal.tools.errorhandling.exceptions.GOALUserError;
import goal.tools.logging.InfoLog;
import goal.tools.logging.Loggers;
import goal.util.Extension;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.HeadlessException;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.WindowConstants;
import javax.swing.event.CaretListener;

import nl.tudelft.goal.SimpleIDE.actions.QuitAction;
import nl.tudelft.goal.SimpleIDE.menu.IDEMenuBar;
import nl.tudelft.goal.SimpleIDE.preferences.IDEPreferences;
import nl.tudelft.goal.SimpleIDE.prefgui.GUIandFilePreferencePanel;

import org.apache.commons.io.FilenameUtils;

/**
 * <p>
 * SimpleIDE is a simple integrated development environment. Contains either a
 * simple text editor or an advanced one (jEdit) and additional tools for agent
 * introspection and e.g. a message sniffer.
 * </p>
 *
 * @author W.Pasman June 2008
 * @modified KH July 2008
 * @modified W.Pasman numerous times 2008, 2009
 * @modified KH December 2009, January 2010
 * @modified W.Pasman 23jun2011 major overhaul, all functions here now are
 *           either private or overriding one of the two interfaces implemented
 *           here.
 */
@SuppressWarnings("serial")
public class SimpleIDE extends JFrame implements IDEfunctionality, IDEState {

	/**
	 *
	 */
	private static final long serialVersionUID = 3769843981337841197L;
	// GUI elements
	private final IDEMainPanel mainPanel;
	private StatusBar statusBar = null;

	/**
	 * main function to start the GOAL IDE
	 */
	public static void main(String[] args) {
		try {
			new SimpleIDE();
		} catch (Throwable e) {
			JOptionPane.showMessageDialog(
					null,
					Resources.get(WarningStrings.FAILED_IDE_LAUNCH)
					+ e.getMessage() + "\n" //$NON-NLS-1$
					+ e.getStackTrace()[0]);
		}
	}

	/**
	 * Creates the IDE interface and related services.
	 */
	public SimpleIDE() throws InstantiationException, IllegalAccessException {
		// Do not use InfoLog; nothing is subscribed yet.
		System.out.println("Launching IDE"); //$NON-NLS-1$

		/**
		 * Checks whether logs should be rerouted to console as well. Set to
		 * false by default. Only to be used for debugging purposes by
		 * developers; change via settings file.
		 */
		if (LoggingPreferences.getShowLogsInConsole()) {
			Loggers.addConsoleLogger();
		}

		// Initialize the action factory.
		ActionFactory.getFactory(this, new IDETempState());

		// Set look and feel.
		setLookAndFeel();

		/**
		 * Set size first, otherwise it is not clear how the fractional values
		 * e.g. for setDividerLocation work out.
		 */
		setSize(IDEPreferences.getWinWidth(), IDEPreferences.getWinHeight());
		if (IDEPreferences.getRememberWinPos()) {
			setLocation(IDEPreferences.getWinX(), IDEPreferences.getWinY());
		}
		setTitle("GOAL IDE"); //$NON-NLS-1$

		setLayout(new BorderLayout());

		// Add center panel; do this before adding tool bar which depends on it
		// for initialization of buttons.
		this.mainPanel = new IDEMainPanel(this);
		add(this.mainPanel, BorderLayout.CENTER);
		this.statusBar = new StatusBar();
		add(this.statusBar, BorderLayout.SOUTH);

		// Add menu.
		setJMenuBar(new IDEMenuBar());

		// Add tool bar.
		add(new ToolBar(), BorderLayout.PAGE_START);
		setVisible(true);

		if (System.getProperty("os.name").equals("Mac OS X")) { //$NON-NLS-1$ //$NON-NLS-2$
			OSXAdapter.setQuitHandler(new Runnable() {
				@Override
				public void run() {
					try {
						ActionFactory.getAction(QuitAction.class).Execute(null,
								null);
					} catch (IllegalAccessException | InstantiationException
							| GOALException e) {
						e.printStackTrace();
					}
				}
			});
		}

		// Disable default close operation and install quit "button" handler.
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				try {
					ActionFactory.getAction(QuitAction.class).Execute(null,
							null);
				} catch (Exception er) {
					System.out.println("BUG: QUIT FAILED"); //$NON-NLS-1$
					er.printStackTrace();
				}
			}
		});
		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentMoved(ComponentEvent e) {
				IDEPreferences.setLastWinPos(getLocation());
			}

			@Override
			public void componentResized(ComponentEvent e) {
				IDEPreferences.setLastWinSize(getSize());
			}
		});

		// Set initial content of file panel.
		// TODO move application logic to platform manager.
		if (IDEPreferences.getReopenMASs()) {
			reopenMASs();
		}
		if (IDEPreferences.getReopenSpurious()) {
			reopenSpurious();
		}

		// IDE state has been configured. Broadcast the info.
		ActionFactory.broadcastStateChange(this);
	}

	/**
	 * Install the OSX adapter. This adapter ensures that the Apple-Q shortcut
	 * is correctly captured and handled by GOAL.
	 * <p>
	 * Trac #718. Indirect class loading because code referring to osxadapter
	 * will not even compile on Windows. The point is, it will also not be
	 * called on windows and it does not need compilation.
	 */
	private void installOSXAdapter() {
		try {
			Class<?> adapterclass = Class.forName("osxadapter.Adapter"); //$NON-NLS-1$
			Constructor<?> adapterconstructor = adapterclass
					.getConstructor(IDEfunctionality.class);
			adapterconstructor.newInstance(this);
		} catch (Exception e) {
			new Warning(Resources.get(WarningStrings.FAILED_OSXADAPTER_LOAD), e);
		}
	}

	/**
	 * re-open spurious files that were open last time.
	 */
	private void reopenSpurious() {
		new InfoLog("Re-opening other files..."); //$NON-NLS-1$

		for (String path : IDEPreferences.getOtherFiles()) {
			try {
				this.mainPanel.getFilePanel()
				.insertSpuriousFile(new File(path));
			} catch (Exception e) {
				new Warning(
						String.format(Resources
								.get(WarningStrings.FAILED_FILE_RELOAD), path),
								e);
			}
		}
	}

	/**
	 * Initialize the content of the {@link FilePanel} by re-opening the saved
	 * MAS project files.
	 */
	private void reopenMASs() {
		new InfoLog("Re-opening MAS projects..."); //$NON-NLS-1$

		for (String mas : IDEPreferences.getMASs()) {
			try {
				this.mainPanel.getFilePanel().insertFile(new File(mas));
			} catch (Exception e) {
				new Warning(String.format(
						Resources.get(WarningStrings.FAILED_MAS_RELOAD), mas),
						e);
			}
		}
	}

	/**
	 * Sets look and feel. Gets preference settings from
	 * {@link GUIandFilePreferencePanel}.
	 */
	private void setLookAndFeel() {
		if (IDEPreferences.getLAF().equals("Nimbus")) { //$NON-NLS-1$
			try {
				for (LookAndFeelInfo info : UIManager
						.getInstalledLookAndFeels()) {
					if ("Nimbus".equals(info.getName())) { //$NON-NLS-1$
						UIManager.setLookAndFeel(info.getClassName());
						break;
					}
				}
			} catch (Exception e) {
				new Warning(Resources.get(WarningStrings.FAILED_LAF_NIMBUS), e);
			}
		}

		UIManager.put("TextArea.font", UIManager.getFont("TextArea.font") //$NON-NLS-1$ //$NON-NLS-2$
				.deriveFont((float) IDEPreferences.getConsoleFontSize()));
	}

	/**
	 * Opens file browser panel and asks user to select a filename/directory.
	 *
	 * @param parentpanel
	 *            the panel to center this file browser panel on.
	 * @param openFile
	 *            boolean that must be set to true to open a file dialog, and to
	 *            false to open a save dialog.
	 * @param title
	 *            the title for the file browser panel.
	 * @param mode
	 *            can be set to FILES_ONLY or DIRECTORIES_ONLY or something else
	 *            to fix selection.
	 * @param ext
	 *            the expected extension. If the extension is not starting with
	 *            a dot, we insert a dot. Is enforced (if not null) by simply
	 *            adding when user enters a filename without any extension, or
	 *            by requesting user to change it if it is not right. If
	 *            enforceExtension = true, we throw exception when user changes
	 *            extension. If false, we allow explicit differnt extension by
	 *            user.
	 * @param defaultName
	 *            File name that will be suggested (if not null) by the dialog.
	 *            Should be of the form defaultName###.extension. Note that if
	 *            openFile is set to true then an existing label is picked of
	 *            that form, and if openFile is set to false then a new name is
	 *            picked.
	 * @param startdir
	 *            is the suggested start location for the browse. If this is set
	 *            to null, {@link System#getProperty(String)} is used with
	 *            String="user.home".
	 * @param enforceExtension
	 *            is set to true if extension MUST be used. false if extension
	 *            is only suggestion and can be changed. Only applicable if
	 *            extension is not null.
	 * @return The selected file. Adds default extension if file path does not
	 *         have file extension.
	 * @throws GOALCommandCancelledException
	 *             if user cancels action.
	 * @throws GOALUserError
	 *             if user makes incorrect selection.
	 *
	 */
	public static File askFile(Component parentpanel, boolean openFile,
			String title, int mode, String exten, String defaultName,
			String startdir, boolean enforceExtension)
					throws GOALCommandCancelledException, GOALUserError {
		// insert the leading dot if necessary.
		String extension = (exten == null || exten.startsWith(".")) ? exten //$NON-NLS-1$
				: "." + exten; //$NON-NLS-1$
		/**
		 * File name to be returned.
		 */
		File selectedFile;
		/**
		 * Return state of file chooser.
		 */
		int returnState;

		if (startdir == null) {
			startdir = System.getProperty("user.home"); //$NON-NLS-1$
		}

		try {
			File dir = new File(startdir);
			String suggestion = null;
			if (defaultName != null) {
				if (openFile) {
					suggestion = findExistingFilename(dir, defaultName,
							extension);
				} else {
					suggestion = createNewNonExistingFilename(dir, defaultName,
							extension);
				}
			}
			JFileChooser chooser = new JFileChooser();
			chooser.setCurrentDirectory(dir);
			chooser.setDialogTitle(title);
			chooser.setFileSelectionMode(mode);
			if (suggestion != null) {
				chooser.setSelectedFile(new File(suggestion));
			}

			if (openFile) {
				returnState = chooser.showOpenDialog(parentpanel);
			} else {
				returnState = chooser.showSaveDialog(parentpanel);
			}

			// TODO: handle ALL return state options + exception here
			if (returnState != JFileChooser.APPROVE_OPTION) {
				throw new GOALCommandCancelledException(
						"user cancelled open dialog."); //$NON-NLS-1$
				// FIXME: why not just return null?
				// That's a clean way of saying that the user did not select
				// any file; cancelling is not a program-breaking offence.
				// (and null will be returned when the user disagrees
				// with overwriting an existing file anyway)
			}

			selectedFile = chooser.getSelectedFile();
			if (openFile && !selectedFile.exists()) {
				// file browsers you can select
				// non-existing files!
				throw new FileNotFoundException("File " //$NON-NLS-1$
						+ selectedFile.getCanonicalPath() + " does not exist."); //$NON-NLS-1$
			}
		} catch (FileNotFoundException e) {
			throw new GOALUserError(e.getMessage(), e);
		} catch (IOException e) {
			throw new GOALUserError(e.getMessage(), e);
		} catch (HeadlessException e) {
			throw new GOALUserError(e.getMessage(), e);
		}

		// Check extension.
		String ext = FilenameUtils.getExtension(selectedFile.getName());
		if (extension != null) {
			if (ext.isEmpty()) {
				selectedFile = new File(selectedFile.getAbsolutePath()
						+ extension);
			} else {
				// Check whether extension is OK.
				if (enforceExtension && !("." + ext).equals(extension)) { //$NON-NLS-1$
					throw new GOALUserError("The file must have extension " //$NON-NLS-1$
							+ extension);
				}
			}
		}
		Extension fileExtension = Extension.getFileExtension(selectedFile
				.getName());
		if (fileExtension == null) {
			throw new GOALUserError("Files with extension " + ext //$NON-NLS-1$
					+ " are not recognized by GOAL"); //$NON-NLS-1$
		}

		// Check whether file name already exists.
		if (!openFile && selectedFile != null && selectedFile.exists()) {
			int selection = JOptionPane.showConfirmDialog(parentpanel,
					"Overwrite existing file?", "Overwrite?", //$NON-NLS-1$ //$NON-NLS-2$
					JOptionPane.YES_NO_OPTION);
			if (selection != JOptionPane.YES_OPTION) {
				throw new GOALCommandCancelledException(
						"User refused overwrite of file " + selectedFile); //$NON-NLS-1$
			}
		}

		return selectedFile;
	}

	/**
	 * TRAC #700
	 *
	 * @return a filename in given directory that starts with name and has
	 *         extension, or {@code null} if no such file was found. If dir is a
	 *         file instead of path, the directory containing dir is used.
	 *
	 *         TODO: should be reimplemented using, e.g., listFiles(File
	 *         directory, IOFileFilter fileFilter, IOFileFilter dirFilter) from
	 *         Apache Commons IO.
	 */
	private static String findExistingFilename(File dir, String name, String ext) {
		String[] children;

		if (dir.isFile()) {
			dir = dir.getParentFile();
		}
		children = dir.list();
		if (children == null) {
			return null;
		}
		for (String child : children) {
			if (child.startsWith(name) && child.endsWith(ext)) {
				return child;
			}
		}
		return null;
	}

	/**
	 * TRAC #700
	 *
	 * @return a filename that starts with name and has extension, and does not
	 *         exist in given directory. If dir is a file instead of path, the
	 *         directory containing dir is used.
	 */
	private static String createNewNonExistingFilename(File dir, String name,
			String ext) {
		String[] chs;
		String newname;

		if (dir.isFile()) {
			dir = dir.getParentFile();
		}
		chs = dir.list();
		if (chs == null) {
			return name + ext;
		}
		ArrayList<String> children = new ArrayList<String>(Arrays.asList(chs));
		// Check if label + ext does not yet exist
		if (!children.contains(name + ext)) {
			return (name + ext);
		} else { // Search for a number that, when added to the file name,
			// yields
			// a non-existing file name
			int n = 1;
			do {
				newname = name + n + ext;
				n++; // assumes less than 2,147,483,647 file names of form
				// "name + n + ext"
			} while (children.contains(newname));
			return newname;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IDEMainPanel getMainPanel() {
		return this.mainPanel;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<? extends IDENode> getSelectedNodes() {
		return this.mainPanel.getSelectedNodes();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public CaretListener getStatusBar() {
		return this.statusBar;
	}

	@Override
	public String toString() {
		return "SimpleIDE[" + getName() + "]"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public JFrame getFrame() {
		return this;
	}

	/************************************************/
	/************ implements IDEState ***************/
	/************************************************/
	@Override
	public Component getRootComponent() {
		return this;
	}

	@Override
	public int getViewMode() {
		return this.mainPanel.getView();
	}

	@Override
	public boolean isRuntimeEnvironmentAvailable() {
		return LaunchManager.getCurrent().isRuntimeEnvironmentAvailable();
	}

}

/**
 * Stub for IDEState, because at initialization the SimpleIDE is not yet ready
 * to provide an IDEState.
 *
 * @author W.Pasman 22jun2011
 */

class IDETempState implements IDEState {

	@Override
	public Component getRootComponent() {
		return null;
	}

	@Override
	public List<? extends IDENode> getSelectedNodes() {
		return new ArrayList<IDENode>();
	}

	@Override
	public int getViewMode() {
		return 0;
	}

	@Override
	public boolean isRuntimeEnvironmentAvailable() {
		return false;
	}

}