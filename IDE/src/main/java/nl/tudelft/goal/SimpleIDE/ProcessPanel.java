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

import goal.core.agent.Agent;
import goal.core.runtime.RuntimeEvent;
import goal.core.runtime.RuntimeEventObserver;
import goal.core.runtime.RuntimeManager;
import goal.core.runtime.service.environmentport.EnvironmentPort;
import goal.tools.IDEGOALInterpreter;
import goal.tools.LaunchManager;
import goal.tools.debugger.SteppingDebugger.RunMode;
import goal.tools.errorhandling.Resources;
import goal.tools.errorhandling.Warning;
import goal.tools.errorhandling.WarningStrings;
import goal.tools.errorhandling.exceptions.GOALBug;
import goal.tools.errorhandling.exceptions.GOALException;

import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import languageTools.program.agent.AgentId;
import nl.tudelft.goal.SimpleIDE.actions.IntrospectorAction;
import nl.tudelft.goal.messaging.messagebox.MessageBoxId;

/**
 * Shows the processes of a running multi-agent system in a process tree. Each
 * node in the tree represents a process (MAS, GOAL, or ENVIRONMENT). The root
 * node is the MAS process.
 *
 * @author K.Hindriks
 */
public class ProcessPanel extends JPanel implements RuntimeEventObserver {
	/**
	 *
	 */
	private static final long serialVersionUID = -6408794831099444206L;
	private final IDEfunctionality myIDE;
	private final IDEState myIDEState;

	private DefaultTreeModel processTreeModel;
	private JTree processTree;
	/*
	 * To avoid double clicking effects, we set the double click count to an
	 * impossible number
	 */
	private static int IMPOSSIBLE_CLICK_COUNT = 9;
	/**
	 * Node that represents the multi-agent system, root of the tree.
	 */
	private ProcessNode masNode = null;

	/**
	 * Creates process panel that shows process tree representing a running MAS.
	 *
	 * @param theIDE
	 *            IDE object that provides user logic.
	 */
	public ProcessPanel(IDEfunctionality theIDE, IDEState state) {
		this.myIDE = theIDE;
		this.myIDEState = state;
	}

	/**
	 * Initializes the panel. To be called after the runtime is created, so that
	 * the panel can properly subscribe with it.
	 *
	 * @param runtime
	 *            The latest runtime to be shown by the panel.
	 */
	public void init() {
		// Just to be sure: empty the process panel. There might be
		// remains of previous init() calls.
		removeAll();

		// Create a new tree model.
		this.processTreeModel = new DefaultTreeModel(null);
		this.processTree = new JTree(this.processTreeModel);

		// add listeners
		addListeners();

		// see TRAC 303
		this.processTree.setToggleClickCount(IMPOSSIBLE_CLICK_COUNT);

		// define layout
		setLayout(new BorderLayout());
		// process nodes cannot be edited
		this.processTree.setEditable(false);
		this.processTree.setRootVisible(true);
		this.processTree.setShowsRootHandles(false);
		this.processTree.setCellRenderer(new IDENodeRenderer());

		// add pop up menu to tree
		TreePopupMenu menu;
		try {
			menu = new TreePopupMenu();
			this.processTree.setComponentPopupMenu(menu);
		} catch (Exception e) {
			new Warning(
					Resources.get(WarningStrings.FAILED_POPUP_WINDOW_CREATE), e);
		}

		// include tree view in pane
		JScrollPane processTreeView = new JScrollPane(this.processTree);
		add(new JLabel("Process Overview"), BorderLayout.NORTH); //$NON-NLS-1$
		add(processTreeView, BorderLayout.CENTER);
	}

