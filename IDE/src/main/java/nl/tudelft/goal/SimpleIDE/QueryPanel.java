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
import goal.tools.IDEGOALInterpreter;
import goal.tools.eclipse.QueryTool;
import goal.tools.errorhandling.exceptions.GOALUserError;
import goal.util.DefaultObservable;
import goal.util.Observable;
import goal.util.Observer;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * Panel offering query possibilities inside an {@link IntrospectorPanel}.
 *
 * @author W.Pasman 4aug09: now using TextTrackingScrollPane to add clear
 *         button.
 */
@SuppressWarnings("serial")
public class QueryPanel extends JPanel
		implements
		FocusListener,
		Observable<Observer<QueryPanel, DatabaseChangedInfo>, QueryPanel, DatabaseChangedInfo> {

	/**
	 *
	 */
	private static final long serialVersionUID = 6879799954644956930L;

	private static final String INITIAL_TEXT = "Enter your query here";

	// preferred sizes of query text area.
	private static final int QUERY_TEXT_WIDTH = 40;
	private static final int QUERY_TEXT_HEIGHT = 2;

	private final JButton querybutton = new JButton("Query");
	private final JButton actionbutton = new JButton("Action");
	private final JTextArea querytext = new JTextArea(QUERY_TEXT_HEIGHT,
			QUERY_TEXT_WIDTH);
	private final JScrollPane querytextscrollpane = new JScrollPane(
			this.querytext);

	private final LogTextTrackingScrollPane queryresult = new LogTextTrackingScrollPane(
			"query results come here");
	private final JPanel querytop = new JPanel(); // query button and query
	// input area

	private final Agent<IDEGOALInterpreter> agent;

	private final DefaultObservable<Observer<QueryPanel, DatabaseChangedInfo>, QueryPanel, DatabaseChangedInfo> myObservable = new DefaultObservable<Observer<QueryPanel, DatabaseChangedInfo>, QueryPanel, DatabaseChangedInfo>();

	/**
	 * @param agent
	 *            is Agent to contact in order to get the database and to handle
	 *            queries.
	 * @param parent
	 *            the IntrospectorPanel containing this, allowing us to force
	 *            refresh when we execute action. HACK #2923
	 * @param datatype
	 *            is type of the database: BELIEFS, GOALS, MAILS or PERCEPTS
	 */
	public QueryPanel(Agent<IDEGOALInterpreter> agent) {
		this.agent = agent;
		this.setLayout(new BorderLayout());

		this.querytext.setText(INITIAL_TEXT);
		this.querytext.addKeyListener(new java.awt.event.KeyAdapter() {
			@Override
			public void keyPressed(java.awt.event.KeyEvent evt) {
				if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
					// check if query already complete.
					// if so, do the query and consume the enter.
					evt.consume();
					try {
						QueryPanel.this.doquery();
					} catch (GOALUserError e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} // TODO if query is expensive or
						// even crashes,
						// this locks up the IDE
				}
			}
		});

		this.querytext.addFocusListener(this);

		/*
		 * we want the query text to auto-size, therefore we place that in the
		 * center. the rest must be in WEST.
		 */
		JPanel buttons = new JPanel(new FlowLayout());
		buttons.add(this.actionbutton);
		buttons.add(this.querybutton);
		this.querytop.setLayout(new BorderLayout());
		this.querytop.add(buttons, BorderLayout.WEST);
		this.querytop.add(this.querytextscrollpane, BorderLayout.CENTER);

		this.add(this.querytop, BorderLayout.NORTH);
		this.add(this.queryresult, BorderLayout.CENTER);

		this.querybutton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					QueryPanel.this.doquery();
				} catch (GOALUserError e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});

		this.actionbutton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					QueryPanel.this.doaction();
				} catch (GOALUserError e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});

	}

	/**
	 * Performs query when the query button in the panel is pressed.
	 *
	 * @throws GOALUserError
	 */
	private void doquery() throws GOALUserError {
		this.queryresult.setText("");
		try {
			QueryTool query = new QueryTool(this.agent);
			String resulttext = query.doquery(this.querytext.getText());
			this.queryresult.setText(resulttext);
		} catch (GOALUserError e) {
			this.queryresult.setText("Query failed: " + e.getMessage());
			throw e;
		}
	}

	/**
	 * Performs query when the query button in the panel is pressed.
	 */
	public void doaction() throws GOALUserError {
		try {
			QueryTool action = new QueryTool(this.agent);
			String message = action.doaction(this.querytext.getText());
			this.queryresult.setText(message);
		} catch (GOALUserError e) {
			this.queryresult.setText("Action failed: " + e.getMessage());
			throw e;
		}
		notifyObservers(this, new DatabaseChangedInfo());
	}

	@Override
	public void focusGained(FocusEvent arg0) {
		if (this.querytext.getText().equals(INITIAL_TEXT)) {
			this.querytext.setText("");
		}
	}

	@Override
	public void focusLost(FocusEvent arg0) {
		// don't care but must implement
	}

	/*********************************
	 * Implements Observable<>
	 ********************************/
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addObserver(Observer<QueryPanel, DatabaseChangedInfo> observer) {
		this.myObservable.addObserver(observer);

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void removeObserver(
			Observer<QueryPanel, DatabaseChangedInfo> observer) {
		this.myObservable.removeObserver(observer);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void notifyObservers(QueryPanel src, DatabaseChangedInfo obj) {
		this.myObservable.notifyObservers(this, obj);
	}

}
