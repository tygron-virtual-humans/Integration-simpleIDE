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

import goal.preferences.DBExportPreferences;

import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * A panel for the user to edit Export preferences. We set the preferences
 * through {@link goal.preferences.DBExportPreferences}.
 * 
 * @author W.Pasman
 * @modified V.Koeman 12jun13 refactoring preferences: management separated from
 *           display through new class
 */
@SuppressWarnings("serial")
public class DBExportPrefPanel extends JPanel implements ChangeListener,
		ActionListener {
	private JCheckBox exportbeliefs;
	private JCheckBox exportpercepts;
	private JCheckBox exportmailbox;
	private JCheckBox exportgoals;
	private JRadioButton separatefiles;
	private JCheckBox openaftersave;

	private static final int INSET_LEFT = 30;

	private JLabel exportBrowseDirExplanation = new JLabel(
			"Start browsing for export directory at:");
	private JLabel exportbrowsedir = new JLabel("/"); // not editable directly
	private JCheckBox rememberUsedExportDir = new JCheckBox(
			"Remember last used export directory");

	/**
	 * Create a DBExportPrefPanel.
	 */
	public DBExportPrefPanel() {
		exportbeliefs = new JCheckBox("Export Beliefbase");
		exportpercepts = new JCheckBox("include percepts");
		exportpercepts.setMargin(new Insets(0, INSET_LEFT, 0, 0));
		exportmailbox = new JCheckBox("include mails");
		exportmailbox.setMargin(new Insets(0, INSET_LEFT, 0, 0));
		exportgoals = new JCheckBox("Export Goalbase");
		separatefiles = new JRadioButton("Export to separate files");
		// make the button indent a bit
		separatefiles.setMargin(new Insets(0, INSET_LEFT, 0, 0));
		openaftersave = new JCheckBox("Open after save");

		initSettings();
		setLayout(new GridLayout(0, 1));

		rememberUsedExportDir.addActionListener(this);
		exportbeliefs.addChangeListener(this);
		exportpercepts.addChangeListener(this);
		exportmailbox.addChangeListener(this);
		exportgoals.addChangeListener(this);
		separatefiles.addChangeListener(this);
		openaftersave.addChangeListener(this);

		add(new JLabel("Database Export Options"));
		add(exportbeliefs);
		add(exportpercepts);
		add(exportmailbox);
		add(exportgoals);
		add(separatefiles);
		add(new JPopupMenu.Separator());
		add(openaftersave);
		add(new JPopupMenu.Separator());
		add(exportBrowseDirExplanation);
		add(exportbrowsedir);
		add(rememberUsedExportDir);
	}

	/**
	 * Copies settings from preferences to the check boxes.
	 */
	private void initSettings() {
		exportbeliefs.setSelected(DBExportPreferences.getExportBeliefs());
		exportpercepts.setSelected(DBExportPreferences.getExportPercepts());
		exportmailbox.setSelected(DBExportPreferences.getExportMailbox());
		exportgoals.setSelected(DBExportPreferences.getExportGoals());
		separatefiles.setSelected(DBExportPreferences.getExportSeparateFiles());
		openaftersave.setSelected(DBExportPreferences.getOpenAfterSave());
		separatefiles.setEnabled(exportbeliefs.isSelected()
				&& exportgoals.isSelected());
		exportbrowsedir.setText(DBExportPreferences.getExportBrowsePath());
		rememberUsedExportDir.setSelected(DBExportPreferences
				.getRememberLastUsedExportDir());
	}

	/**
	 * save most recent state to java preferences
	 * 
	 */
	private void update() {
		DBExportPreferences.setExportBeliefs(exportbeliefs.isSelected());
		DBExportPreferences.setExportPercepts(exportpercepts.isSelected());
		DBExportPreferences.setExportMailbox(exportmailbox.isSelected());
		DBExportPreferences.setExportGoals(exportgoals.isSelected());
		DBExportPreferences.setExportSeparateFiles(separatefiles.isSelected());
		DBExportPreferences.setOpenAfterSave(openaftersave.isSelected());
		separatefiles.setEnabled(exportbeliefs.isSelected()
				&& exportgoals.isSelected());
		// you can not change the export browse dir directly here, so no need to
		// update that here.
		DBExportPreferences.setRememberLastUsedExportDir(rememberUsedExportDir
				.isSelected());
	}

	/**
	 * called when radio box state changes.
	 * 
	 * @param event
	 *            is the event that triggered the state change.
	 */
	public void stateChanged(ChangeEvent event) {
		update();
	}

	/**
	 * called when checkbox is changed.
	 */
	public void actionPerformed(ActionEvent event) {
		update();
	}
}