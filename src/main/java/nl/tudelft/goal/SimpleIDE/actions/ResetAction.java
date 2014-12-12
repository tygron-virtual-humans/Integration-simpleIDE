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

import goal.core.agent.AgentId;
import goal.core.runtime.service.agent.AgentService;
import goal.core.runtime.service.environmentport.EnvironmentPort;
import goal.tools.IDEDebugger;
import goal.tools.IDEGOALInterpreter;
import goal.tools.LaunchManager;
import goal.tools.errorhandling.exceptions.GOALException;
import goal.tools.errorhandling.exceptions.GOALLaunchFailureException;
import goal.tools.errorhandling.exceptions.GOALWarning;
import goal.tools.errorhandling.exceptions.KRInitFailedException;

import java.awt.event.ActionEvent;

import nl.tudelft.goal.SimpleIDE.IDEMainPanel;
import nl.tudelft.goal.SimpleIDE.IDENode;
import nl.tudelft.goal.SimpleIDE.IconFactory;
import nl.tudelft.goal.SimpleIDE.ProcessNode;

/**
 * reset the selected node or all. Only available in Debug mode. IDE figures out
 * what has to be reset, depending on selection
 * 
 * @author W.Pasman 20jun2011
 */
@SuppressWarnings("serial")
public class ResetAction extends GOALAction {

	public ResetAction() {
		setIcon(IconFactory.RESET_PROCESS.getIcon());
		setDescription("reset selected agent");
	}

	@Override
	public void stateChangeEvent() {
		setActionEnabled(currentState.getViewMode() == IDEMainPanel.DEBUG_VIEW);
	}

	@Override
	protected void execute(IDENode selectedNode, ActionEvent ae)
			throws GOALException {
		if (!(selectedNode instanceof ProcessNode)) {
			throw new UnsupportedOperationException(
					"RESET cannot be applied to non-process node");
		}
		reset((ProcessNode) selectedNode);
	}

	/**
	 * Resets the selected node(s).
	 * <p>
	 * Only available in Debug mode. IDE figures out what has to be reset,
	 * depending on selection.
	 * </p>
	 * 
	 * @param selectedNode
	 *            The node currently selected in the process panel.
	 * @throws GOALException
	 */
	@SuppressWarnings("unchecked")
	private void reset(ProcessNode selectedNode) throws GOALException {
		switch (selectedNode.getType()) {
		case ENVIRONMENT_PROCESS:
			try {
				((EnvironmentPort) selectedNode.getUserObject()).reset();
			} catch (Exception e1) {
				throw new GOALWarning("reset of environment failed", e1);
			}
			break;
		case AGENT_PROCESS:
			AgentId id = new AgentId(selectedNode.getNodeName());
			LaunchManager.getCurrent().getRuntimeManager().resetAgent(id);
			break;
		case MAS_PROCESS:
			try {
				((AgentService<IDEDebugger, IDEGOALInterpreter>) selectedNode
						.getUserObject()).reset();
			} catch (KRInitFailedException e) {
				throw new GOALLaunchFailureException("Could not reset agents",
						e);
			} catch (InterruptedException e) {
				throw new GOALLaunchFailureException("Could not reset agents",
						e);
			}
			break;
		default:
			// do nothing;
		}
	}
}
