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

package nl.tudelft.goal.SimpleIDE.actions;

import goal.tools.errorhandling.Resources;
import goal.tools.errorhandling.Warning;
import goal.tools.errorhandling.WarningStrings;
import goal.tools.errorhandling.exceptions.GOALException;

import java.awt.event.ActionEvent;
import java.util.List;

import nl.tudelft.goal.SimpleIDE.IDEMainPanel;
import nl.tudelft.goal.SimpleIDE.IDENode;
import nl.tudelft.goal.SimpleIDE.IconFactory;
import nl.tudelft.goal.SimpleIDE.NodeType;

/**
 * Open introspector for given node(s).
 *
 * @author W.Pasman 20jun2011
 */
public class IntrospectorAction extends GOALAction {
	/**
	 *
	 */
	private static final long serialVersionUID = -6571389905183333021L;

	public IntrospectorAction() {
		setIcon(IconFactory.DEBUG.getIcon());
		setShortcut('D');
		setDescription("Launch introspector for selected agent"); //$NON-NLS-1$
	}

	@Override
	/**
	 * {@inheritDoc}
	 */
	public void stateChangeEvent() {
		List<? extends IDENode> selection = this.currentState
				.getSelectedNodes();
		if (selection.isEmpty()) {
			setActionEnabled(false);
			return;
		}

		setActionEnabled(this.currentState.getViewMode() == IDEMainPanel.DEBUG_VIEW
				&& selection.get(0).getType() == NodeType.AGENT_PROCESS);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void execute(IDENode selectedNode, ActionEvent ae)
			throws GOALException {
		switch (selectedNode.getType()) { // must be process node
		case AGENT_PROCESS: // only in this case do something
			developmentEnvironment.getMainPanel().getDebugPanel()
			.showIntrospectorPanel(selectedNode.toString()); // toString
			// gives
			// agent's
			// name.
			// FIXME
			// make
			// that
			// explicit
			break;
		default: // MAS file and environment cannot be introspected
			new Warning(
					Resources.get(WarningStrings.FAILED_INTROSPECT_NOT_LOCAL));
		}
	}
}
