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

import goal.tools.errorhandling.exceptions.GOALUserError;

import java.awt.event.ActionEvent;

import nl.tudelft.goal.SimpleIDE.IDEMainPanel;
import nl.tudelft.goal.SimpleIDE.IDENode;
import nl.tudelft.goal.SimpleIDE.SimpleIDE;

/**
 * Close current component. What exactly is closed depends on ....
 *
 * @author W.Pasman 16jun2011
 */
public class CloseIntrospectorAction extends GOALAction {
	/**
	 *
	 */
	private static final long serialVersionUID = 4972769881483942838L;

	@Override
	/**
	 * {@inheritDoc}
	 */
	public void stateChangeEvent() {
		setActionEnabled(this.currentState.getViewMode() == IDEMainPanel.DEBUG_VIEW);
	}

	/**
	 * this action requires the ActionEvent to be set up properly.
	 *
	 * @param selectedNode
	 *            is the node that is treated now by the action. It is not used
	 *            here and can be set to null.
	 * @param e
	 *            should contain the name of the tab to be closed, see
	 *            {@link SimpleIDE#closeIntrospector}
	 */
	@Override
	protected void execute(IDENode selectedNode, ActionEvent e)
			throws GOALUserError {
		developmentEnvironment.getMainPanel().getDebugPanel()
		.closeIntrospector(e.getActionCommand());
	}
}
