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
import goal.tools.PlatformManager;
import goal.tools.errorhandling.Resources;
import goal.tools.errorhandling.Warning;
import goal.tools.errorhandling.WarningStrings;
import goal.tools.errorhandling.exceptions.GOALBug;
import goal.tools.errorhandling.exceptions.GOALCommandCancelledException;
import goal.tools.errorhandling.exceptions.GOALUserError;
import goal.util.Extension;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;

import nl.tudelft.goal.SimpleIDE.EditManager;
import nl.tudelft.goal.SimpleIDE.IDEMainPanel;
import nl.tudelft.goal.SimpleIDE.IDENode;
import nl.tudelft.goal.SimpleIDE.IconFactory;
import nl.tudelft.goal.SimpleIDE.SimpleIDE;

/**
 * Create new file. What kind of file depends on the selection in file panel.
 *
 * @author W.Pasman 20jun2011
 */
public class NewFileAction extends GOALAction {
	/**
	 *
	 */
	private static final long serialVersionUID = 6259940026095798941L;

	public NewFileAction() {
		setIcon(IconFactory.NEW_FILE.getIcon());
		setShortcut('N');
		setDescription("create new file and open it"); //$NON-NLS-1$
	}

	@Override
	/**
	 * {@inheritDoc}
	 */
	public void stateChangeEvent() {
		setActionEnabled(this.currentState.getViewMode() == IDEMainPanel.EDIT_VIEW);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void execute(IDENode selectedNode, ActionEvent e)
			throws GOALUserError {
		File theFile;

		switch (selectedNode.getType()) {
		case ROOT:
			try {
				// here we enforce the extension since we need a MAS to hang
				// agent (.goal) files in.
				theFile = SimpleIDE.askFile(
						developmentEnvironment.getMainPanel(),
						false,
						"Provide a name for a new MAS file", //$NON-NLS-1$
						JFileChooser.FILES_ONLY, Extension.MAS.toString(),
						null, PMPreferences.getAgentBrowsePath(), true);
			} catch (GOALCommandCancelledException ignore) {
				theFile = null;
			}
			break;
		case GOALFILE:
		case MASFILE:
		case MODFILE:
		case PLFILE:
		case TXTFILE:
		case NULLFILE:
			try {
				// enforce is false, user can change extension to .mas2g
				theFile = SimpleIDE.askFile(
						developmentEnvironment.getMainPanel(),
						false,
						"Provide a name for a new GOAL Agent", //$NON-NLS-1$
						JFileChooser.FILES_ONLY, Extension.GOAL.toString(),
						null, PMPreferences.getAgentBrowsePath(), false);
			} catch (GOALCommandCancelledException ignore) {
				theFile = null;
			}

			break;
		default:
			throw new GOALBug(
					this
					+ "should only be enabled while selection is a FILE node, but found" //$NON-NLS-1$
					+ selectedNode);
		} // end switch(nodeType)

		if (theFile != null) {
			try {
				if (PMPreferences.getRememberLastUsedAgentDir()) {
					PMPreferences.setAgentBrowsePath(theFile.getParent());
				}
				File newFile = PlatformManager.createfile(theFile,
						Extension.getFileExtension(theFile));
				developmentEnvironment.getMainPanel().getFilePanel()
				.insertFile(newFile);
				// select new file for editing
				EditManager.getInstance().editFile(newFile);
			} catch (IOException ex) {
				new Warning(Resources.get(WarningStrings.FAILED_FILE_OPEN1), ex);
			} catch (Exception er) {
				new Warning(
						Resources
						.get(WarningStrings.FAILED_IDE_FILENODE_INSERT),
						er);
			}
		}
	}
}