	/**
	 * Handles events from RuntimeManager.
	 *
	 * @param observable
	 *            The {@link MonitoringService}.
	 * @param argument
	 *            A {@link RuntimeEvent} event.
	 *
	 *            FIXME: for which events is this still used? Appears that, for
	 *            example, AGENT_DIED is never handled here...
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void eventOccured(final RuntimeManager<?, ?> observable,
			final RuntimeEvent event) {
		// Invoke later because this is an event listener that may be called
		// from another thread then the swing thread.
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				Object source = event.getSource();
				switch (event.getType()) {
				case MAS_BORN:
					handleMasBorn(source);
					break;
				case MAS_DIED: // clean up tree and tree model
					removeAll();
					ProcessPanel.this.processTreeModel = new DefaultTreeModel(
							null);
					ProcessPanel.this.processTree
					.setModel(ProcessPanel.this.processTreeModel);
					break;
				case AGENT_BORN:
				case AGENT_IS_LOCAL_AND_READY:
					handleAgentBorn(source);
					break;
				case AGENT_DIED:
					handleAgentDied((String) source);
					break;
				case AGENT_REMOVED:
					handleAgentRemoved((String) source);
					break;
				case SCHEDULER_SELECTED_AGENT:
					// highlight selected agents in process panel
					newSelectedAgent((ArrayList<Agent<IDEGOALInterpreter>>) source);
					break;
				case ENVIRONMENT_LAUNCHED:
					handleEnvBorn(source);
					break;
				case ENVIRONMENT_RUNMODE_CHANGED:
					handleEnvStateChanged(source);
					break;
				case ENVIRONMENT_KILLED:
					handleEnvKilled(source);
					break;
				default:
					break;
				}
			}
		});
	}

	/**
	 * Remove process node for environment.
	 *
	 * @param source
	 *            either EIS or messagebox source
	 */
	private synchronized void handleEnvKilled(Object source) {
		ProcessNode node = findNode(source.toString());
		if (node == null) {
			// if env is local, we have no messagebox style node.
			return;
		}
		// can we restart? should we restart? remote vs local?
		this.processTreeModel.removeNodeFromParent(node);
	}

	/**
	 * Updates the sources for the environment process node.
	 *
	 * @param source
	 */

	private void handleEnvStateChanged(Object source) {
		ProcessNode treenode = findNode(source.toString());
		if (treenode == null) {
			/**
			 * Initially, all envs look like remote envs. In that case, we
			 * received the messageboxid of the remote env and we have to fix a
			 * few things.
			 */
			if (source instanceof EnvironmentPort) {
				EnvironmentPort port = (EnvironmentPort) source;
				treenode = findNode(((EnvironmentPort) source)
						.getMessageBoxId().toString());
				if (treenode == null) {
					throw new GOALBug("unknown environment " + source //$NON-NLS-1$
							+ " encountered"); //$NON-NLS-1$
				}
				// we thought it was another a remote env but it's
				// THE env.
				treenode.setUserObject(port);
			}
		}
		this.processTreeModel.nodeChanged(treenode);
		ActionFactory.broadcastStateChange(this.myIDEState);

	}

	/**
	 * Agent died. change mode to KILLED.
	 *
	 * @param agentname
	 */
	private void handleAgentDied(String agentname) {
		ProcessNode node = findNode(agentname);

		// check if node is null which may be the case if MAS has been
		// killed
		if (node != null) {
			if (node.getType() == NodeType.REMOTE_AGENT_PROCESS) {
				// remote agent. Just remove the node.
				this.processTreeModel.removeNodeFromParent(node);
			} else {
				// local agent
				node.setKilled();
				this.processTreeModel.nodeChanged(node);
			}
		}
	}

	/**
	 * Agent has been removed from system. Remove it from the panel.
	 *
	 * @param source
	 */
	private synchronized void handleAgentRemoved(String agentname) {
		final ProcessNode node = findNode(agentname);
		if (node != null) {
			this.processTreeModel.removeNodeFromParent(node);
		}
	}

	/**
	 * handle a new environment, by creating a new node representing it
	 *
	 * @param source
	 *            is the event source. This is either a {@link MessageBoxId} if
	 *            the environment is remote, or a {@link EnvironmentPort} if
	 *            this is an environment that is running inside our
	 *            {@link RuntimeManager}
	 */
	private synchronized void handleEnvBorn(Object source) {
		ProcessNode node = new ProcessNode(source, this.processTreeModel) {
			/**
			 *
			 */
			private static final long serialVersionUID = 1405245480774729309L;

			@Override
			public void panelHasChanged() {
				ActionFactory
				.broadcastStateChange(ProcessPanel.this.myIDEState);
			}
		};
		this.processTreeModel.insertNodeInto(node, this.masNode,
				this.masNode.getChildCount());
		this.processTree.scrollPathToVisible(new TreePath(node.getPath()));
	}

