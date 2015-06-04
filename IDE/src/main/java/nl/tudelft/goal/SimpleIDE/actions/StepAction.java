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

import eis.iilang.EnvironmentState;
import goal.core.agent.Agent;
import goal.core.runtime.RuntimeManager;
import goal.core.runtime.service.environmentport.EnvironmentPort;
import goal.tools.IDEDebugger;
import goal.tools.IDEGOALInterpreter;
import goal.tools.LaunchManager;
import goal.tools.debugger.SteppingDebugger.RunMode;
import goal.tools.errorhandling.Resources;
import goal.tools.errorhandling.Warning;
import goal.tools.errorhandling.WarningStrings;
import goal.tools.errorhandling.exceptions.GOALBug;
import goal.tools.errorhandling.exceptions.GOALException;

import java.awt.event.ActionEvent;
import java.util.List;

import nl.tudelft.goal.SimpleIDE.IDENode;
import nl.tudelft.goal.SimpleIDE.IconFactory;
import nl.tudelft.goal.SimpleIDE.NodeType;
import nl.tudelft.goal.SimpleIDE.ProcessNode;

/**
 * Puts the selected process into STEPPING mode. Basically this means "PAUSED"
 * mode but with a delayed effect.
 *
 * More specifically, based on the selection the following happens:
 * <ul>
 * <li> <em>mas has been selected</em> (or no process has been selected):<br>
 * ALL agents are put into stepping mode.
 * <li> <em>agent process has been selected:</em><br>
 * the agent is put into step mode (but has to wait until the scheduler selects
 * it for execution)
 * <li> <em>environment has been selected:</em><br>
 * GUI should not enable this. The call step(environment) will RUN the
 * environment.</li>
 * </ul>
 *
 * @param node
 *            process node to be put into STEPPING mode.
 *
 * @author W.Pasman
 * @modified W.Pasman 20jun2011 into action
 */
public class StepAction extends GOALAction {
	/**
	 *
	 */
	private static final long serialVersionUID = -7678141425812077047L;

	public StepAction() {
		setIcon(IconFactory.STEP_PROCESS.getIcon());
		setShortcut('T');
	}

	@Override
	/**
	 * {@inheritDoc}
	 */
	public synchronized void stateChangeEvent() {
		List<? extends IDENode> selection = this.currentState
				.getSelectedNodes();
		if (selection.isEmpty()) {
			setActionEnabled(false);
			return;
		}
		IDENode node = selection.get(0);
		NodeType nodeType = node.getType();
		RunMode mode;

		switch (nodeType) {
		case ROOT:
		case MASFILE:
		case EMOFILE:
		case GOALFILE:
		case MODFILE:
		case PLFILE:
		case TXTFILE:
		case NULLFILE:
		case REMOTE_AGENT_PROCESS:
			setActionEnabled(false);
			break;
		case MAS_PROCESS:
		case AGENT_PROCESS:
			setDescription("Step " + nameOfNode(node)); //$NON-NLS-1$
			mode = ((ProcessNode) node).getProcessRunMode();
			setActionEnabled(mode == RunMode.RUNNING || mode == RunMode.PAUSED);
			break;
		case ENVIRONMENT_PROCESS:
		case REMOTE_ENVIRONMENT_PROCESS:
			boolean connected = (this.currentState
					.isRuntimeEnvironmentAvailable() && LaunchManager
					.getCurrent().getRuntimeManager().getEnvironmentPorts() != null);

			mode = ((ProcessNode) node).getProcessRunMode();
			setActionEnabled(connected && mode != RunMode.KILLED);
			break;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void execute(IDENode selectedNode, ActionEvent ae)
			throws GOALException {
		step((ProcessNode) selectedNode);
	}

	/**
	 * DOC
	 *
	 * @param node
	 * @throws GOALException
	 */
	private void step(ProcessNode node) throws GOALException {
		final RuntimeManager<IDEDebugger, IDEGOALInterpreter> runtime = LaunchManager
				.getCurrent().getRuntimeManager();

		switch (node.getType()) {
		case MAS_PROCESS:
			// TRAC #1229: first check if environment needs to be put in run
			// mode.
			for (int i = 0; i < node.getChildCount(); i++) {
				ProcessNode childNode = (ProcessNode) node.getChildAt(i);
				if (childNode.getType() == NodeType.ENVIRONMENT_PROCESS
						&& childNode.getProcessRunMode() != RunMode.RUNNING) {
					developmentEnvironment.getMainPanel().getProcessPanel()
					.runProcessNode(childNode);
				}
			}

			// All agents will perform a step.
			for (Agent<IDEGOALInterpreter> agent : runtime.getAliveAgents()) {
				agent.getController().getDebugger().step();
			}
			break;
		case AGENT_PROCESS:
			switch (node.getProcessRunMode()) {
			case KILLED: // ignore stepping of killed agents
			case STEPPING: // ignore agents still busy with stepping.
			case FINESTEPPING:
				break;
			case PAUSED:
			case RUNNING:
				@SuppressWarnings("unchecked")
				Agent<IDEGOALInterpreter> agent = (Agent<IDEGOALInterpreter>) node
				.getUserObject();
				agent.getController().getDebugger().step();
				break;
			default: // FIXME should throw GOALBug but that's not possible
				// here...
				throw new RuntimeException("[SimpleIDE] Unknown status " //$NON-NLS-1$
						+ node.getProcessRunMode() + " process node."); //$NON-NLS-1$
			}
			break;
		case ENVIRONMENT_PROCESS:
			// under discussion whether STEP should start or stop the
			// environment. #1229
			if (((EnvironmentPort) node.getUserObject()).getEnvironmentState() == EnvironmentState.RUNNING) {
				try {
					((EnvironmentPort) node.getUserObject()).pause();
				} catch (Exception e) {
					new Warning(Resources.get(WarningStrings.FAILED_ENV_PAUSE),
							e);
				}
			}
			break;
		default: // Redundant check, if we make NodeType more specific
			throw new GOALBug(
					this
					+ "should only be enabled while selection is a PROCESS node, but found" //$NON-NLS-1$
					+ node);
		}
	}

	/** @return name for a node, usable for tooltip text */
	String nameOfNode(IDENode node) {
		switch (node.getType()) {
		case AGENT_PROCESS:
			return "agent"; //$NON-NLS-1$
		case ENVIRONMENT_PROCESS:
			return "environment"; //$NON-NLS-1$
		case MAS_PROCESS:
			return "multi-agent system"; //$NON-NLS-1$
		default:
			return "unknown"; //$NON-NLS-1$
		}
	}
}
