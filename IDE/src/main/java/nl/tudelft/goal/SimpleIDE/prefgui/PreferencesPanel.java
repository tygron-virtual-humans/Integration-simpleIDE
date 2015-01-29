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

package nl.tudelft.goal.SimpleIDE.prefgui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

/**
 * GUI that combines all known preference GUIs. Shows the various preference
 * panels in a tabbed pane
 *
 * <p>
 * This functionality is needed also in uninstaller Therefore avoid general
 * dependencies on GOAL, e.g. don't use the Logger, don't use callbacks to the
 * SimpleIDE, don't use GOAL Exceptions, etc.
 * </p>
 *
 * @author W.Pasman mar2009
 * @modified KH Added debug preference pane
 */
@SuppressWarnings("serial")
public class PreferencesPanel extends JPanel implements ActionListener {

	/**
	 *
	 */
	private static final long serialVersionUID = 2277869948061028972L;
	/**
	 * the dialog that we show. We might consider singleton for this panel. But
	 * that would not allowing freeing of the associated memory when closing the
	 * dialog.
	 */
	JDialog dialog = null;

	/**
	 * create panel containing all preference panels, and show it as non-modal
	 * dialog.
	 *
	 * @param parent
	 *            is the panel requesting this pref panel. Used only to center
	 *            the preferences panel.
	 * @param editorpanel
	 *            is an optional extra preferences panel to set the editor
	 *            preferences. If null, it is ignored.
	 */
	public PreferencesPanel(JFrame parent, JPanel editorpanel) {
		setLayout(new BorderLayout());

		/*
		 * Create close button. We place it in panel to allow the button to be
		 * placed right aligned
		 */
		JPanel closeButtonPanel = new JPanel(new BorderLayout());
		JButton closeButton = new JButton("Close");
		closeButton.addActionListener(this);
		closeButtonPanel.add(closeButton, BorderLayout.EAST);

		// add preference panels
		JTabbedPane preferencesTabbedPane = new JTabbedPane();
		addTab(preferencesTabbedPane, "Runtime", new RuntimePrefPanel());
		addTab(preferencesTabbedPane, "Breakpoints", new DebugPreferencePane());
		addTab(preferencesTabbedPane, "Logging", new LoggingPrefPanel());
		addTab(preferencesTabbedPane, "GUI & Files",
				new GUIandFilePreferencePanel());
		addTab(preferencesTabbedPane, "Database Export",
				new DBExportPrefPanel());

		/*
		 * TODO: Not yet working. Pref panel works but don't know how to
		 * instruct the editor to switch font... #1117 if (editorpanel != null)
		 * { preferencesTabbedPane.addTab("Editor", editorpanel); }
		 */

		add(preferencesTabbedPane, BorderLayout.CENTER);

		add(closeButtonPanel, BorderLayout.SOUTH);
		// show a NON-MODAL dialog with this panel.
		this.dialog = new JDialog(parent, false);
		this.dialog.setTitle("GOAL Preferences");
		this.dialog.setContentPane(this);
		this.dialog.pack();

		// following should not be necessary, bug in Java? Did I miss something?
		this.dialog.setLocationRelativeTo(parent);
		this.dialog.setVisible(true);
	}

	/**
	 * Add a tab with given contents to given pane.
	 *
	 * @param pane
	 *            the pane to extend with a new tab
	 * @param tabname
	 *            name for the tab
	 * @param c
	 *            the component to add
	 */
	private void addTab(JTabbedPane pane, String tabname, Component c) {
		JPanel padding = new JPanel(new BorderLayout());
		padding.add(c, BorderLayout.NORTH);
		// the CENTER will eat the extra space
		pane.addTab(tabname, padding);
	}

	/**
	 * This is called by Swing when the close button is hit.
	 */
	@Override
	public void actionPerformed(ActionEvent arg0) {
		this.dialog.setVisible(false);
		this.dialog = null;
	}

	/**
	 * Utility method for creating {@link JLabel} with bold font.
	 *
	 * @param text
	 *            The text to display in label.
	 *
	 *            TODO: create utility class and add this method to it?
	 */
	public static JLabel getBoldFontJLabel(String text) {
		JLabel label = new JLabel(text);
		label.setFont(new Font(label.getFont().getFontName(), Font.BOLD, label
				.getFont().getSize()));
		return label;
	}

}
