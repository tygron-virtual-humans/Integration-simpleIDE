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

import goal.preferences.Preferences;
import goal.tools.errorhandling.exceptions.GOALException;

import java.awt.event.ActionEvent;

import nl.tudelft.goal.SimpleIDE.IDENode;

/**
 * Clear all preferences.
 *
 * @author W.Pasman 29jun2011
 */
@SuppressWarnings("serial")
public class ClearPreferencesAction extends GOALAction {

	/**
	 *
	 */
	private static final long serialVersionUID = 6198831936674012540L;

	@Override
	public void stateChangeEvent() {
		setActionEnabled(true);
	}

	@Override
	protected void execute(IDENode selectedNode, ActionEvent ae)
			throws GOALException {
		Preferences.resetToDefaultPreferences();
	}
}