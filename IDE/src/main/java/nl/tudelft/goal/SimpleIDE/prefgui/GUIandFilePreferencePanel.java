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

import goal.preferences.PMPreferences;
import goal.tools.errorhandling.exceptions.GOALCommandCancelledException;
import goal.tools.errorhandling.exceptions.GOALUserError;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import nl.tudelft.goal.SimpleIDE.SimpleIDE;
import nl.tudelft.goal.SimpleIDE.preferences.IDEPreferences;
import nl.tudelft.goal.SimpleIDE.preferences.IntrospectorPreferences;

/**
 * Stores the IDE preferences and provides a GUI to edit them.
 *
 * <p>
 * This functionality is needed also in uninstaller. Therefore avoid general
 * dependencies on GOAL, e.g. don't use the Logger, don't use callbacks to the
 * SimpleIDE etc.
 * </p>
 *
 * @author W.Pasman 24mar09
 * @modified V.Koeman 12jun13 refactoring preferences: management separated from
 *           display through new class
 * @modified K.Hindriks Layout and naming.
 */
public class GUIandFilePreferencePanel extends JPanel implements
		ChangeListener, ActionListener {
	/**
	 *
	 */
	private static final long serialVersionUID = -6947782993162803478L;
	// File handling items.
	private final JTextField agentsbrowsedir = new JTextField("/");
	private final JLabel agentsBrowseDirExplanation = new JLabel(
			"Start browsing for agent files here: ");
	private final JButton agentBrowseButton = new JButton("Browse...");

	private final JCheckBox rememberUsedAgentDir = new JCheckBox(
			"Remember last used agent directory");
	private final JCheckBox reopenMASs; // re-open MASs that were open last
	// time?
	private final JCheckBox reopenGOALs; // re-open spurious GOAL files that
	// were open
	// last time?

	// Look and Feel
	private final JComboBox<String> lookAndFeel;

	// Window Sizing and Settings
	private final JCheckBox rememberIDEWindowSize;
	private final JCheckBox rememberIDEWindowPosition;
	// Font size console area.
	private final JSpinner fontsizespinner;
	private static final int FONT_MIN_SIZE = 5;
	private static final int FONT_MAX_SIZE = 20;
	private final JCheckBox rememberFeedbackAreaSize;
	private final JSpinner maxlinesspinner;
	private static final int MAX_LINES_IN_TEXTFIELDS = 10000;
	private static final int SPINNER_LINE_COUNT_STEP = 100;

	// Window sizing of the Introspector.
	private final JCheckBox rememberdbsize;
	private final JCheckBox couplequerysize;
	private final JSpinner spinner;

	private static final int DEFAULT_OUTPUT_LINES = 1000;
	private static final int MAX_OUTPUT_LINES = 2000;
	private static final int MIN_OUTPUT_LINES = 100;
	private static final int OUTPUT_LINES_STEPSIZE = 100;

	/**
	 * Creates the preference panel for GUI & File related options.
	 */
	public GUIandFilePreferencePanel() {

		final GUIandFilePreferencePanel thispanel = this;

		// copy of "winwidth" and "winheight" as in the current Java look and
		// feel
		JTextArea winsize;
		JTextArea feedbackAreaSize;

		// Look and feel.
		String[] lafs = { "Default", "Nimbus" };
		this.lookAndFeel = new JComboBox<String>(lafs);

		// Button to start browsing for path.
		this.agentBrowseButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				try {
					File newFile = SimpleIDE
							.askFile(
									thispanel,
									true,
									"Select path to start browsing from for .goal files",
									JFileChooser.DIRECTORIES_ONLY, null, null,
									PMPreferences.getAgentBrowsePath(), true);
					if (newFile != null) {
						GUIandFilePreferencePanel.this.agentsbrowsedir
						.setText(newFile.getAbsolutePath());
						thispanel.actionPerformed(null);
					}

				} catch (GOALCommandCancelledException e) {
					// user cancelled the action. ignore.
				} catch (GOALUserError e) {
					// not much else we can do here. We can not use GOAL
					// exceptions
					System.out.println("browse failed:" + e);
				}
			}
		});

		// Panel fragment for path to browse for agent files
		JPanel agentbrowsepanel = new JPanel(new BorderLayout());
		agentbrowsepanel.add(this.agentsbrowsedir, BorderLayout.CENTER);
		agentbrowsepanel.add(this.agentBrowseButton, BorderLayout.EAST);

		// Main IDE window size
		this.rememberIDEWindowSize = new JCheckBox("Remember IDE window size");
		this.rememberIDEWindowPosition = new JCheckBox(
				"Remember IDE window position");
		winsize = new JTextArea("      width=" + IDEPreferences.getWinWidth()
				+ " height=" + IDEPreferences.getWinHeight());
		winsize.setEditable(false);

		// Feedback area size gets 30%, 70% for the main window.
		this.rememberFeedbackAreaSize = new JCheckBox(
				"Remember size of CONSOLE area");
		feedbackAreaSize = new JTextArea("      current pos:"
				+ IDEPreferences.getConsoleAreaHeight());
		feedbackAreaSize.setEditable(false);

		// Show tool bar TODO: add this to the IDE panel, set defaults etc.
		// JCheckBox showtoolbar = new JCheckBox("Show toolbar");

		//
		JPanel maxlinespanel = new JPanel(new BorderLayout());
		SpinnerNumberModel sm = new SpinnerNumberModel(
				IDEPreferences.getConsoleAreaHeight(), SPINNER_LINE_COUNT_STEP,
				MAX_LINES_IN_TEXTFIELDS, SPINNER_LINE_COUNT_STEP);
		maxlinespanel.add(new JLabel("Maximum number of lines in CONSOLE area "
				+ "(changes take effect after restart)"), BorderLayout.CENTER);
		this.maxlinesspinner = new JSpinner(sm);
		maxlinespanel.add(this.maxlinesspinner, BorderLayout.WEST);

		// Introspector window sizing.
		this.rememberdbsize = new JCheckBox(
				"Remember the size of the database view area in INTROSPECTOR");
		// JTextArea dbsize = new JTextArea("      current size:"
		// + IntrospectorPreferences.getDBContentSize());
		this.couplequerysize = new JCheckBox(
				"Single size of query area in INTROSPECTOR");

		JPanel maxlinesIntrospectorPanel = new JPanel(new BorderLayout());
		SpinnerNumberModel smIntrospector = new SpinnerNumberModel(
				DEFAULT_OUTPUT_LINES, MIN_OUTPUT_LINES, MAX_OUTPUT_LINES,
				OUTPUT_LINES_STEPSIZE);
		maxlinesIntrospectorPanel
				.add(new JLabel(
						"Maximum number of lines in query output area in INTROSPECTOR"),
						BorderLayout.CENTER);
		this.spinner = new JSpinner(smIntrospector);
		maxlinesIntrospectorPanel.add(this.spinner, BorderLayout.WEST);

		// Font size
		JPanel fontsizepanel = new JPanel(new BorderLayout());
		SpinnerNumberModel fontsm = new SpinnerNumberModel(
				IDEPreferences.getConsoleFontSize(), FONT_MIN_SIZE,
				FONT_MAX_SIZE, 1);
		this.fontsizespinner = new JSpinner(fontsm);
		fontsizepanel
				.add(new JLabel(
						"Font size in CONSOLE area (changes take effect after restart)"),
						BorderLayout.CENTER);
		fontsizepanel.add(this.fontsizespinner, BorderLayout.WEST);

		// Re-opening of files at start-up
		this.reopenMASs = new JCheckBox("Re-open MAS Projects at start-up");
		this.reopenGOALs = new JCheckBox("Re-open Other files at start-up.");

		// Initialize
		initSettings();

		// Define layout.
		setLayout(new GridLayout(0, 1));

		// File handling section.
		add(PreferencesPanel.getBoldFontJLabel("File Handling"));
		add(this.agentsBrowseDirExplanation);
		add(agentbrowsepanel);
		add(this.rememberUsedAgentDir);
		this.rememberUsedAgentDir.addActionListener(this);

		add(this.reopenMASs);
		this.reopenMASs.addChangeListener(this);
		add(this.reopenGOALs);
		this.reopenGOALs.addChangeListener(this);

		add(new JPopupMenu.Separator());

		// Java look and feel.
		add(PreferencesPanel
				.getBoldFontJLabel("Java Look and Feel (changes take effect after restart): "));
		add(this.lookAndFeel);
		this.lookAndFeel.addActionListener(this);

		add(new JPopupMenu.Separator());

		// Window sizes section.
		add(PreferencesPanel.getBoldFontJLabel("Window Sizing and Settings"));
		add(this.rememberIDEWindowSize);
		add(this.rememberIDEWindowPosition);
		this.rememberIDEWindowSize.addChangeListener(this);
		this.rememberIDEWindowPosition.addChangeListener(this);

		add(fontsizepanel);
		this.fontsizespinner.addChangeListener(this);
		add(this.rememberFeedbackAreaSize);
		this.rememberFeedbackAreaSize.addChangeListener(this);
		// add(feedbackAreaSize);
		add(maxlinespanel);
		this.maxlinesspinner.addChangeListener(this);

		add(this.rememberdbsize);
		this.rememberdbsize.addChangeListener(this);
		// add(dbsize);
		add(this.couplequerysize);
		this.couplequerysize.addChangeListener(this);
		add(maxlinesIntrospectorPanel);
		this.spinner.addChangeListener(this);
	}

	/**
	 * Copies settings from preferences to the check boxes.
	 */
	private void initSettings() {
		this.agentsbrowsedir.setText(PMPreferences.getAgentBrowsePath());
		this.rememberUsedAgentDir.setSelected(PMPreferences
				.getRememberLastUsedAgentDir());
		this.rememberIDEWindowSize.setSelected(IDEPreferences
				.getRememberWinSize());
		this.rememberIDEWindowPosition.setSelected(IDEPreferences
				.getRememberWinPos());
		this.rememberFeedbackAreaSize.setSelected(IDEPreferences
				.getRememberConsoleSize());
		this.reopenMASs.setSelected(IDEPreferences.getReopenMASs());
		this.reopenGOALs.setSelected(IDEPreferences.getReopenSpurious());
		this.lookAndFeel.setSelectedItem(IDEPreferences.getLAF());
		this.rememberdbsize.setSelected(IntrospectorPreferences
				.getRememberDBSize());
		this.couplequerysize.setSelected(IntrospectorPreferences
				.getCoupleQuerySize());
		this.spinner.setValue(IntrospectorPreferences.getMaxLines());
	}

	/**
	 * Called after user manipulated the preference settings with the GUI.
	 * change most recent state. Called also from actionPerformed
	 *
	 * @param e
	 *            is not used.
	 */
	@Override
	public void stateChanged(ChangeEvent e) {
		PMPreferences.setAgentBrowsePath(this.agentsbrowsedir.getText());
		PMPreferences.setRememberLastUsedAgentDir(this.rememberUsedAgentDir
				.isSelected());
		IDEPreferences.setRememberWinSize(this.rememberIDEWindowSize
				.isSelected());
		IDEPreferences.setRememberWinPos(this.rememberIDEWindowPosition
				.isSelected());
		IDEPreferences.setRememberConsoleSize(this.rememberFeedbackAreaSize
				.isSelected());
		IDEPreferences.setConsoleAreaHeight((Integer) this.maxlinesspinner
				.getValue());
		IDEPreferences.setReopenMASs(this.reopenMASs.isSelected());
		IDEPreferences.setReopenSpurious(this.reopenGOALs.isSelected());
		IDEPreferences.setLAF((String) this.lookAndFeel.getSelectedItem());
		IDEPreferences.setConsoleFontSize((Integer) this.fontsizespinner
				.getValue());

		IntrospectorPreferences.setRememberDBSize(this.rememberdbsize
				.isSelected());
		IntrospectorPreferences.setCoupleQuerySize(this.couplequerysize
				.isSelected());
		IntrospectorPreferences.setMaxLines((Integer) this.spinner.getValue());
	}

	/**
	 * When user manipulates combo box, we get an actionPerformed event.
	 *
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		stateChanged(null); // handling is identical to stateChanged()
	}
}