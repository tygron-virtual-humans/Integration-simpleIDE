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

import goal.tools.errorhandling.Resources;
import goal.tools.errorhandling.Warning;
import goal.tools.errorhandling.WarningStrings;
import goal.tools.errorhandling.exceptions.GOALCommandCancelledException;
import goal.tools.errorhandling.exceptions.GOALException;
import goal.util.Extension;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.List;

import nl.tudelft.goal.SimpleIDE.EditManager;
import nl.tudelft.goal.SimpleIDE.FilePanel;
import nl.tudelft.goal.SimpleIDE.IDEMainPanel;
import nl.tudelft.goal.SimpleIDE.IDENode;
import nl.tudelft.goal.SimpleIDE.IconFactory;
import nl.tudelft.goal.SimpleIDE.files.FileNode;

/**
 * Edit the selected file.
 *
 * @author W.Pasman 20jun2011
 */
@SuppressWarnings("serial")
public class EditAction extends GOALAction {

	/**
	 *
	 */
	private static final long serialVersionUID = -8537113669251150277L;

	public EditAction() {
		setIcon(IconFactory.EDIT_TEXT.getIcon());
		setDescription("Edit selected file"); //$NON-NLS-1$
	}

	@Override
	/**
	 * {@inheritDoc}
	 */
	public void stateChangeEvent() {
		List<? extends IDENode> sel = this.currentState.getSelectedNodes();
		setActionEnabled(this.currentState.getViewMode() == IDEMainPanel.EDIT_VIEW
				&& !sel.isEmpty() && isEditable(sel.get(0)));
	}

	private boolean isEditable(IDENode ideNode) {
		switch (ideNode.getType()) {
		case GOALFILE:
		case MASFILE:
		case MODFILE:
		case PLFILE:
			return true;
		default:
			return false;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void execute(IDENode selectedNode, ActionEvent ae)
			throws GOALException {
		Extension extension = null;
		switch (selectedNode.getType()) {
		case GOALFILE:
			extension = Extension.GOAL;
			break;
		case MASFILE:
			extension = Extension.MAS;
			break;
		case MODFILE:
			extension = Extension.MODULES;
			break;
		case PLFILE:
			extension = Extension.PROLOG;
			break;
		default:
			new Warning(
					Resources
					.get(WarningStrings.FAILED_EDITACTION_NOT_AVAILABLE));
			break;
		}
		if (extension != null) {
			File file = ((FileNode) selectedNode).getBaseFile();
			try {
				if (!file.exists()) {
					FilePanel.proposeToCreate(
							developmentEnvironment.getFrame(), file, extension);
				}
				EditManager.getInstance().editFile(file);
				// ignore any messages occurring when the user cancels
				// creation
				// also don't try to edit the file when that happens as well
			} catch (GOALCommandCancelledException ignore) {
			}
		}
	}
}
