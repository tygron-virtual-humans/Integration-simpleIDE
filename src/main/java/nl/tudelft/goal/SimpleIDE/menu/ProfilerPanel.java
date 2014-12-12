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

package nl.tudelft.goal.SimpleIDE.menu;

import goal.core.agent.AgentId;
import goal.tools.IDEDebugger;
import goal.tools.LaunchManager;
import goal.tools.PlatformManager;
import goal.tools.debugger.Channel;
import goal.tools.debugger.DebugEvent;
import goal.tools.debugger.DebugObserver;
import goal.tools.debugger.Debugger;

import java.util.Arrays;

import nl.tudelft.goal.SimpleIDE.LogTextTrackingScrollPane;

/**
 * 
 * TODO Complete this partial implementation.
 * <p>
 * Suggestion for ProfilerPanel. See TRAC 797. shows Agent profile information.
 * Added because standard profilers for e.g. Java do not work as we want. It is
 * hard to focus on particular GOAL objects, instead most profilers use stack
 * traces which is not convenient because 1. in many threads we have no interest
 * 2. separate calls to a function are often profiled separately, 3. Once you
 * start logging everything the system performance drops dramatically.
 * <p/>
 * 
 * @author W.Pasman
 * */
@SuppressWarnings("serial")
public class ProfilerPanel extends LogTextTrackingScrollPane implements
		DebugObserver {
	private final IDEDebugger agentdebugger;
	private String agentname = null;

	public ProfilerPanel(String agt) {
		super("");
		agentname = agt;

		// subscribe to all channels
		Arrays.asList(Channel.values());

		agentdebugger = LaunchManager.getCurrent().getRuntimeManager()
				.getAgent(new AgentId(agentname)).getController().getDebugger();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void notifyBreakpointHit(DebugEvent info) {
		if (info.getMessage() == null) {
			return; // do not print non-messages.
		}
		System.nanoTime();
		super.append(info + "\n");
	}

	public Debugger getDebugger() {
		return agentdebugger;
	}

	/**
	 * Cleans up before closing down the panel. Unsubscribes panel as observer.
	 */
	public void close() {
		super.append("Panel closedown requested\n");
		agentdebugger.unsubscribe(this);
	}

	@Override
	public String getObserverName() {
		return "profilerpanel";
	}

}