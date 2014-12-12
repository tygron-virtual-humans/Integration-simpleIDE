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
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import nl.tudelft.goal.SimpleIDE.preferences.EditorPreferences;

/**
 * This object stores the preferences for exporting a GOAL file. It also
 * provides a GUI to change these preferences.
 * <p>
 * This functionality is needed also in uninstaller Therefore avoid general
 * dependencies on GOAL, e.g. don't use the Logger, don't use callbacks to the
 * SimpleIDE etc
 * </p>
 * 
 * @modified V.Koeman 12jun13 refactoring preferences: management separated from
 *           display through new class
 */
@SuppressWarnings("serial")
public class EditorPrefPanel extends JPanel implements ChangeListener,
		ItemListener {
	private static final int MIN_FONT_SIZE = 4;
	private static final int MAX_FONT_SIZE = 30;

	private JComboBox fontName;
	private JSpinner fontSizeSpinner;
	private JCheckBox antiAlias;

	public EditorPrefPanel() {
		JPanel dblevelpanel = new JPanel(new BorderLayout());
		dblevelpanel.add(new JLabel("Font size"), BorderLayout.CENTER);
		SpinnerNumberModel spinnerModel = new SpinnerNumberModel(
				EditorPreferences.getFontSize(), MIN_FONT_SIZE, MAX_FONT_SIZE,
				1);
		fontSizeSpinner = new JSpinner(spinnerModel);
		dblevelpanel.add(fontSizeSpinner, BorderLayout.WEST);

		fontName = new JComboBox(allFontNames().toArray());

		antiAlias = new JCheckBox("font anti aliasing");

		initSettings();
		setLayout(new GridLayout(0, 1));

		add(new JLabel("Editor Font"));
		add(fontName);
		fontName.addItemListener(this);
		add(dblevelpanel);
		fontSizeSpinner.addChangeListener(this);
		add(antiAlias);
		antiAlias.addChangeListener(this);
	}

	/**
	 * Copies settings from preferences to the check boxes.
	 */
	private void initSettings() {
		fontName.setSelectedItem(EditorPreferences.getFontName());
		fontSizeSpinner.setValue(EditorPreferences.getFontSize());
		antiAlias.setSelected(EditorPreferences.isAntiAliased());
	}

	/**
	 * save most recent state to java preferences
	 * 
	 * @param event
	 *            is the event that triggered the state change. We don't use
	 *            this, you can just as well call this function with null
	 *            argument.
	 */
	public void stateChanged(ChangeEvent anyevent) {
		EditorPreferences.setFontName((String) fontName.getSelectedItem());
		EditorPreferences.setFontSize((Integer) fontSizeSpinner.getValue());
		EditorPreferences.isAntiAliased(antiAlias.isSelected());
	}

	/**
	 * get list of all font names installed on this machine, plus the name of
	 * the currently selected font (which might not be installed but if it is in
	 * use it must be listed, right?)
	 * 
	 * @return set of all font names available on this machine.
	 */
	public static Set<String> allFontNames() {
		GraphicsEnvironment e = GraphicsEnvironment
				.getLocalGraphicsEnvironment();
		Font[] fonts = e.getAllFonts();
		Set<String> allFontNames = new HashSet<String>();
		for (Font f : fonts) {
			allFontNames.add(f.getFontName());
		}
		allFontNames.add(EditorPreferences.getFontName());
		return allFontNames;
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		stateChanged(null);
	}

}