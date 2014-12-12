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
import goal.tools.LaunchManager;
import goal.tools.errorhandling.Resources;
import goal.tools.errorhandling.Warning;
import goal.tools.errorhandling.WarningStrings;
import goal.tools.errorhandling.exceptions.GOALCommandCancelledException;
import goal.tools.errorhandling.exceptions.GOALException;
import goal.tools.errorhandling.exceptions.GOALIncompleteGUIUsageException;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.EventObject;

import javax.swing.JPanel;

import languageTools.program.agent.AgentId;
import nl.tudelft.goal.SimpleIDE.CloseTabbedPane.CloseTabbedPane;
import nl.tudelft.goal.SimpleIDE.CloseTabbedPane.TabCloseListener;
import nl.tudelft.goal.SimpleIDE.actions.CloseIntrospectorAction;
import nl.tudelft.goal.SimpleIDE.actions.GOALAction;
import nl.tudelft.goal.SimpleIDE.preferences.IntrospectorPreferences;

/**
 * The debug panel shows tabs with introspectors for agents that have been
 * launched.
 *
 */
@SuppressWarnings("serial")
public class DebugPanel extends CloseTabbedPane implements
		LayoutChangeListener, TabCloseListener {

	/**
	 *
	 */
	private static final long serialVersionUID = 3825712573125052729L;
	private final GOALAction closeIntroAction;

	/**
	 * Creates a debug panel.
	 *
	 * @param theIDE
	 *            is the central IDE functionality
	 */
	public DebugPanel() throws IllegalAccessException, InstantiationException {
		this.closeIntroAction = ActionFactory
				.getAction(CloseIntrospectorAction.class);
		setCloseListener(this);
	}

	/**
	 * callback from Listener
	 */
	@Override
	public void splitPositionChange(int newSplitPosition, EventObject event) {
		if (IntrospectorPreferences.getCoupleQuerySize()) {
			for (int n = 0; n < getComponentCount(); n++) {
				Component c = getComponent(n);
				if (c instanceof IntrospectorPanel) {
					((IntrospectorPanel) c)
							.setDividerLocation(newSplitPosition);
				}
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void closeOperation(AWTEvent e, int tabIndex) {
		try {
			// CHECK: not sure if component that you close is actually selected,
			// otherwise we could just call ide.close().
			Component c = getComponentAt(tabIndex);
			if (c instanceof IntrospectorPanel) {
				this.closeIntroAction.actionPerformed(new ActionEvent(e
						.getSource(), e.getID(), ((IntrospectorPanel) c)
						.getAgentId().getName()));
			}
		} catch (Exception er) {
			new Warning(Resources.get(WarningStrings.FAILED_PANEL_CLOSE), er);
		}
	}

	/**
	 * Shows introspector panel if available, otherwise creates one and shows
	 * it.
	 *
	 * @param agentName
	 *            DOC
	 */
	public void showIntrospectorPanel(String agentName) {
		IntrospectorPanel introspectorPanel;
		int index = indexOfTab(agentName);

		if (index == -1) { // create new introspector panel
			Agent<IDEGOALInterpreter> agent = LaunchManager.getCurrent()
					.getRuntimeManager().getAgent(new AgentId(agentName));
			introspectorPanel = new IntrospectorPanel(agent, this) {
				/**
				 *
				 */
				private static final long serialVersionUID = 2164856045676365243L;

				@Override
				public void close() {
					closeIntrospector(this);
				}
			};

			add(agentName, introspectorPanel);
		} else {
			introspectorPanel = (IntrospectorPanel) getComponentAt(index);
		}
		setSelectedComponent(introspectorPanel);
	}

	/**
	 * Closes the tab. Returns silently if introspector does not exist.
	 *
	 * @param DOC
	 */
	public void closeIntrospector(String name) {
		int n = indexOfTab(name);
		if (n == -1) { // tab does not exist, return silently
			return;
		}
		IntrospectorPanel introspectorPanel = (IntrospectorPanel) getComponentAt(n);
		closeIntrospector(introspectorPanel);
	}

	/**
	 * DOC
	 *
	 * @param ip
	 */
	private void closeIntrospector(IntrospectorPanel ip) {
		// TODO: check if we can remove: ip.close();
		remove(ip);
	}

	/**
	 * Closes the current tab, generic close command. Call needs to be re-routed
	 * through the IDE, because closing the tab may require deleting the debug
	 * observer.
	 *
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws GOALException
	 * @throws GOALIncompleteGUIUsageException
	 * @throws GOALCommandCancelledException
	 *
	 */
	public void close() throws GOALCommandCancelledException,
			GOALIncompleteGUIUsageException, GOALException,
			IllegalAccessException, InstantiationException {
		Component c = getSelectedComponent();
		if (c == null) {
			return;
		}
		if (c instanceof IntrospectorPanel) {
			this.closeIntroAction.actionPerformed(new ActionEvent(this, 1,
					((IntrospectorPanel) c).getAgentId().getName()));
		}
	}

	/**
	 * Closes all debug panels and prepares for IDE shutdown.
	 *
	 * @throws DOC
	 */
	public void closeAll() throws GOALException {
		for (int i = 0; i < getTabCount(); i++) {
			IntrospectorPanel ip = (IntrospectorPanel) getComponentAt(i);
			closeIntrospector(ip);
		}
		LaunchManager.getCurrent().shutDownRuntime();
	}

	/**
	 * Adds a given sniffer panel and makes it visible.
	 *
	 * @param snifferPanel
	 */
	public void addSnifferPanel(JPanel snifferPanel) {
		add("Sniffer", snifferPanel); //$NON-NLS-1$
		setSelectedComponent(snifferPanel);
	}

	/**
	 * Removes the sniffer panel.
	 *
	 * @param snifferPanel
	 */
	public void removeSnifferPanel(JPanel snifferPanel) {
		remove(snifferPanel);
	}

}
