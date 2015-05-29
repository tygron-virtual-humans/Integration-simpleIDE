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

import eis.exceptions.EnvironmentInterfaceException;
import goal.core.agent.Agent;
import goal.core.runtime.service.agent.AgentService;
import goal.core.runtime.service.environmentport.EnvironmentPort;
import goal.tools.IDEDebugger;
import goal.tools.IDEGOALInterpreter;
import goal.tools.LaunchManager;
import goal.tools.errorhandling.Resources;
import goal.tools.errorhandling.WarningStrings;
import goal.tools.errorhandling.exceptions.GOALBug;
import goal.tools.errorhandling.exceptions.GOALException;
import goal.tools.errorhandling.exceptions.GOALLaunchFailureException;
import goal.tools.errorhandling.exceptions.GOALUserError;

import java.awt.event.ActionEvent;

import languageTools.program.agent.AgentId;
import nl.tudelft.goal.SimpleIDE.IDEMainPanel;
import nl.tudelft.goal.SimpleIDE.IDENode;
import nl.tudelft.goal.SimpleIDE.IconFactory;
import nl.tudelft.goal.SimpleIDE.ProcessNode;
import nl.tudelft.goal.messaging.exceptions.MessagingException;

/**
 * reset the selected node or all. Only available in Debug mode. IDE figures out
 * what has to be reset, depending on selection
 *
 * @author W.Pasman 20jun2011
 */
@SuppressWarnings("serial")
public class ResetAction extends GOALAction {

	/**
	 *
	 */
	private static final long serialVersionUID = 2122956530073662262L;

	public ResetAction() {
		setIcon(IconFactory.RESET_PROCESS.getIcon());
		setDescription("reset selected agent");
	}

	@Override
	public void stateChangeEvent() {
		setActionEnabled(this.currentState.getViewMode() == IDEMainPanel.DEBUG_VIEW);
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
			} catch (MessagingException | EnvironmentInterfaceException e) {
				throw new GOALUserError("reset of environment failed", e);
			}
			break;
		case AGENT_PROCESS:
			Agent<IDEGOALInterpreter> agt = LaunchManager.getCurrent()
			.getRuntimeManager()
			.getAgent(new AgentId(selectedNode.getNodeName()));
			if (agt != null) {
				try {
					agt.reset();
				} catch (Exception e) {
					// this should not throw.
					throw new GOALBug(String.format(
							Resources.get(WarningStrings.FAILED_AGENT_RESTART),
							agt.getId()), e);

				}
			}
			break;
		case MAS_PROCESS:
			try {
				((AgentService<IDEDebugger, IDEGOALInterpreter>) selectedNode
						.getUserObject()).reset();
			} catch (Exception e) {
				throw new GOALLaunchFailureException("Could not reset agents",
						e);
			}
			break;
		default:
			// do nothing;
		}
	}
}
