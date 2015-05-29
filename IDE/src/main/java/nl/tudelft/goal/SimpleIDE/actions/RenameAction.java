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

import goal.tools.errorhandling.exceptions.GOALException;

import java.awt.event.ActionEvent;
import java.util.List;

import krTools.errors.exceptions.ParserException;
import nl.tudelft.goal.SimpleIDE.IDEMainPanel;
import nl.tudelft.goal.SimpleIDE.IDENode;
import nl.tudelft.goal.SimpleIDE.NodeType;
import nl.tudelft.goal.SimpleIDE.files.FileNode;

/**
 * Rename file selected in files panel.
 *
 * @author W.Pasman 20jun2011
 */
@SuppressWarnings("serial")
public class RenameAction extends GOALAction {

	/**
	 *
	 */
	private static final long serialVersionUID = -6247409321377349356L;

	@Override
	/**
	 * {@inheritDoc}
	 */
	public void stateChangeEvent() {

		List<? extends IDENode> selection = this.currentState
				.getSelectedNodes();
		if (selection.isEmpty()) {
			setActionEnabled(false);
			return;
		}
		IDENode node = selection.get(0);
		NodeType nodeType = node.getType();

		setActionEnabled(this.currentState.getViewMode() == IDEMainPanel.EDIT_VIEW
				// TODO: change this, so that files not part of running mas
				// can be closed and removed.
				&& !this.currentState.isRuntimeEnvironmentAvailable()
				&& (nodeType == NodeType.MASFILE
				|| nodeType == NodeType.GOALFILE
				|| nodeType == NodeType.MODFILE
				|| nodeType == NodeType.PLFILE || nodeType == NodeType.TXTFILE)
				&& ((FileNode) node).getBaseFile().exists());

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void execute(IDENode selectedNode, ActionEvent ae)
			throws GOALException {
		try {
			developmentEnvironment.getMainPanel().getFilePanel()
			.rename(((FileNode) selectedNode).getBaseFile());
		} catch (ParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
