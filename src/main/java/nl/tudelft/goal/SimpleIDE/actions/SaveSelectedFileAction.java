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
import goal.tools.errorhandling.exceptions.GOALUserError;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.List;

import nl.tudelft.goal.SimpleIDE.EditManager;
import nl.tudelft.goal.SimpleIDE.IDEMainPanel;
import nl.tudelft.goal.SimpleIDE.IDENode;
import nl.tudelft.goal.SimpleIDE.NodeType;
import nl.tudelft.goal.SimpleIDE.files.FileNode;

/**
 * Save file currently selected in files panel.
 *
 * @author W.Pasman 20jun2011
 */
@SuppressWarnings("serial")
public class SaveSelectedFileAction extends GOALAction {

	/**
	 *
	 */
	private static final long serialVersionUID = 1788490054008498147L;

	@Override
	public void stateChangeEvent() {
		if (this.currentState.getViewMode() == IDEMainPanel.EDIT_VIEW) {
			// We're in debug view. Process nodes can't be saved.
			setActionEnabled(false);
		} else {
			// We're in edit view.
			List<? extends IDENode> selection = this.currentState
					.getSelectedNodes();
			// No nodes selected
			if (selection.isEmpty()) {
				setActionEnabled(false);
				return;
			}
			// TODO: we get a list but only inspect first element?
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
	protected void execute(IDENode selectedNode, ActionEvent ae)
			throws GOALUserError {
		// save only files that are currently being edited.
		try {
			EditManager.getInstance()
			.getEditor(((FileNode) selectedNode).getBaseFile()).save();
		} catch (IOException e) {
			new Warning(String.format(
					Resources.get(WarningStrings.FAILED_FILE_SAVE),
					selectedNode.toString()), e);
		}
	}
}
