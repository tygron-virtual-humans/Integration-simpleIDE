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
public class DBExportPrefPanel extends JPanel implements ChangeListener,
ActionListener {
	/**
	 *
	 */
	private static final long serialVersionUID = 2392505424113267387L;
	private final JCheckBox exportbeliefs;
	private final JCheckBox exportpercepts;
	private final JCheckBox exportemotions;
	private final JCheckBox exportmailbox;
	private final JCheckBox exportgoals;
	private final JRadioButton separatefiles;
	private final JCheckBox openaftersave;

	private static final int INSET_LEFT = 30;

	private final JLabel exportBrowseDirExplanation = new JLabel(
			"Start browsing for export directory at:");
	private final JLabel exportbrowsedir = new JLabel("/"); // not editable
	// directly
	private final JCheckBox rememberUsedExportDir = new JCheckBox(
			"Remember last used export directory");

	/**
	 * Create a DBExportPrefPanel.
	 */
	public DBExportPrefPanel() {
		this.exportbeliefs = new JCheckBox("Export Beliefbase");
		this.exportpercepts = new JCheckBox("include percepts");
		this.exportpercepts.setMargin(new Insets(0, INSET_LEFT, 0, 0));
		this.exportemotions = new JCheckBox("include emotions");
		this.exportemotions.setMargin(new Insets(0, INSET_LEFT, 0, 0));
		this.exportmailbox = new JCheckBox("include mails");
		this.exportmailbox.setMargin(new Insets(0, INSET_LEFT, 0, 0));
		this.exportgoals = new JCheckBox("Export Goalbase");
		this.separatefiles = new JRadioButton("Export to separate files");
		// make the button indent a bit
		this.separatefiles.setMargin(new Insets(0, INSET_LEFT, 0, 0));
		this.openaftersave = new JCheckBox("Open after save");

		initSettings();
		setLayout(new GridLayout(0, 1));

		this.rememberUsedExportDir.addActionListener(this);
		this.exportbeliefs.addChangeListener(this);
		this.exportpercepts.addChangeListener(this);
		this.exportemotions.addChangeListener(this);
		this.exportmailbox.addChangeListener(this);
		this.exportgoals.addChangeListener(this);
		this.separatefiles.addChangeListener(this);
		this.openaftersave.addChangeListener(this);

		add(new JLabel("Database Export Options"));
		add(this.exportbeliefs);
		add(this.exportpercepts);
		add(this.exportemotions);
		add(this.exportmailbox);
		add(this.exportgoals);
		add(this.separatefiles);
		add(new JPopupMenu.Separator());
		add(this.openaftersave);
		add(new JPopupMenu.Separator());
		add(this.exportBrowseDirExplanation);
		add(this.exportbrowsedir);
		add(this.rememberUsedExportDir);
	}

	/**
	 * Copies settings from preferences to the check boxes.
	 */
	private void initSettings() {
		this.exportbeliefs.setSelected(DBExportPreferences.getExportBeliefs());
		this.exportpercepts
				.setSelected(DBExportPreferences.getExportPercepts());
		this.exportemotions
				.setSelected(DBExportPreferences.getExportEmotions());
		this.exportmailbox.setSelected(DBExportPreferences.getExportMailbox());
		this.exportgoals.setSelected(DBExportPreferences.getExportGoals());
		this.separatefiles.setSelected(DBExportPreferences
				.getExportSeparateFiles());
		this.openaftersave.setSelected(DBExportPreferences.getOpenAfterSave());
		this.separatefiles.setEnabled(this.exportbeliefs.isSelected()
				&& this.exportgoals.isSelected());
		this.exportbrowsedir.setText(DBExportPreferences.getExportBrowsePath());
		this.rememberUsedExportDir.setSelected(DBExportPreferences
				.getRememberLastUsedExportDir());
	}

	/**
	 * save most recent state to java preferences
	 *
	 */
	private void update() {
		DBExportPreferences.setExportBeliefs(this.exportbeliefs.isSelected());
		DBExportPreferences.setExportPercepts(this.exportpercepts.isSelected());
		DBExportPreferences.setExportEmotions(this.exportemotions.isSelected());
		DBExportPreferences.setExportMailbox(this.exportmailbox.isSelected());
		DBExportPreferences.setExportGoals(this.exportgoals.isSelected());
		DBExportPreferences.setExportSeparateFiles(this.separatefiles
				.isSelected());
		DBExportPreferences.setOpenAfterSave(this.openaftersave.isSelected());
		this.separatefiles.setEnabled(this.exportbeliefs.isSelected()
				&& this.exportgoals.isSelected());
		// you can not change the export browse dir directly here, so no need to
		// update that here.
		DBExportPreferences
				.setRememberLastUsedExportDir(this.rememberUsedExportDir
						.isSelected());
	}

	/**
	 * called when radio box state changes.
	 *
	 * @param event
	 *            is the event that triggered the state change.
	 */
	@Override
	public void stateChanged(ChangeEvent event) {
		update();
	}

	/**
	 * called when checkbox is changed.
	 */
	@Override
	public void actionPerformed(ActionEvent event) {
		update();
	}
}