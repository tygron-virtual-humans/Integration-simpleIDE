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

import goal.preferences.LoggingPreferences;
import goal.tools.errorhandling.exceptions.GOALCommandCancelledException;
import goal.tools.errorhandling.exceptions.GOALUserError;
import goal.tools.logging.GOALLogger;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import nl.tudelft.goal.SimpleIDE.SimpleIDE;

/**
 * Provides a JPanel allowing modification of the settings. We set the
 * preferences through {@link goal.preferences.LoggingPreferences}.
 * 
 * <p>
 * See {@link GOALLogger} for more details on logging to file.
 * </p>
 * 
 * @author W.Pasman
 * @modified W.Pasman 15sep10 code cleanup trac #1084
 * @modified V.Koeman 12jun13 refactoring preferences: management separated from
 *           display through new class
 */
@SuppressWarnings("serial")
public class LoggingPrefPanel extends JPanel implements ChangeListener {
	private final JCheckBox javaDetailsCheckBox = new JCheckBox(
			"Show Java details with warning messages");
	private final JCheckBox stackTraceCheckBox = new JCheckBox(
			"Show Java stack traces with warning messages");
	private final JCheckBox showLogTime = new JCheckBox("Show log time");
	private final JCheckBox logtofile = new JCheckBox("Write logs to files");
	private final JCheckBox logconsoles = new JCheckBox(
			"Log the consoles to file");
	private final JTextField logdirectory = new JTextField("/");
	private final JLabel logDirExplanation = new JLabel("Write log files here:");
	private final JButton logDirBrowseButton = new JButton("Browse...");
	private final JPanel logdirpanel = new JPanel(new BorderLayout());
	private final JCheckBox overwritefile = new JCheckBox(
			"Overwrite old log files");
	private final JSpinner maxNrOfWarningMessagesSpinner;
	private SpinnerNumberModel spinnerModel;
	private static final int MAX_NUMBER_OF_WARNING_REPEATS = 50;
	private static final int INSET_LEFT = 30;

	/**
	 * create the panel that allows user to change the prefs.
	 */
	public LoggingPrefPanel() {

		overwritefile.setMargin(new Insets(0, INSET_LEFT, 0, 0));
		logconsoles.setMargin(new Insets(0, INSET_LEFT, 0, 0));
		logdirpanel.add(logDirExplanation, BorderLayout.WEST);
		logdirpanel.add(logdirectory, BorderLayout.CENTER);
		logdirpanel.add(logDirBrowseButton, BorderLayout.EAST);

		initSettings(); // here since it initializes spinner model

		JPanel dblevelpanel = new JPanel(new BorderLayout());
		dblevelpanel.add(new JLabel(
				"Maximum number that same warning message is shown"),
				BorderLayout.CENTER);
		maxNrOfWarningMessagesSpinner = new JSpinner(spinnerModel);
		dblevelpanel.add(maxNrOfWarningMessagesSpinner, BorderLayout.WEST);

		setLayout(new GridLayout(0, 1));

		// fix listener
		javaDetailsCheckBox.addChangeListener(this);
		stackTraceCheckBox.addChangeListener(this);
		logtofile.addChangeListener(this);
		logconsoles.addChangeListener(this);
		overwritefile.addChangeListener(this);
		showLogTime.addChangeListener(this);

		logDirBrowseButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				try {
					File newFile = SimpleIDE.askFile(logdirpanel, true,
							"Select browse start path for goal agents",
							JFileChooser.DIRECTORIES_ONLY, null, null,
							LoggingPreferences.getLogDirectory(), true);
					if (newFile != null) {
						logdirectory.setText(newFile.getAbsolutePath());
						actionPerformed(null);
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

		// add components
		add(PreferencesPanel.getBoldFontJLabel("Log Handling"));
		add(showLogTime);
		add(logtofile);
		add(logconsoles);
		add(overwritefile);
		add(logdirpanel);
		add(new JPopupMenu.Separator());
		add(PreferencesPanel.getBoldFontJLabel("Display of Warnings"));
		add(dblevelpanel);
		maxNrOfWarningMessagesSpinner.addChangeListener(this);
		add(javaDetailsCheckBox);
		add(stackTraceCheckBox);
	}

	/**
	 * Copies settings from preferences to the check boxes and spinner model.
	 */
	private void initSettings() {
		logdirectory.setText(LoggingPreferences.getLogDirectory());
		logtofile.setSelected(LoggingPreferences.getLogToFile());
		logconsoles.setSelected(LoggingPreferences.getLogConsolesToFile());
		overwritefile.setSelected(LoggingPreferences.getOverwriteFile());
		javaDetailsCheckBox
				.setSelected(LoggingPreferences.getShowJavaDetails());
		stackTraceCheckBox.setSelected(LoggingPreferences.getShowStackdump());
		spinnerModel = new SpinnerNumberModel(
				LoggingPreferences.getSuppressLevel(), 1,
				MAX_NUMBER_OF_WARNING_REPEATS, 1);
		showLogTime.setSelected(LoggingPreferences.getShowTime());
	}

	/**
	 * Handles change event, when user changed a setting in the GUI.
	 * 
	 * @param event
	 *            is the GUI event
	 */
	@Override
	public void stateChanged(ChangeEvent event) {
		LoggingPreferences.setLogToFile(logtofile.isSelected());
		LoggingPreferences.setLogConsolesToFile(logconsoles.isSelected());
		LoggingPreferences.setShowTime(showLogTime.isSelected());
		LoggingPreferences.setOverwriteFile(overwritefile.isSelected());
		LoggingPreferences.setShowJavaDetails(javaDetailsCheckBox.isSelected());
		LoggingPreferences.setShowStackdump(stackTraceCheckBox.isSelected());
		LoggingPreferences
				.setSuppressLevel((Integer) maxNrOfWarningMessagesSpinner
						.getValue());
		LoggingPreferences.setLogDirectory(logdirectory.getText());
	}
}