	/**
	 * handle a new MAS, by creating a new node representing it
	 *
	 * @param source
	 *            is the RuntimeServiceManager
	 */
	private synchronized void handleMasBorn(Object source) {
		this.masNode = new ProcessNode(source, this.processTreeModel) {
			/**
			 *
			 */
			private static final long serialVersionUID = -3976206418465524654L;

			@Override
			public void panelHasChanged() {
				ActionFactory
				.broadcastStateChange(ProcessPanel.this.myIDEState);
			}
		};
		this.processTreeModel.setRoot(this.masNode);
	}

	/**
	 * New agent was born. Insert the new agent in the panel and update buttons.
	 * Synchronized because IDE is not threadsafe. #2402
	 *
	 * @param source
	 *            is name (string) of the agent.
	 */
	@SuppressWarnings("unchecked")
	private synchronized void handleAgentBorn(Object source) {
		String agentname;
		if (source instanceof String) {
			agentname = (String) source;
		} else {
			agentname = ((Agent<IDEGOALInterpreter>) source).getId().getName();
		}

		// only take action if we don't have the node already.
		if (findNode(agentname) == null) {
			ProcessNode node = new ProcessNode(source, this.processTreeModel) {
				/**
				 *
				 */
				private static final long serialVersionUID = 6888454348133887002L;

				@Override
				public void panelHasChanged() {
					ActionFactory
					.broadcastStateChange(ProcessPanel.this.myIDEState);
				}
			};

			// Determine where to insert the node; lexicographic ordering.
			int j = 0;
			if (this.masNode.getChildCount() > 0) {
				for (int i = 0; i < this.masNode.getChildCount(); i++) {
					j = i;
					if (node.getNodeName().compareTo(
							((ProcessNode) this.masNode.getChildAt(i))
							.getNodeName()) <= 0) {
						break;
					}
				}
				if (node.getNodeName().compareTo(
						((ProcessNode) this.masNode.getChildAt(j))
						.getNodeName()) > 0) {
					j++;
				}
			}

			// Insert node.
			this.processTreeModel.insertNodeInto(node, this.masNode, j);
			this.processTree.scrollPathToVisible(new TreePath(node.getPath()));

			// DOC
			ActionFactory.broadcastStateChange(this.myIDEState);
		}
	}

	/**
	 * A new agent has been selected by scheduler. This means we have to update
	 * the panel, because non-selected agents in STEPPING mode are shown as
	 * being in PAUSE.
	 *
	 * @param selectedAgents
	 *            are the agents that were selected by scheduler
	 */
	synchronized void newSelectedAgent(
			List<Agent<IDEGOALInterpreter>> selectedAgents) {
		boolean setBold;
		ProcessNode node;
		String name;
		List<String> selectedNames = new ArrayList<String>();
		for (Agent<IDEGOALInterpreter> agt : selectedAgents) {
			selectedNames.add(agt.getId().getName());
		}

		// this may be called _before_ #update has finished handling MAS_BORN
		// so the masNode may still be null. just ignore this call in that case.
		// (used to be synchronized with #update, but then #update sometimes got
		// into a deadlock with itself.
		if (this.masNode == null) {
			return;
		}

		// check selection-state
		for (int i = 0; i < this.masNode.getChildCount(); i++) {
			node = (ProcessNode) this.masNode.getChildAt(i);
			boolean selected = selectedNames.contains(node.getNodeName());
			node.setSelectedByScheduler(selected);
		}

		for (int i = 0; i < this.masNode.getChildCount(); i++) {
			setBold = false;
			node = (ProcessNode) this.masNode.getChildAt(i);
			name = node.getNodeName();
			for (Agent<IDEGOALInterpreter> agent : selectedAgents) {
				if (agent.getId().equals(name)) {
					setBold = true;
					break;
				}
			}
			if (node.getBoldPrinting() != setBold) {
				node.setBoldPrinting(setBold);
				// only render node if not in running mode to avoid
				// 'flickering'
				if (node.getRunMode() != RunMode.RUNNING) {
					this.processTreeModel.nodeChanged(node);
				}
			}
		}
	}

