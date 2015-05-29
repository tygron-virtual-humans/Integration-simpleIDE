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
import goal.preferences.DebugPreferences;
import goal.tools.IDEGOALInterpreter;
import goal.tools.debugger.Channel;
import goal.tools.errorhandling.Resources;
import goal.tools.errorhandling.Warning;
import goal.tools.errorhandling.WarningStrings;
import goal.tools.logging.Loggers;

import java.awt.AWTEvent;
import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import languageTools.program.agent.AgentId;
import nl.tudelft.goal.SimpleIDE.CloseTabbedPane.CloseTabbedPane;
import nl.tudelft.goal.SimpleIDE.CloseTabbedPane.TabCloseListener;
import nl.tudelft.goal.SimpleIDE.preferences.IDEPreferences;

/**
 * Shows the console, action, and other user feedback panels. The FeedbackPanel
 * is the lower area of the IDE panel holding. Subscribes to the Platform
 * Manager in order to show the messages sent by agents.
 */
public class FeedbackPanel extends CloseTabbedPane implements
RuntimeEventObserver, TabCloseListener, PropertyChangeListener,
ChangeListener {
	/**
	 *
	 */
	private static final long serialVersionUID = 8369272852845355060L;
	/**
	 * Main console panel for platform, logging, and error output.
	 */
	private final ConsoleTextPanel console = new ConsoleTextPanel();
	/**
	 * Panel that displays information about parsing of files.
	 */
	private final LogTextTrackingScrollPane parseTab = new LogTextTrackingScrollPane(
			""); //$NON-NLS-1$
	/**
	 * Panel that only records the actions that have been performed by agents.
	 */
	private final ActionHistoryTextPanel actionLog = new ActionHistoryTextPanel(
			"historyOfActions"); //$NON-NLS-1$
	/**
	 * Debug tracer tabs that are available.
	 */
	private final ConcurrentHashMap<AgentId, DebugTextPanel> tracers = new ConcurrentHashMap<AgentId, DebugTextPanel>();

	/**
	 * Main feedback panel with console, parsing, action history tabs and debug
	 * tracers.
	 */
	public FeedbackPanel() {

		// Add fixed panes.
		add("Console", this.console); //$NON-NLS-1$
		add("Parse Info", this.parseTab); //$NON-NLS-1$
		add("Action log", this.actionLog); //$NON-NLS-1$

		this.parseTab.subscribeTo(Loggers.getParserLogger());
		// Parsing is not done at runtime, so we can immediately print
		// everything.
		this.parseTab.setPushLevel(Level.ALL);

		// Listen to our own change events (when selected tab changes).
		addChangeListener(this);

		// Listen to close button.
		setCloseListener(this);

		// Listen to changes user makes to preferences.
		DebugPreferences.addChangeListener(this);
	}

	/**
	 * Creates debug tracer tab for an agent.
	 *
	 * @param agent
	 *            The agent to be traced in the debug tab.
	 */
	public void openDebugTracer(final Agent<IDEGOALInterpreter> agent) {
		if (this.tracers.containsKey(agent.getId())) {
			// nothing to do; silently return.
			return;
		}

		// Create new tab.
		final DebugTextPanel tracer = new DebugTextPanel(agent);
		this.tracers.put(agent.getId(), tracer);

		// Add it to this feedback panel.
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				add(agent.getId().getName(), tracer);
			}
		});
	}

	// ********************** OBSERVER METHODS ***********************/

	/**
	 * Handles events from {@link RuntimeManager}.
	 *
	 * @param observable
	 *            The {@link MonitoringService}.
	 * @param argument
	 *            A {@link RuntimeEvent} event.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void eventOccured(RuntimeManager<?, ?> observable, RuntimeEvent event) {
		Object source = event.getSource();
		Agent<IDEGOALInterpreter> agent;

		switch (event.getType()) {
		case MAS_BORN:
			this.actionLog.setText(""); //$NON-NLS-1$
			break;
		case AGENT_IS_LOCAL_AND_READY:
			agent = (Agent<IDEGOALInterpreter>) source;
			// Check whether we should open a debug tracer for the agent.
			if (IDEPreferences.getOpenDebugTraceTabAtAgentLaunch()) {
				openDebugTracer(agent);
			}
			// Subscribe the actionLog panel (only if user wants to see
			// actions).
			this.actionLog.subscribeToDebugger(agent.getController()
					.getDebugger());
			break;
		case MAS_DIED:
			Set<AgentId> agentIds = this.tracers.keySet();
			for (AgentId id : agentIds) {
				int i = indexOfTab(id.getName());
				closeTab(i);
			}
			break;
			// case SCHEDULER_SELECTED_AGENT:
			// ArrayList<Agent> agents = (ArrayList<Agent>) source;
			// int index = indexOfTab(agents.get(0).getId().getName());
			//
			// if (agents.size() == 1
			// && (index != -1)
			// && (agents.get(0).getDebugger().getRunMode() != RunMode.RUNNING)
			// && IDEPreferences.getAutoSwitchDebugTab()) {
			// /*
			// * possible to switch to unique debug pane switch to and make
			// * visible corresponding debug pane for agent only switch if
			// * agent is not in RUNNING mode and currently selected index is
			// * debug pane
			// */
			// this.setSelectedIndex(index);
			// }
			// break;
		default:
			break;
		}
	}

	/**
	 * Catch changes in preferences in DebugPreferencePanel, so that views can
	 * be made and removed when necessary. See
	 * {@link goal.tools.SimpleIDE.preferences.DebugPreferencePane}.
	 */
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		Channel channel = null;

		try {
			channel = Channel.valueOf(evt.getPropertyName());
		} catch (IllegalArgumentException ex) {
			// ignore non-channel keys
			return;
		}

		handleChannelViewUpdateEvent(channel,
				DebugPreferences.getChannelState(channel).canView());
	}

	/**
	 * Handles view update event from user related to debug output preferences.
	 * See DebugPreferencePane.
	 *
	 * @param channel
	 *            channel for which user has updated viewing preference.
	 * @param value
	 *            true if user wants to view channel, false otherwise.
	 */
	private void handleChannelViewUpdateEvent(Channel channel, boolean value) {
		for (DebugTextPanel tracer : this.tracers.values()) {
			if (value) {
				tracer.addViewChannel(channel);
			} else {
				tracer.removeViewChannel(channel);
			}
		}
	}

	/**
	 * called back when tab close button is pressed
	 */
	@Override
	public void closeOperation(AWTEvent e, int tabIndex) {
		closeTab(tabIndex);
	}

	/**
	 * Closes a given tab, but only if it is a debug tracer tab.
	 *
	 * @param tabIndex
	 *            Index to find the tab.
	 */
	private void closeTab(final int tabIndex) {
		/**
		 * Events give us an index, but we need to convert to an object as soon
		 * as possible because the number of tabs and order may change any time,
		 * particularly during shutdown.
		 */
		final Component c = getComponentAt(tabIndex);
		if (c instanceof DebugTextPanel) {
			this.tracers.values().remove(c);

			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					try {
						((DebugTextPanel) c).cleanUp();
						remove(c);
					} catch (Exception ex) {
						new Warning(Resources
								.get(WarningStrings.FAILED_TAB_CLOSE), ex);
					}
				}
			});
		}
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		// this is called whenever this panel changes state.
		// that is, whenever another tab is selected, and just before
		// this panel is (re)painted.

		JComponent tab = (JComponent) getSelectedComponent();
		if (tab instanceof MarkedReadable) {
			((MarkedReadable) tab).markRead();
		}
	}
}
