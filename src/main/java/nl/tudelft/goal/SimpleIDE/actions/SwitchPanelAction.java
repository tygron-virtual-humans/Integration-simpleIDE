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

import nl.tudelft.goal.SimpleIDE.IDEMainPanel;
import nl.tudelft.goal.SimpleIDE.IDENode;

/**
 * toggle visible panel - debug panel <-> edit panel.
 *
 * @author W.Pasman 20jun2011
 */
@SuppressWarnings("serial")
public class SwitchPanelAction extends GOALAction {

	/**
	 *
	 */
	private static final long serialVersionUID = -1698205630136016457L;

	public SwitchPanelAction() {
		setDescription("Switch to Debug window");
		setShortcut('/');
	}

	@Override
	/**
	 * {@inheritDoc}
	 */
	public void stateChangeEvent() {
		setActionEnabled(this.currentState.isRuntimeEnvironmentAvailable()
				|| this.currentState.getViewMode() == IDEMainPanel.DEBUG_VIEW);
		switch (this.currentState.getViewMode()) {
		case 0:
			setDescription("Switch to Debug window");
			setName("Debug");
			break;
		case 1:
			setDescription("Switch to Edit window");
			setName("Edit");
			break;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void execute(IDENode selectedNode, ActionEvent ae)
			throws GOALException {
		developmentEnvironment.getMainPanel().switchView();
		// CHECK that this triggers stateChangeEvent() for all actions.
	}
}
