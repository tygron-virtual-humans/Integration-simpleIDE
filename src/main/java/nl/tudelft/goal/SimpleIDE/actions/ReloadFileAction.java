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
import goal.tools.errorhandling.exceptions.GOALBug;
import goal.tools.errorhandling.exceptions.GOALCommandCancelledException;
import goal.tools.errorhandling.exceptions.GOALException;
import goal.tools.errorhandling.exceptions.GOALIncompleteGUIUsageException;
import goal.tools.errorhandling.exceptions.GOALUserError;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.List;

import javax.swing.JOptionPane;

import nl.tudelft.goal.SimpleIDE.EditManager;
import nl.tudelft.goal.SimpleIDE.IDEMainPanel;
import nl.tudelft.goal.SimpleIDE.IDENode;
import nl.tudelft.goal.SimpleIDE.NodeType;
import nl.tudelft.goal.SimpleIDE.files.FileNode;

/**
 * Create new file. What kind of file depends on the selection in file panel.
 * 
 * @author W.Pasman 20jun2011
 */
@SuppressWarnings("serial")
public class ReloadFileAction extends GOALAction {

	@Override
	public void stateChangeEvent() {
		if (currentState.getViewMode() == IDEMainPanel.DEBUG_VIEW) {
			// We're in debug view. Process nodes can't be saved.
			setActionEnabled(false);
		} else {
			// We're in edit view.
			List<? extends IDENode> selection = currentState.getSelectedNodes();
			if (selection.isEmpty()) {
				setActionEnabled(false);
				return;
			}
			IDENode node = selection.get(0);
			NodeType nodeType = node.getType();
			setActionEnabled(((nodeType == NodeType.MASFILE
					|| nodeType == NodeType.GOALFILE
					|| nodeType == NodeType.MODFILE
					|| nodeType == NodeType.PLFILE || nodeType == NodeType.TXTFILE))
					&& EditManager.getInstance().isOpenEditor(
							((FileNode) node).getBaseFile()));
		}
	}

	@Override
	/**
	 * We override the default because we want to show a one-time
	 * requester whether user is sure to reload.
	 */
	public void executeAll(ActionEvent e) throws GOALCommandCancelledException,
			GOALIncompleteGUIUsageException, GOALException {
		List<? extends IDENode> sel = currentState.getSelectedNodes();
		if (sel.isEmpty()) {
			throw new GOALBug(
					"Reload should not be enabled if nothing is selected"); //$NON-NLS-1$
		}
		// CANCEL = NO in this case.
		int choice = JOptionPane
				.showConfirmDialog(
						currentState.getRootComponent(),
						"Do you want to reload the selected files? All unsaved changes will be lost.", //$NON-NLS-1$
						"Reload?", JOptionPane.YES_NO_CANCEL_OPTION); //$NON-NLS-1$

		if (choice != JOptionPane.YES_OPTION) {
			return;
		}
		super.executeAll(e);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void execute(IDENode selectedNode, ActionEvent e)
			throws GOALUserError {
		execute((FileNode) selectedNode);
	}

	protected void execute(FileNode selectedNode) throws GOALUserError {
		switch (selectedNode.getType()) {
		case GOALFILE:
		case MASFILE:
		case MODFILE:
		case PLFILE:
			FileNode fileNode = selectedNode;
			if (EditManager.getInstance().isOpenEditor(fileNode.getBaseFile())) {
				try {
					EditManager.getInstance().getEditor(fileNode.getBaseFile())
							.reload();
				} catch (IOException er) {
					new Warning(String.format(
							Resources.get(WarningStrings.FAILED_FILE_RELOAD),
							fileNode.getNodeName()), er);
				}
			}
			break;
		default:
			new Warning(
					Resources
							.get(WarningStrings.FAILED_FILE_RELOAD_NO_SELECTION));
			break;
		}
	}
}
