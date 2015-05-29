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

import goal.preferences.DebugPreferences;
import goal.tools.IDEDebugger;
import goal.tools.debugger.Channel;
import goal.tools.debugger.DebugEvent;
import goal.tools.debugger.DebugObserver;
import goal.tools.debugger.SteppingDebugger.RunMode;
import goal.tools.logging.GOALLogger;

import java.util.HashMap;
import java.util.Map;

/**
 * Shows the actions performed by the agents.
 *
 * @author W.Pasman
 * @modified KH now shows actions performed by agents.
 */
@SuppressWarnings("serial")
public class ActionHistoryTextPanel extends LogTextTrackingScrollPane implements
		DebugObserver {

	/**
	 *
	 */
	private static final long serialVersionUID = 4971396704382055797L;
	/**
	 * Debug observers are identified by their label
	 */
	private final String name;
	/**
	 *
	 */
	private final GOALLogger logger;
	/**
	 * The last received message on the ROUND_SEPARATION channel.
	 */
	private final Map<String, DebugEvent> lastSepEvents = new HashMap<String, DebugEvent>();

	/**
	 * Panel logging the actions performed by actions.
	 *
	 * @param name
	 *            The name of the panel.
	 */
	public ActionHistoryTextPanel(String name) {
		super("");

		this.name = name;

		// create and subscribe to a new logger.
		this.logger = new GOALLogger(name, true);
		this.subscribeTo(this.logger);
	}

	@Override
	public String getObserverName() {
		return this.name;
	}

	/**
	 * Subscribes to debugger of agent. Call when agent is born.
	 *
	 * @param debugger
	 *            The debugger to subscribe to.
	 */
	public void subscribeToDebugger(IDEDebugger debugger) {
		// Check whether we should subscribe. Only do this if at least one of
		// the channels
		// we would like to view here has been selected for viewing by the user
		// as well.
		boolean viewBuiltin = DebugPreferences.getChannelState(
				Channel.ACTION_EXECUTED_BUILTIN).canView();
		boolean viewUserSpec = DebugPreferences.getChannelState(
				Channel.ACTION_EXECUTED_USERSPEC).canView();
		// CYCLE SEPARATOR channel is subscribed to for layout reasons; user
		// does not need to select it.
		// RUNMODE channel is always hidden and cannot be selected for viewing
		// by the user; do not check it.

		// Subscribe.
		if (viewBuiltin || viewUserSpec) {
			debugger.subscribe(this, Channel.RUNMODE);
			debugger.subscribe(this, Channel.REASONING_CYCLE_SEPARATOR);
			if (viewUserSpec) {
				debugger.subscribe(this, Channel.ACTION_EXECUTED_USERSPEC);
			}
			if (viewBuiltin) {
				debugger.subscribe(this, Channel.ACTION_EXECUTED_BUILTIN);
			}
		}
	}

	/**
	 * Prints update received from debugger to text pane.
	 */
	@Override
	public synchronized void notifyBreakpointHit(DebugEvent event) {
		switch (event.getChannel()) {
		case RUNMODE:
			// Flush if the run mode is PAUSED or KILLED.
			if (event.getRunMode().equals(RunMode.PAUSED)
					|| event.getRunMode().equals(RunMode.KILLED)) {
				super.flush();
			}
			break;
		case REASONING_CYCLE_SEPARATOR:
			// Store event to prevent printing the round separator for each
			// agent
			// when there are no executed actions in the round.
			this.lastSepEvents.put(event.getSource(), event);
			break;
		case ACTION_EXECUTED_BUILTIN:
		case ACTION_EXECUTED_USERSPEC:
			if (event.getSource() != null
					&& this.lastSepEvents.get(event.getSource()) != null) {
				this.logger.log(this.lastSepEvents.get(event.getSource()));
				this.lastSepEvents.remove(event.getSource());
			}
			this.logger.log(event);
			break;
		default:
			// we did not subscribe to any other channels.
		}
	}

	/**
	 * Returns a brief description of this {@link ActionHistoryTextPanel},
	 * including: its name, the associated logger, and the stored separator
	 * events. Details of the exact representation or format are not specified
	 * here.
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder("Action history panel = ");
		builder.append(this.name);
		builder.append("\nStored separator events:\n");
		builder.append(this.lastSepEvents.toString());
		return builder.toString();
	}

}
