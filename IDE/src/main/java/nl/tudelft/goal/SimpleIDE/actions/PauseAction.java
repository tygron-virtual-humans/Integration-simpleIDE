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
import goal.core.runtime.service.environmentport.EnvironmentPort;
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
 * Puts the selected MAS in pause mode.
 *
 * More specifically, based on the selection the following happens:
 * <ul>
 * <li> <em>mas has been selected</em> (or no process has been selected):<br>
 * ALL agents are put into pause mode.
 * <li> <em>agent process has been selected:</em><br>
 * the agent is put into pause mode (NOTE!!! this really is "STEPPING" mode, we
 * are kind of abusing the debugger mode here and consequences are not
 * immediately clear )
 * <li> <em>environment has been selected:</em><br>
 * pauses environment.</li>
 * </ul>
 *
 * @param node
 *            process node to be put into PAUSE mode.
 * @author W.Pasman
 * @modified W.Pasman 20jun2011 into action
 */
@SuppressWarnings("serial")
public class PauseAction extends GOALAction {

	/**
	 *
	 */
	private static final long serialVersionUID = -1797650112011964406L;

	public PauseAction() {
		setIcon(IconFactory.PAUSE.getIcon());
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
		IDENode node = selection.get(0);
		NodeType nodeType = node.getType();
		RunMode mode;

		switch (nodeType) {
		case ROOT:
		case MASFILE:
		case GOALFILE:
		case MODFILE:
		case PLFILE:
		case TXTFILE:
		case NULLFILE:
		case REMOTE_AGENT_PROCESS:
			setActionEnabled(false);
			break;
		case MAS_PROCESS:
			setDescription("Pause all agents"); //$NON-NLS-1$
			mode = ((ProcessNode) node).getProcessRunMode();
			setActionEnabled(mode == RunMode.RUNNING
					|| mode == RunMode.STEPPING);
			break;
		case AGENT_PROCESS:
			setDescription("Pause agent"); //$NON-NLS-1$
			mode = ((ProcessNode) node).getProcessRunMode();
			setActionEnabled(mode == RunMode.RUNNING
					|| mode == RunMode.STEPPING);
			break;
		case ENVIRONMENT_PROCESS:
			setDescription("Pause environment"); //$NON-NLS-1$
			mode = ((ProcessNode) node).getProcessRunMode();
			boolean connected = (this.currentState
					.isRuntimeEnvironmentAvailable() && LaunchManager
					.getCurrent().getRuntimeManager().getEnvironmentPorts() != null);
			setActionEnabled(connected && mode != RunMode.KILLED);
			break;
		default:
			// do nothing.
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void execute(IDENode selectedNode, ActionEvent ae)
			throws GOALException {
		pause((ProcessNode) selectedNode);
	}

	/**
	 * DOC
	 *
	 * @param node
	 */
	@SuppressWarnings("unchecked")
	private void pause(ProcessNode node) {
		switch (node.getType()) {
		case MAS_PROCESS:
			// Pause all agents and environment that are part of the MAS.
			for (int i = 0; i < node.getChildCount(); i++) {
				ProcessNode childNode = (ProcessNode) node.getChildAt(i);
				pause(childNode);
			}
			break;
		case AGENT_PROCESS:
			switch (node.getProcessRunMode()) {
			case KILLED:
				// ignore stepping of killed agents.
			case STEPPING:
				// ignore agents still busy with stepping.
			case FINESTEPPING:
				break;
			case PAUSED:
			case RUNNING:
				Agent<IDEGOALInterpreter> agent = (Agent<IDEGOALInterpreter>) node
				.getUserObject();
				agent.getController().getDebugger().finestep();
				break;
			case REMOTEPROCESS:
				new Warning(
						Resources.get(WarningStrings.FAILED_PAUSE_REMOTEAGT));
				break;
			case UNKNOWN:
				agent = (Agent<IDEGOALInterpreter>) node.getUserObject();
				if (agent.getController().getDebugger().getRunMode() != RunMode.KILLED) {
					// Let's be brave and try to pause agent.
					// We'll see what happens.
					agent.getController().getDebugger().finestep();
				}
				break;
			}
			break;
		case ENVIRONMENT_PROCESS:
			if (((EnvironmentPort) node.getUserObject()).getEnvironmentState() == EnvironmentState.RUNNING) {
				try {
					((EnvironmentPort) node.getUserObject()).pause();
				} catch (Exception e) {
					new Warning(Resources.get(WarningStrings.FAILED_PAUSE), e);
				}
			}
			break;
		default: // FIXME redundant check, if we make node types more accurate.
			throw new GOALBug(
					this
					+ " should only be enabled while selection is a PROCESS node, but found node of " //$NON-NLS-1$
					+ node.getType() + " named " + node.getNodeName()); //$NON-NLS-1$
		}
	}

}
