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

import goal.core.agent.Agent;
import goal.tools.IDEGOALInterpreter;
import goal.tools.errorhandling.Resources;
import goal.tools.errorhandling.Warning;
import goal.tools.errorhandling.WarningStrings;
import goal.tools.errorhandling.exceptions.GOALException;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.util.List;

import nl.tudelft.goal.SimpleIDE.IDEMainPanel;
import nl.tudelft.goal.SimpleIDE.IDENode;
import nl.tudelft.goal.SimpleIDE.NodeType;
import nl.tudelft.goal.SimpleIDE.ProcessNode;

/**
 * Open debug log window for given agent(s).
 *
 * @author W.Pasman 20jun2011
 */
@SuppressWarnings("serial")
public class DebugLogAction extends GOALAction {

	/**
	 *
	 */
	private static final long serialVersionUID = 4458479834399622347L;

	public DebugLogAction() {
		setShortcut('D', InputEvent.SHIFT_MASK);
	}

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

		setActionEnabled(this.currentState.getViewMode() == IDEMainPanel.DEBUG_VIEW
				&& selection.get(0).getType() == NodeType.AGENT_PROCESS);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void execute(IDENode selectedNode, ActionEvent ae)
			throws GOALException {
		switch (selectedNode.getType()) { // must be process node
		case AGENT_PROCESS: // only in this case do something
			@SuppressWarnings("unchecked")
			Agent<IDEGOALInterpreter> agent = (Agent<IDEGOALInterpreter>) ((ProcessNode) selectedNode)
			.getUserObject();
			developmentEnvironment.getMainPanel().getFeedbackPanel()
			.openDebugTracer(agent);
			break;
		default: // MAS file and environment do not produce debug
			// messages
			new Warning(Resources.get(WarningStrings.FAILED_SELECTION_NOTGOAL));
		}
	}
}
