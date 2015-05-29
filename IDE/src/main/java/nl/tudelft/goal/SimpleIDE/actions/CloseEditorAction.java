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

import nl.tudelft.goal.SimpleIDE.EditManager;
import nl.tudelft.goal.SimpleIDE.IDEMainPanel;
import nl.tudelft.goal.SimpleIDE.IDENode;

/**
 * Close a given editor panel. The component to close should be an
 * EditorInterface and it should be in the Source field of the ActionEvent.
 *
 * @author W.Pasman 16jun2011
 */
public class CloseEditorAction extends GOALAction {
	/**
	 *
	 */
	private static final long serialVersionUID = -1648333384033831744L;

	@Override
	public void stateChangeEvent() {
		setActionEnabled(this.currentState.getViewMode() == IDEMainPanel.DEBUG_VIEW);
	}

	/**
	 * this action requires the ActionEvent to be set up properly.
	 *
	 * @param selectedNode
	 *            is irrelevant.
	 * @param e
	 *            should contain the name of the file to be closed.
	 * @throws GOALException
	 */
	@Override
	protected void execute(IDENode selectedNode, ActionEvent e)
			throws GOALException {
		EditManager.getInstance().close(e.getActionCommand());
	}
}