	/**
	 * Returns the selected {@link ProcessNode}s the process panel. If no node
	 * has been selected, the MAS root node is returned.
	 *
	 * @return the selected process node, or the MAS root node if nothing has
	 *         been selected.
	 * @see goal.tools.SimpleIDE.IDEfunctionality#getSelectedNode()
	 */
	public synchronized List<IDENode> getSelectedNodes() {
		TreePath[] paths = this.processTree.getSelectionPaths();
		List<IDENode> nodes = new ArrayList<IDENode>();
		if (paths == null) {
			if (this.masNode != null) {
				nodes.add(this.masNode);
			}
		} else {
			for (TreePath path : paths) {
				nodes.add((IDENode) path.getLastPathComponent());
			}
		}
		return nodes;
	}

	/**
	 * Returns the node with the given name, i.e. the name of the associated
	 * user object. Performs a walk-through of the tree.
	 *
	 * @param name
	 *            name to be searched for.
	 * @return node in process panel with given name, or null if node not found.
	 */
	public synchronized ProcessNode findNode(String name) {
		if (this.processTreeModel.getRoot() == null) { // empty tree
			return null;
		}
		// start at root node
		return findNode(name, (ProcessNode) this.processTreeModel.getRoot());
	}

	/**
	 * Performs a walk-through of the tree below given node, searching for a
	 * node with given name.
	 *
	 * @param name
	 *            name to be searched for.
	 * @param node
	 *            node to start search.
	 * @return node with given name, or null if no such node was found.
	 */
	public synchronized ProcessNode findNode(String name, ProcessNode node) {
		ProcessNode result;

		// the node has the name searched for
		if (node.getNodeName().equals(name)) {
			return node;
		}
		// the node is a leaf node with a different name
		if (node.isLeaf()) {
			return null;
		}
		// search child nodes for name
		for (int i = 0; i < node.getChildCount(); i++) {
			result = findNode(name, (ProcessNode) node.getChildAt(i));
			if (result != null) {
				return result;
			}
		}
		// name not found in subtree with node as root
		return null;
	}

	/**
	 * Adds all relevant listeners.
	 *
	 * @param tree
	 *            tree to which tree listeners need to be added.
	 */
	public void addListeners() {
		// add mouse listener
		this.processTree.addMouseListener(new MyMouseAdapter());

		// add tree selection listener
		this.processTree.addTreeSelectionListener(new TreeSelectionListener() {
			@Override
			public void valueChanged(TreeSelectionEvent e) {
				ActionFactory
				.broadcastStateChange(ProcessPanel.this.myIDEState);
			}
		});

		// tree expansion listener
		this.processTree.addTreeExpansionListener(new TreeExpansionListener() {
			@Override
			public void treeExpanded(TreeExpansionEvent event) {
				ActionFactory
				.broadcastStateChange(ProcessPanel.this.myIDEState);
			}

			@Override
			public void treeCollapsed(TreeExpansionEvent event) {
				ActionFactory
				.broadcastStateChange(ProcessPanel.this.myIDEState);
			}
		});
	}

