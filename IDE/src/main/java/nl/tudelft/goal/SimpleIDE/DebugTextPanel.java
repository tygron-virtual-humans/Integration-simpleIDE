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
import goal.preferences.DebugPreferences;
import goal.tools.IDEDebugger;
import goal.tools.IDEGOALInterpreter;
import goal.tools.debugger.Channel;
import goal.tools.debugger.Channel.ChannelState;
import goal.tools.debugger.DebugEvent;
import goal.tools.debugger.DebugObserver;
import goal.tools.debugger.SteppingDebugger.RunMode;
import goal.tools.logging.GOALLogger;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import languageTools.program.agent.AgentId;

/**
 * <p>
 * Shows debug trace information and a level number input field. Note that this
 * panel uses a debug observer separate from the Platform Manager.
 * <p/>
 * <p>
 * So we have a bit curious situation. This panel, that wants to show the debug
 * messages does the following:
 * <ol>
 *
 * <li>it creates a {@link GOALLogger} for the messages
 * <li>It subscribes ITSELF to the source that it wants to log, eg the
 * {@link IDEDebugger}.
 * <li>When an event that it wants to report/show comes in, it does not write it
 * directly to the screen but dumps the message into the loggger that it made.
 * <li>The panel extends the {@link LogTextTrackingScrollPane} that catches the
 * log and writes it to the screen.
 * </ol>
 * </p>
 *
 * @author W.Pasman
 * @modified KH091218 clean up, added debug viewing channels
 * @modified N.Kraayenbrink Does not directly display incoming debug events, but
 *           logs them. Flushes once a PAUSED event is received.
 * */
@SuppressWarnings("serial")
public final class DebugTextPanel extends LogTextTrackingScrollPane implements
DebugObserver, PropertyChangeListener {

	/**
	 *
	 */
	private static final long serialVersionUID = 2048259638325071337L;
	private final AgentId agentId;
	private final GOALLogger logger;
	private IDEDebugger debugger;

	/**
	 * Displays debug information associated with a specific agent. Subscribes
	 * to debugger associated with agent to receive debug event information.
	 * Subscribes to viewing those debug channels (events) selected and
	 * preferred by the user.
	 *
	 * @param agent
	 *            corresponding agent whose debug output needs to be shown on
	 *            panel.
	 */
	public DebugTextPanel(Agent<IDEGOALInterpreter> agent) {
		super("");

		this.agentId = agent.getId();

		// create an anonymous logger, in order to make sure having a unique one
		// for each agent
		// this logger will log all incoming DebugEvents
		// this.theLogger = GOALLogger.getAnonymousLogger();
		this.logger = new GOALLogger(this.agentId.getName(), true);
		// subscribe to the created anonymous logger
		subscribeTo(this.logger);
		this.debugger = agent.getController().getDebugger();

		// subscribe only after fields have been set!!! Callback may happen
		// immediately
		// we NEED the RUNMODE channel to trigger text flush
		this.debugger.subscribe(this, Channel.RUNMODE);

		// we also want to see the round separator here.
		// No we don't #3005. Performance issue as this is also going to the log
		// file. Let the user turn that on if he
		// wants to see it.
		// debugger.subscribe(this, Channel.REASONING_CYCLE_SEPARATOR);

		for (Channel channel : Channel.values()) {
			if (DebugPreferences.getChannelState(channel).canView()) {
				addViewChannel(channel);
			}
		}

		DebugPreferences.addChangeListener(this);
	}

	/**
	 * Returns name of the debug text pane.
	 *
	 * @return name of debug text pane, same name as associated agent.
	 */
	@Override
	public String getObserverName() {
		return "debugtextpanel_" + this.agentId;
	}

	/**
	 * Prints events received from debugger. Handles selection events from
	 * scheduler separately, as these indicate round updates.
	 *
	 * @param event
	 *            The debug event received from the debugger.
	 */
	@Override
	public synchronized void notifyBreakpointHit(DebugEvent event) {

		if (event.getChannel() != Channel.RUNMODE) {
			// Log the event; wait with printing.
			this.logger.log(event);
		}

		if (event.getRunMode().equals(RunMode.PAUSED)
				|| event.getRunMode().equals(RunMode.KILLED)
				|| event.getChannel() == Channel.SLEEP) {
			super.flush();
		}
	}

	/**
	 * Adds a channel to the channels which the observer wants to view.
	 *
	 * @param channel
	 *            channel which is to be viewed.
	 */
	public synchronized void addViewChannel(Channel channel) {
		this.debugger.subscribe(this, channel);
		if (!Channel.getConditionalChannel(channel).equals(channel)) {
			this.debugger.subscribe(this,
					Channel.getConditionalChannel(channel));
		}
	}

	/**
	 * Removes a channel from the list of channels which the observer wants to
	 * view.
	 *
	 * @param channel
	 *            channel which no longer should be viewed.
	 */
	public synchronized void removeViewChannel(Channel channel) {
		this.debugger.unsubscribe(this, channel);
		if (!Channel.getConditionalChannel(channel).equals(channel)) {
			this.debugger.unsubscribe(this,
					Channel.getConditionalChannel(channel));
		}
	}

	/**
	 * Here we catch user edits in the debug settings and forward the
	 * modifications to the debugger.
	 */
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		// keys we are interested in can be parsed by Channel.vaulueOf
		Channel channel = null;
		try {
			channel = Channel.valueOf(evt.getPropertyName());
		} catch (IllegalArgumentException ex) {
			// an IAE is thrown when the value can not be parsed ignore those
			// keys.
			return;
		}

		if (ChannelState.valueOf(evt.getNewValue().toString()).canView()) {
			this.debugger.subscribe(this, channel);
		} else {
			this.debugger.unsubscribe(this, channel);
		}
	}

	/**
	 * Unsubscribes panel as observer and releases debugger.
	 */
	public void cleanUp() {
		unsubscribeFrom(this.logger);
		DebugPreferences.removeChangeListener(this);
		this.debugger.unsubscribe(this);
		this.debugger = null;
	}

	/**
	 * Returns a brief description of this {@link DebugTextPanel}, including:
	 * the associate agent, the associated logger, and the stored separator
	 * events. Details of the exact representation or format are not specified
	 * here.
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder("Debug trace window for ");
		builder.append(this.agentId);
		builder.append("\nSubscribed to debugger ");
		builder.append(this.debugger.getName());
		return builder.toString();
	}

}