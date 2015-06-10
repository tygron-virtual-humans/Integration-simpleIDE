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
import goal.core.mentalstate.BeliefBase;
import goal.tools.IDEDebugger;
import goal.tools.IDEGOALInterpreter;
import goal.tools.debugger.Channel;
import goal.tools.debugger.DebugEvent;
import goal.tools.debugger.DebugObserver;
import goal.tools.errorhandling.Resources;
import goal.tools.errorhandling.Warning;
import goal.tools.errorhandling.WarningStrings;
import goal.util.Observer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import mentalState.BASETYPE;

/**
 * Database panel including a query panel. This panel will directly call to the
 * Agent for listings and queries.
 *
 * @author W.Pasman
 * @modified N.Kraayenbrink Removed timer, made the panel observe the database
 *           it is showing the contents of.
 * @modified K.Hindriks Panel now owns mental state but no longer agent; TODO:
 *           panel should receive incremental updates for updating panel
 *           contents instead of owning the mental state; see also
 *           {@link #refreshPanelContent()}.
 */
public class DatabasePanel extends JPanel implements DebugObserver,
		Observer<QueryPanel, DatabaseChangedInfo> {
	/**
	 *
	 */
	private static final long serialVersionUID = 6973991836787879497L;
	/**
	 * The type of database this {@link DatabasePanel} displays.
	 */
	private final BASETYPE databaseType;
	/**
	 * The mental state that contains the database that is displayed in this
	 * {@link DatabasePanel}.
	 */
	private final Agent<IDEGOALInterpreter> agent;
	private final IDEDebugger debugger;
	/**
	 * Text area and font size used for displaying database contents.
	 */
	private final JTextArea databaseText;
	private static final int FONT_SIZE = 12;

	/**
	 * DOC
	 *
	 * @param mentalState
	 *            The mental state that contains the database displayed.
	 * @param datatype
	 *            The type of database that should be displayed.
	 * @param initialtext
	 *            The initial text for the panel.
	 */
	public DatabasePanel(Agent<IDEGOALInterpreter> agent, BASETYPE datatype,
			String initialtext) {
		this.agent = agent;
		this.debugger = agent.getController().getDebugger();
		this.databaseType = datatype;

		// Initialize panel.
		setLayout(new BorderLayout());
		this.databaseText = new JTextArea(initialtext);
		JScrollPane databaseoutput = new JScrollPane(this.databaseText);
		this.databaseText.setEditable(false);
		this.databaseText.setBackground(Color.white);
		this.databaseText.setFont(new Font("Courier", Font.PLAIN, FONT_SIZE)); //$NON-NLS-1$
		add(databaseoutput, BorderLayout.CENTER);

		// Add observer to agent's debugger to receive notifications of
		// relevant changes.
		switch (datatype) {
		case BELIEFBASE:
			agent.getController().getDebugger()
					.subscribe(this, Channel.BB_UPDATES);
			break;
		case GOALBASE:
			agent.getController().getDebugger()
					.subscribe(this, Channel.GB_UPDATES);
			agent.getController().getDebugger()
					.subscribe(this, Channel.GOAL_ACHIEVED);
			agent.getController().getDebugger()
					.subscribe(this, Channel.GB_CHANGES);
			break;
		case KNOWLEDGEBASE:
			// No channel to subscribe to because knowledge base does not
			// change.
			break;
		case MAILBOX:
			agent.getController().getDebugger().subscribe(this, Channel.MAILS);
			break;
		case PERCEPTBASE:
			// Percept base changes are reported on
			// Channel.PERCEPTS_CONDITIONAL_VIEW.
			agent.getController().getDebugger()
					.subscribe(this, Channel.PERCEPTS_CONDITIONAL_VIEW);
			break;
		case EMOTIONBASE:
			// Percept base changes are reported on
			// Channel.EMOTIONS_CONDITIONAL_VIEW.
			agent.getController().getDebugger()
					.subscribe(this, Channel.EMOTIONS_CONDITIONAL_VIEW);
			break;
		}

		refreshPanelContent();
	}

	/**
	 * Refreshes the panel.
	 *
	 * TODO: this is really nasty code, we really should not be manipulating
	 * agent code from here. First, this is another thread, we risk thread
	 * safety issues (ConcurrentModificationExceptions) Second, if this is
	 * executed in a timer thread instead of SWING we have multiple threads in
	 * the GUI, which is bad practice
	 * (http://java.sun.com/products/jfc/tsc/articles/threads/threads1.html)
	 */
	private void refreshPanelContent() {
		try {
			StringBuffer buffer = new StringBuffer();
			switch (this.databaseType) {
			case BELIEFBASE:
				// ENABLE FOLLOWING TO ALSO GET VIEW OF REAL PROLOG DATABASE
				// buffer.append("\n----------------------\n");
				// DatabaseFormula[] forms = beliefbase.getLanguage()
				// .getInferenceEngine()
				// .getAllSentences(beliefbase.getDatabase());
				// for (int i = 0; i < forms.length; i++)
				// buffer.append(forms[i] + "\n");
				// mentalstate.getBeliefBase().get(0).getDatabase().showStatistics();
				// //DEBUG
			case MAILBOX:
			case PERCEPTBASE:
			case EMOTIONBASE:
				BeliefBase base = this.agent.getController().getRunState()
						.getMentalState().getOwnBase(this.databaseType);
				if (base != null) {
					buffer.append(base.getTheory().toString());
				}
				break;
			case GOALBASE:
				buffer.append(this.agent.getController().getRunState()
						.getMentalState().printAttentionStack());
				/*
				 * / ENABLE FOLLOWING TO ALSO GET VIEW OF REAL PROLOG GOALBASE
				 */
				// for (SingleGoal goal :
				// mentalstate.getAttentionSet().getGoals()) {
				// buffer.append("\n-----------" + goal + "-----------\n");
				// DatabaseFormula[] forms = mentalstate.getAttentionSet()
				// .getLanguage().getInferenceEngine()
				// .getAllSentences(goal.getGoalDatabase());
				// for (int i = 0; i < forms.length; i++)
				// buffer.append(forms[i] + "\n");
				// }
				break;
			case KNOWLEDGEBASE:
				// Nothing to do because knowledge base is static and therefore
				// does not need to be refreshed.
				return;
			}

			// Try to maintain the caret position.
			int oldCaretPos = this.databaseText.getCaretPosition();
			this.databaseText.setText(buffer.toString());
			if (oldCaretPos > buffer.length()) {
				this.databaseText.setCaretPosition(buffer.length());
			} else {
				this.databaseText.setCaretPosition(oldCaretPos);
			}
		} catch (Exception e) {
			new Warning(Resources.get(WarningStrings.FAILED_AWT_REFRESH_PANEL),
					e);
		}
	}

	@Override
	public String getObserverName() {
		return "DB_Window_" + this.databaseType; //$NON-NLS-1$
	}

	@Override
	public void notifyBreakpointHit(DebugEvent event) {
		refreshPanelContent();
	}

	@Override
	public void removeNotify() {
		this.debugger.unsubscribe(this);

		System.out.println("unsubscribed " + getObserverName()); //$NON-NLS-1$
	}

	/**
	 * called when database changed in QueryPanel. HACK I can't get the generics
	 * right. Should be more generic but java complains.
	 */
	@Override
	public void eventOccured(QueryPanel source, DatabaseChangedInfo evt) {
		refreshPanelContent();
	}
}