	/**
	 * Puts the selected process (or mas) into RUNNING mode. More specifically,
	 * based on the selection the following happens:
	 * <ul>
	 * <li> <em>mas has been selected</em> (or no process has been selected):<br>
	 * all agents are put into RUNNING mode (and so the mas is in RUNNING mode).
	 * </li>
	 * <li> <em>agent process has been selected:</em><br>
	 * the agent is put into RUNNING mode (but has to wait until the scheduler
	 * selects it for execution). If the agent has been killed, the agent is
	 * re-launched and starts in PAUSED mode.</li>
	 * <li> <em>environment has been selected:</em><br>
	 * environment is put into RUNNING mode TODO</li>
	 * </ul>
	 *
	 * FIXME it seems we can move this to the PlatformManager or Runtime? In
	 * fact we use it in 2 actions only... maybe move it to them?
	 *
	 * @param node
	 *            process node (or the MAS child if root node) to be put into
	 *            RUNNING mode.
	 *
	 */
	@SuppressWarnings("unchecked")
	public void runProcessNode(ProcessNode node) throws GOALException {
		Agent<IDEGOALInterpreter> agent;

		switch (node.getType()) {
		case MAS_PROCESS:
			for (int i = 0; i < node.getChildCount(); i++) {
				// Start ALL process nodes that have NOT been KILLED.
				// TODO: make starting environment process optional
				ProcessNode childNode = (ProcessNode) node.getChildAt(i);
				switch (childNode.getType()) {
				case AGENT_PROCESS:
					if (childNode.getProcessRunMode() != RunMode.KILLED) {
						runProcessNode(childNode);
					}
					break;
				case REMOTE_AGENT_PROCESS: // can't control these (yet?)
				case REMOTE_ENVIRONMENT_PROCESS: // can't control these (yet?)
					break;
				case ENVIRONMENT_PROCESS:
					if (childNode.getProcessRunMode() == RunMode.PAUSED) {
						runProcessNode(childNode);
					}
					break;
				default:
					throw new GOALBug(
							"[SimpleIDE] Unexpected child of MAS process: " //$NON-NLS-1$
							+ childNode);
				}
			}
			break;
		case AGENT_PROCESS:
			switch (node.getProcessRunMode()) {
			case RUNNING:
				break;
			case STEPPING:
			case FINESTEPPING:
			case PAUSED:
				agent = (Agent<IDEGOALInterpreter>) node.getUserObject();
				agent.getController().getDebugger().run();
				break;
			case KILLED:
				/*
				 * must be agent process node, as MAS node cannot have status
				 * killed (in that case it would have been removed and the debug
				 * view would have been closed) TODO how do we know if agent
				 * needs environment connection?
				 */
				Agent<IDEGOALInterpreter> agt = LaunchManager.getCurrent()
				.getRuntimeManager()
				.getAgent(new AgentId(node.getNodeName()));
				try {
					if (agt != null) {
						agt.reset();
					}
				} catch (Exception e) {
					// this should not throw.
					throw new GOALBug(String.format(
							Resources.get(WarningStrings.FAILED_AGENT_RESTART),
							agt.getId()), e);
				}
				break;
			case REMOTEPROCESS:
				// TODO: nothing we can do?
				break;
			case UNKNOWN:
				agent = (Agent<IDEGOALInterpreter>) node.getUserObject();
				if (agent.getController().getDebugger().getRunMode() != RunMode.KILLED) {
					// Let's be brave and try to start agent.
					// We'll see what happens.
					agent.getController().getDebugger().run();
				}
				break;
			}
			break;
		case ENVIRONMENT_PROCESS:
			try {
				((EnvironmentPort) node.getUserObject()).start();
			} catch (Exception e) {
				new Warning(Resources.get(WarningStrings.FAILED_ENV_START), e);
			}
			break;
		default:
			// FIXME avoid this by making ProcessNode type
			throw new GOALBug("[SimpleIDE] Unknown type " + node.getType() //$NON-NLS-1$
					+ " of process node."); //$NON-NLS-1$
		}
	}

	@Override
	public String toString() {
		return "ProcessPanel[" + this.myIDE + "]"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Handles double click events on process nodes, in order to open
	 * introspector panels for the agent(s) that are selected. Single click
	 * events are handled by the tree selection listener.
	 *
	 * We have inner class here because ProcessPanel already extends JPanel so
	 * it can not also extend MouseAdapter.
	 *
	 * @see http
	 *      ://java.sun.com/j2se/1.4.2/docs/api/javax/swing/JTree.html for code.
	 */

	class MyMouseAdapter extends MouseAdapter {
		@Override
		public void mousePressed(MouseEvent event) {
			ActionFactory.broadcastStateChange(ProcessPanel.this.myIDEState);
			if (event.isPopupTrigger()) {
				ProcessPanel.this.processTree.getComponentPopupMenu().show(
						ProcessPanel.this.processTree, event.getX(),
						event.getY());
				return;
			}

			TreePath selPath = ProcessPanel.this.processTree
					.getPathForLocation(event.getX(), event.getY());
			if (selPath == null) { // nothing selected
				return;
			}
			ProcessNode node = (ProcessNode) selPath.getLastPathComponent();
			if (node != null && event.getClickCount() == 2) {
				try {
					// user double clicked process node
					ActionFactory.getAction(IntrospectorAction.class).Execute(
							node, null);
				} catch (Exception e) {
					new Warning(
							Resources
							.get(WarningStrings.FAILED_INTROSPECTOR_OPEN),
							e);
				}
			}
		}

		@Override
		public void mouseReleased(MouseEvent event) {
			if (event.isPopupTrigger()) {
				ProcessPanel.this.processTree.getComponentPopupMenu().show(
						ProcessPanel.this.processTree, event.getX(),
						event.getY());
				return;
			}
		}
	};
}
