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

import goal.tools.errorhandling.exceptions.GOALBug;
import goal.tools.errorhandling.exceptions.GOALCommandCancelledException;
import goal.tools.errorhandling.exceptions.GOALException;
import goal.tools.errorhandling.exceptions.GOALIncompleteGUIUsageException;

import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.JOptionPane;

import nl.tudelft.goal.SimpleIDE.EditManager;
import nl.tudelft.goal.SimpleIDE.IDEMainPanel;
import nl.tudelft.goal.SimpleIDE.IDENode;
import nl.tudelft.goal.SimpleIDE.NodeType;
import nl.tudelft.goal.SimpleIDE.files.FileNode;

/**
 * Close the active file editor in the main panel and remove the file from the
 * file system
 * 
 * @author W.Pasman 20jun2011
 */
@SuppressWarnings("serial")
public class CloseAndRemoveAction extends GOALAction {

	@Override
	public void stateChangeEvent() {
		List<? extends IDENode> selection = currentState.getSelectedNodes();
		if (selection.isEmpty()) {
			setActionEnabled(false);
			return;
		}
		NodeType nodeType = selection.get(0).getType();

		setActionEnabled(currentState.getViewMode() == IDEMainPanel.EDIT_VIEW
				// TODO: change this, so that files not part of running MAS
				// can be closed and removed.
				&& !currentState.isRuntimeEnvironmentAvailable()
				&& (nodeType == NodeType.MASFILE
						|| nodeType == NodeType.GOALFILE
						|| nodeType == NodeType.MODFILE
						|| nodeType == NodeType.PLFILE || nodeType == NodeType.TXTFILE));
	}

	/**
	 * Override so that we can show the warning that the user needs to update
	 * his files.
	 * 
	 * @throws GOALException
	 * @throws GOALIncompleteGUIUsageException
	 * @throws GOALCommandCancelledException
	 */
	@Override
	protected void executeAll(ActionEvent evt)
			throws GOALCommandCancelledException,
			GOALIncompleteGUIUsageException, GOALException {
		// first get the selection, because remove will change it
		List<? extends IDENode> selection = currentState.getSelectedNodes();
		super.executeAll(evt);
		if (selection.size() == 1) {
			showRemoveWarning(selection.get(0).getType());
		} else {
			JOptionPane.showMessageDialog(currentState.getRootComponent(),
					"Remember to edit"
							+ " the remaining files to remove the references.");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void execute(IDENode selectedNode, ActionEvent ae)
			throws GOALException {
		if (!(selectedNode instanceof FileNode)) {
			throw new GOALBug(
					this
							+ "should only be enabled while selection is a FILE node, but found"
							+ selectedNode);
		}
		FileNode fn = (FileNode) selectedNode;
		/*
		 * close editor tabs of all children. dont need to remove file nodes of
		 * children, as removeFileNode works recursively
		 */
		for (int child = 0; child < fn.getChildCount(); child++) {
			EditManager.getInstance().close(
					((FileNode) fn.getChildAt(child)).getBaseFile());
		}
		EditManager.getInstance().close(fn.getBaseFile());
		developmentEnvironment.getMainPanel().getFilePanel().removeNode(fn);
	}

	/**
	 * show warning after removing GOAL file f, that user needs to edit his MAS
	 * 
	 * @param f
	 *            is file that was renamed
	 */
	private void showRemoveWarning(NodeType type) {
		switch (type) {
		case GOALFILE:
		case MODFILE:
		case PLFILE:
			JOptionPane
					.showMessageDialog(
							currentState.getRootComponent(),
							"Remember to edit"
									+ " the agent file(s) manually to remove the references");
			break;
		default:
			// no warning for MAS files, no user action required then.
			break;
		}

	}
}
