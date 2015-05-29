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

import javax.swing.SwingWorker;

import languageTools.program.agent.AgentId;
import nl.tudelft.goal.SimpleIDE.EditManager;
import nl.tudelft.goal.SimpleIDE.IDEMainPanel;
import nl.tudelft.goal.SimpleIDE.IDENode;
import nl.tudelft.goal.SimpleIDE.IconFactory;
import nl.tudelft.goal.SimpleIDE.NodeType;
import nl.tudelft.goal.SimpleIDE.ProcessNode;
import nl.tudelft.goal.SimpleIDE.TextEditorInterface;

/**
 * kill selected process node.
 *
 * @author W.Pasman
 * @modified W.Pasman 20jun2011 into action
 */
public class KillAction extends GOALAction {
	/**
	 *
	 */
	private static final long serialVersionUID = 7697754986672271636L;

	public KillAction() {
		setIcon(IconFactory.KILL_PROCESS.getIcon());
		setShortcut('K');
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

		switch (nodeType) {
		case ROOT:
		case MASFILE:
		case GOALFILE:
		case MODFILE:
		case PLFILE:
		case TXTFILE:
		case NULLFILE:
		case REMOTE_AGENT_PROCESS:
		case REMOTE_ENVIRONMENT_PROCESS:
			setActionEnabled(false);
			break;
		case MAS_PROCESS:
			setDescription("Kill multi-agent system"); //$NON-NLS-1$
			setActionEnabled(true);
			break; // always, to always provide opportunity to
		// terminate run environment
		case AGENT_PROCESS:
			setDescription("Kill agent"); //$NON-NLS-1$
			setActionEnabled(((ProcessNode) node).getProcessRunMode() != RunMode.KILLED);
			break;
		case ENVIRONMENT_PROCESS:
			setDescription("Kill environment"); //$NON-NLS-1$
			setActionEnabled(((ProcessNode) node).getProcessRunMode() != RunMode.KILLED);
			break;
		default:
			throw new RuntimeException("[SimpleIDE] Unknown node type " //$NON-NLS-1$
					+ nodeType);
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void execute(IDENode selectedNode, ActionEvent ae)
			throws GOALException {
		killProcessNode((ProcessNode) selectedNode);
	}

	/**
	 * kill selected process node.
	 *
	 * @param node
	 *            is node to be killed
	 * @throws GOALException
	 * @throws Exception
	 */
	private void killProcessNode(final ProcessNode node) throws GOALException {
		if (node.getProcessRunMode() == RunMode.KILLED) {
			return;
		}

		switch (node.getType()) {
		case AGENT_PROCESS:
			String agentName = node.toString();

			// kill agent to avoid further call backs.
			// introspectors will notice the kill and close panels if
			// necessasry.
			try {
				RuntimeManager<IDEDebugger, IDEGOALInterpreter> manager = LaunchManager
						.getCurrent().getRuntimeManager();
				manager.stopAgent(new AgentId(agentName));
			} catch (Exception e) {
				new Warning(String.format(
						Resources.get(WarningStrings.FAILED_AGENT_KILL),
						agentName), e);
			}
			break;
		case MAS_PROCESS:
			// Close debug panel. Close all debug observers, including those
			// without corresponding panel.
			// We can not just keep closing all DebugObservers until no
			// DebugObservers are left,
			// because agents in PAUSE mode will not be removed as observer
			// (that would put the agent into RUN mode! TRAC 702).
			// HACK we thus make a list of all agents running in the MAS and try
			// to close process inspectors with those names.
			// This is a hack because we *might* (in future) have multiple MASs
			// run at same time.

			// Shut down runtime environment.
			try {
				LaunchManager.getCurrent().shutDownRuntime();
			} catch (Exception e) {
				new Warning(Resources.get(WarningStrings.FAILED_MAS_KILL), e);
			}

			// close all panels: introspectors, sniffer; debug text panes are
			// killed via events TODO: also close sniffer etc in similar manner
			// List<AgentId> agentIds = ((RuntimeServiceManager) node
			// .getUserObject()).getLocalAgentNames();
			// for (AgentId id : agentIds) {
			developmentEnvironment.getMainPanel().getDebugPanel().closeAll();
			developmentEnvironment.getMainPanel().getProcessPanel().removeAll();
			// .closeIntrospector(id.getName());
			// }

			// Switch view.
			developmentEnvironment.getMainPanel().switchView();
			for (TextEditorInterface editor : EditManager.getInstance()
					.getEditors()) {
				editor.setEditable(true);
			}
			EditManager.getInstance().setEditable(true);
			break;
		case ENVIRONMENT_PROCESS:
			try {
				new SwingWorker<Void, Void>() {

					@Override
					protected Void doInBackground() throws Exception {
						((EnvironmentPort) node.getUserObject()).kill();
						return null;
					}

				}.execute();
				;
			} catch (Exception e) {
				new Warning(Resources.get(WarningStrings.FAILED_KILL_ENV), e);
			}
			break;
		default: // FIXME redundant check, if we make NodeType more specific
			throw new GOALBug(
					this
							+ "should only be enabled while selection is a PROCESS node, but found" //$NON-NLS-1$
							+ node);
		}

		// update view and switch to edit view if needed
		if ((this.currentState.getViewMode() == IDEMainPanel.DEBUG_VIEW)
				&& (!this.currentState.isRuntimeEnvironmentAvailable())) {
			developmentEnvironment.getMainPanel().switchView();
		}
	}
}
