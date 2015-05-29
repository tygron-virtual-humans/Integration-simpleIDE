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

package nl.tudelft.goal.SimpleIDE;

import eis.iilang.EnvironmentState;
import goal.core.agent.Agent;
import goal.core.runtime.service.agent.AgentService;
import goal.core.runtime.service.environmentport.EnvironmentPort;
import goal.tools.IDEDebugger;
import goal.tools.IDEGOALInterpreter;
import goal.tools.debugger.Channel;
import goal.tools.debugger.DebugEvent;
import goal.tools.debugger.DebugObserver;
import goal.tools.debugger.SteppingDebugger.RunMode;
import goal.tools.errorhandling.exceptions.GOALBug;

import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import nl.tudelft.goal.messaging.messagebox.MessageBoxId;

/**
 * Stores information about nodes in a process tree. Internal data structure for
 * process nodes.
 */
@SuppressWarnings("serial")
public abstract class ProcessNode extends DefaultMutableTreeNode implements
		IDENode, DebugObserver {

	/**
	 *
	 */
	private static final long serialVersionUID = -8771952616721022263L;
	/**
	 * Represents the process state, i.e. either RUNNING, STEPPING, PAUSED, or
	 * KILLED. The state of a process is derived from its corresponding user
	 * object. By default, a process node does not have a process state (i.e.
	 * processState = null).
	 */
	private RunMode processState = RunMode.UNKNOWN;
	private boolean boldPrinting = false;
	private final DefaultTreeModel model;

	private boolean isSelectedByScheduler = false;

	private boolean isConnected = false;// set to true when we connect to our

	// process(agent,env)

	/**
	 * Creates new process node to visualize a process of a running multi-agent
	 * system.
	 *
	 * @param obj
	 *            the object that is visualized and displayed by the process
	 *            node. Object should be either an Agent, String (for a remote
	 *            agent), a MAS registry, or an environment interface.
	 */
	@SuppressWarnings("unchecked")
	public ProcessNode(Object obj, DefaultTreeModel model) {
		super(obj);
		this.model = model;

		// Subscribe to agent's debugger if process node's object is agent.
		if (obj instanceof Agent) {
			subscribeToAgentDebugger((Agent<IDEGOALInterpreter>) obj);
		}
	}

	/**
	 * In the process panel, we only display process nodes for agents that are
	 * locally run. Connect to debugger of local agent, if user is interested in
	 * run mode, and get run mode to display.
	 *
	 * @param agent
	 */
	private void subscribeToAgentDebugger(Agent<IDEGOALInterpreter> agent) {
		IDEDebugger debugger = agent.getController().getDebugger();

		// Subscribe to agent's debugger if we're not yet connected.
		if (!this.isConnected) {
			setUserObject(agent);
			this.processState = debugger.getRunMode();
			debugger.subscribe(this, Channel.RUNMODE);
			this.isConnected = true;
		}
	}

	/**
	 * Returns the label associated with the process node that is displayed in
	 * the process pane.
	 *
	 * @returns label displayed in process pane.
	 */
	@Override
	public String getObserverName() {
		return "processnode_" + toString();
	}

	@Override
	public String getNodeName() {
		return toString();
	}

	/**
	 * When a debug event happens we need to check the state of the node.
	 * {@inheritDoc}
	 */
	@Override
	public synchronized void notifyBreakpointHit(DebugEvent event) {
		if (this.processState != event.getRunMode()) {
			this.processState = event.getRunMode();
			// See #1229. When node changes, parent mode can also change.
			// See also #1791
			SwingUtilities.invokeLater(new MyAwtRerenderTrigger(this));
			panelHasChanged();
		}
	}

	/**
	 * This runnable forces re-render of a node in the panel. see also #1791
	 *
	 * @author W.Pasman 7jun2011
	 *
	 */
	class MyAwtRerenderTrigger implements Runnable {
		private final ProcessNode theNode; // node to be rerendered.

		public MyAwtRerenderTrigger(ProcessNode node) {
			this.theNode = node;
		}

		@Override
		public void run() {
			ProcessNode.this.model.nodeStructureChanged(this.theNode);
		}
	}

	/**
	 * This function is called whenever teh ProcessNode panel has been changed.
	 * You can override this to get notifications of such events.
	 */
	public void panelHasChanged() {
	}

	/**
	 * Returns run mode of process node as DebugObserver. Always RUNNING. This
	 * means that this node will never ask the Debugger to HALT the agent
	 * process. It just wants to be updated of the agent's state.
	 */
	public RunMode getRunMode() {
		return RunMode.RUNNING;
	}

	/**
	 * Changes the run mode to KILLED.
	 *
	 * FIXME / DOC why isn't the runmode coming from deeper layers in this case?
	 * This should be a callback, not a direct call from the ProcessPanel,
	 * right?
	 */
	public void setKilled() {
		this.processState = RunMode.KILLED;
		this.model.nodeChanged(this);
		panelHasChanged();
	}

	/**
	 * Returns the type of the process node. A process node can represent a mas
	 * process, a GOAL agent process, or an environment process.
	 *
	 * @return type of process.
	 */
	@Override
	public NodeType getType() {
		Object obj = getUserObject();
		if (obj instanceof Agent) {
			return NodeType.AGENT_PROCESS;
		}
		if (obj instanceof String) {
			return NodeType.REMOTE_AGENT_PROCESS;
		}
		if (obj instanceof AgentService) {
			return NodeType.MAS_PROCESS;
		}
		if (obj instanceof EnvironmentPort) {
			return NodeType.ENVIRONMENT_PROCESS;
		}
		if (obj instanceof MessageBoxId) {
			MessageBoxId bid = (MessageBoxId) obj;
			switch (bid.getType()) {
			case ENVIRONMENT:
				return NodeType.REMOTE_ENVIRONMENT_PROCESS;
			default:
				// do nothing...
			}
		}
		throw new GOALBug("[ProcessNode] Unexpected class " + obj.getClass());
	}

	/**
	 * Returns run mode of process represented by the process node. It is
	 * updated by debugger and runtime service manager (indirectly by scheduler)
	 * about the current state of the runtime system.
	 *
	 * @return either RUNNING, KILLED, PAUSED, STEPPING, UNKNOWN run mode.
	 */
	public RunMode getProcessRunMode() {
		RunMode mode = RunMode.RUNNING;
		RunMode modeChildNode;

		switch (getType()) {
		case AGENT_PROCESS:
			if (!this.isSelectedByScheduler
					&& (this.processState == RunMode.STEPPING || this.processState == RunMode.FINESTEPPING)) {
				return RunMode.PAUSED;
			}
			return this.processState;
		case REMOTE_AGENT_PROCESS:
		case REMOTE_ENVIRONMENT_PROCESS:
			return RunMode.UNKNOWN;
		case MAS_PROCESS:
			// merge state of children
			for (int i = 0; i < this.getChildCount(); i++) {
				modeChildNode = ((ProcessNode) this.getChildAt(i))
						.getProcessRunMode();
				// See #1229. RunModes higher than PAUSED are ignored
				if (modeChildNode.getPriority() <= RunMode.PAUSED.getPriority()) {
					mode = mode.merge(modeChildNode);
				}
			}
			return mode;
		case ENVIRONMENT_PROCESS:
			EnvironmentState s = ((EnvironmentPort) getUserObject())
					.getEnvironmentState();
			switch (s) {
			case INITIALIZING:
				return RunMode.UNKNOWN;
			case KILLED:
				return RunMode.KILLED;
			case PAUSED:
				return RunMode.PAUSED;
			case RUNNING:
				return RunMode.RUNNING;
			default:
				throw new GOALBug("BUG Unknown EIS state " + s);
			}

		default:
			throw new GOALBug("[ProcessNode] Cannot determine "
					+ "run mode of unknown process type " + getType() + ".");
		}
	}

	/**
	 * This should be called whenever the scheduler changes its selection.
	 *
	 * @param selected
	 *            is true if scheduler selected this agent, false if not.
	 */
	public void setSelectedByScheduler(boolean selected) {
		if (selected != this.isSelectedByScheduler) {
			this.isSelectedByScheduler = selected;
			// panelHasChanged(); // expensive, not necessary #2176
		}

	}

	/**
	 * Return icon that corresponds to run mode of process node.
	 *
	 * @return paused, running, stepping, or killed icon.
	 */
	@Override
	public ImageIcon getIcon() {
		switch (getProcessRunMode()) {
		case RUNNING:
			return IconFactory.RUNNING_PROCESS.getIcon();
		case PAUSED:
			return IconFactory.PAUSED_PROCESS.getIcon(); // DEBUG. Shows red
			// icon in
			// process overview.
		case STEPPING:
		case FINESTEPPING:
			return IconFactory.STEPPING_PROCESS.getIcon();
		case KILLED:
			return IconFactory.KILLED_PROCESS.getIcon();
		case UNKNOWN:
			if (getType() == NodeType.REMOTE_AGENT_PROCESS) {
				return IconFactory.REMOTE_PROCESS.getIcon();
			}
			if (getType() == NodeType.REMOTE_ENVIRONMENT_PROCESS) {
				return IconFactory.REMOTE_PROCESS.getIcon();
			}

			return IconFactory.QUESTIONMARK.getIcon();
		default:
			throw new GOALBug("[ProcessNode] Could not "
					+ "determine run mode of process node of type: "
					+ getType());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean getBoldPrinting() {
		return this.boldPrinting;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setBoldPrinting(boolean value) {
		this.boldPrinting = value;
	}

}
