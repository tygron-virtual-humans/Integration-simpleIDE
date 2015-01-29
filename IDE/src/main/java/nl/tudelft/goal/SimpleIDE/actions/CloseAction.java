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

import goal.tools.errorhandling.exceptions.GOALException;
import goal.tools.errorhandling.exceptions.GOALUserError;

import java.awt.event.ActionEvent;

import nl.tudelft.goal.SimpleIDE.IDENode;

/**
 * Close the active tab in the main panel. This can be either a editor,
 * introspector, sniffer panel.
 *
 * @author W.Pasman 20jun2011
 */
@SuppressWarnings("serial")
public class CloseAction extends GOALAction {

	/**
	 *
	 */
	private static final long serialVersionUID = -2343421884298353039L;

	public CloseAction() {
		setShortcut('W');
	}

	@Override
	/**
	 * {@inheritDoc}
	 */
	public void stateChangeEvent() {
		boolean isTabOpenNow = true;
		try {
			developmentEnvironment.getMainPanel().getCurrentPanel();
		} catch (GOALUserError e) {
			isTabOpenNow = false;
		}
		setActionEnabled(isTabOpenNow);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void execute(IDENode selectedNode, ActionEvent ae)
			throws GOALException {
		try {
			developmentEnvironment.getMainPanel().close();
		} catch (Exception e) {
			// do not use InfoLog, but System.out in order to see the
			// message if the panel is closed.
			System.out.println("Encountered problem while closing IDE.");
			e.printStackTrace();
		}
	}
}
