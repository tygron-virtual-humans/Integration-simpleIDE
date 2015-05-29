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
import goal.tools.IDEDebugger;
import goal.tools.IDEGOALInterpreter;
import goal.tools.debugger.Channel;
import goal.tools.debugger.DebugEvent;
import goal.tools.debugger.DebugObserver;
import goal.tools.debugger.SteppingDebugger.RunMode;
import goal.util.Observable;
import goal.util.Observer;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;

import krTools.language.DatabaseFormula;
import languageTools.program.agent.AgentId;
import mentalState.BASETYPE;
import nl.tudelft.goal.SimpleIDE.preferences.IntrospectorPreferences;

/**
 * Provides a number of tabs in an introspector window for displaying the
 * contents of the mental state of an agent. Includes a query area for
 * evaluating mental state conditions on this mental state and for performing
 * built-in actions.
 *
 * @author W.Pasman
 * @modified K.Hindriks
 */

@SuppressWarnings("serial")
public class IntrospectorPanel extends JPanel implements
PropertyChangeListener, DebugObserver {
	/**
	 *
	 */
	private static final long serialVersionUID = -196241958360230968L;
	/**
	 * The id and the debugger of the agent that is introspected through this
	 * panel.
	 */
	private final AgentId id;
	private final IDEDebugger debugger;
	/**
	 * The window is a split pane including the introspector and query area.
	 */
	private final JSplitPane pane;
	private final LayoutChangeListener layoutlistener;

	/**
	 * DOC
	 *
	 * @param agent
	 *            The agent associated with this introspector window.
	 * @param listener
	 *            A LayoutChangeListener which is informed about layout changes.
	 */
	public IntrospectorPanel(Agent<IDEGOALInterpreter> agent,
			LayoutChangeListener listener) {
		this.layoutlistener = listener;

		setLayout(new BorderLayout());

		// add first agent panel
		MentalComponentsPanel tabs = new MentalComponentsPanel(agent);
		QueryPanel querypanel = new QueryPanel(agent);
		this.pane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tabs, querypanel);
		this.pane
		.setDividerLocation(IntrospectorPreferences.getDBContentSize());

		this.pane.setResizeWeight(1.0);
		this.pane.setOneTouchExpandable(true);
		this.pane.setContinuousLayout(true);
		this.pane.addPropertyChangeListener(this);

		this.add(this.pane, BorderLayout.CENTER);

		// Subscribe to agent debugger on run mode channel to get
		// notifications of agent that has been killed.
		this.id = agent.getId();
		this.debugger = agent.getController().getDebugger();
		this.debugger.subscribe(this, Channel.RUNMODE);

		tabs.informAbout(querypanel);
	}

	/**
	 * DOC which events need to be handled here?
	 */
	@Override
	public void propertyChange(PropertyChangeEvent event) {
		this.layoutlistener.splitPositionChange(this.pane.getDividerLocation(),
				event);
	}

	/**
	 * Store any changes made to size of window.
	 */
	@Override
	public void removeNotify() {
		this.debugger.unsubscribe(this);

		IntrospectorPreferences
		.setDBContentSize(this.pane.getDividerLocation());
	}

	/**
	 * Returns the id of the agent whose mental state is displayed here.
	 *
	 * @return The agent's id.
	 */
	public AgentId getAgentId() {
		return this.id;
	}

	/**
	 * DOC Important: Only set the location if it differs from the current one,
	 * to prevent infinite recursion of change events.
	 *
	 * @param loc
	 */
	public void setDividerLocation(int loc) {
		if (this.pane.getDividerLocation() != loc) {
			this.pane.setDividerLocation(loc);
		}
	}

	@Override
	public String getObserverName() {
		return "IntroPanel_" + this.id;
	}

	@Override
	public void notifyBreakpointHit(DebugEvent event) {
		if (event.getRunMode().equals(RunMode.KILLED)) {
			close();
		}
	}

	/**
	 * Override in {@link DebugPanel#showIntrospectorPanel(String)}.
	 */
	public void close() {
	}

}

/**
 * Displays the various components of an agent's mental state in different tabs,
 * including the agent's beliefs, goals, mails, percepts, and knowledge.
 */
@SuppressWarnings("serial")
class MentalComponentsPanel extends JTabbedPane {

	/**
	 *
	 */
	private static final long serialVersionUID = 1725662313063232914L;

	private static final String INITTEXT = "Database contents will appear here...";

	private final DatabasePanel beliefs, goals, mails, percepts, knowledge;

	/**
	 * DOC
	 *
	 * @param agent
	 *            The agent associated with the panel.
	 */
	public MentalComponentsPanel(Agent<IDEGOALInterpreter> agent) {
		// Get agent with ID agentId
		// convert knowledge from array to string
		Iterable<DatabaseFormula> knowledgebase = agent.getController()
				.getProgram().getAllKnowledge();
		StringBuffer kbtext = new StringBuffer();
		for (DatabaseFormula formula : knowledgebase) {
			kbtext.append(formula.toString());
			kbtext.append(".\n");
		}

		// The database panels.
		this.beliefs = new DatabasePanel(agent, BASETYPE.BELIEFBASE, INITTEXT);
		this.goals = new DatabasePanel(agent, BASETYPE.GOALBASE, INITTEXT);
		this.mails = new DatabasePanel(agent, BASETYPE.MAILBOX, INITTEXT);
		this.percepts = new DatabasePanel(agent, BASETYPE.PERCEPTBASE, INITTEXT);
		this.knowledge = new DatabasePanel(agent, BASETYPE.KNOWLEDGEBASE,
				kbtext.toString());

		this.add("Beliefs", this.beliefs);
		this.add("Goals", this.goals);
		this.add("Mails", this.mails);
		this.add("Percepts", this.percepts);
		this.add("Knowledge", this.knowledge);
	}

	/**
	 * called from above, if there is a query panel attached that may tell the
	 * panels to refresh.
	 *
	 * @param changenotifier
	 */
	public void informAbout(
			Observable<Observer<QueryPanel, DatabaseChangedInfo>, QueryPanel, DatabaseChangedInfo> changenotifier) {
		changenotifier.addObserver(this.beliefs);
	}

}
