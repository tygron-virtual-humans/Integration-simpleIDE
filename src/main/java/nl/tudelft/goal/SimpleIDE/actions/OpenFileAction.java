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

package nl.tudelft.goal.SimpleIDE.actions;

import goal.preferences.PMPreferences;
import goal.tools.errorhandling.Resources;
import goal.tools.errorhandling.Warning;
import goal.tools.errorhandling.WarningStrings;
import goal.tools.errorhandling.exceptions.GOALCommandCancelledException;
import goal.tools.errorhandling.exceptions.GOALException;
import goal.tools.errorhandling.exceptions.GOALUserError;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.JFileChooser;

import nl.tudelft.goal.SimpleIDE.IDEMainPanel;
import nl.tudelft.goal.SimpleIDE.IDENode;
import nl.tudelft.goal.SimpleIDE.IconFactory;
import nl.tudelft.goal.SimpleIDE.SimpleIDE;

/**
 * Create new file. What kind of file depends on the selection in file panel.
 * 
 * @author W.Pasman 20jun2011
 */
@SuppressWarnings("serial")
public class OpenFileAction extends GOALAction {

	public OpenFileAction() {
		setIcon(IconFactory.OPEN_FILE.getIcon());
		setShortcut('O');
		setDescription("Open existing file"); //$NON-NLS-1$
	}

	@Override
	/**
	 * {@inheritDoc}
	 */
	public void stateChangeEvent() {
		setActionEnabled(currentState.getViewMode() == IDEMainPanel.EDIT_VIEW);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void execute(IDENode selectedNode, ActionEvent e)
			throws GOALUserError {
		File theFile;

		try {
			theFile = SimpleIDE.askFile(developmentEnvironment.getMainPanel(),
					true, "Open file", JFileChooser.FILES_ONLY, null, null, //$NON-NLS-1$
					PMPreferences.getAgentBrowsePath(), true);
		} catch (GOALCommandCancelledException ignore) {
			theFile = null;
		}
		if (theFile != null) {
			if (PMPreferences.getRememberLastUsedAgentDir()) {
				PMPreferences.setAgentBrowsePath(theFile.getParent());
			}
			try {
				developmentEnvironment.getMainPanel().getFilePanel()
						.insertFile(theFile);
			} catch (GOALException e1) {
				new Warning(String.format(
						Resources.get(WarningStrings.FAILED_FILE_OPEN2),
						theFile.getName()), e1);
			} catch (IllegalAccessException e1) {
				// TODO Change these exception types...
				e1.printStackTrace();
			} catch (InstantiationException e1) {
				// TODO Change these exception types...
				e1.printStackTrace();
			}
		}
	}
}
