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

import javax.swing.JPopupMenu;

import nl.tudelft.goal.SimpleIDE.actions.DebugLogAction;
import nl.tudelft.goal.SimpleIDE.actions.IntrospectorAction;
import nl.tudelft.goal.SimpleIDE.actions.KillAction;
import nl.tudelft.goal.SimpleIDE.actions.PauseAction;
import nl.tudelft.goal.SimpleIDE.actions.RunAction;
import nl.tudelft.goal.SimpleIDE.actions.StepAction;

/**
 * Pop up menu attached to a tree.
 * 
 * @author KH
 * @modified W.Pasman 23jun2011 #1676 In fact this popup seems only working
 *           since I made these modifications, it used not to work.
 */
@SuppressWarnings("serial")
public class TreePopupMenu extends JPopupMenu {

	/**
	 * Creates a tree pop up menu for the process panel
	 * 
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public TreePopupMenu() throws IllegalAccessException,
			InstantiationException {
		add(ActionFactory.getAction(RunAction.class));
		add(ActionFactory.getAction(StepAction.class));
		add(ActionFactory.getAction(PauseAction.class));
		add(ActionFactory.getAction(KillAction.class));
		add(ActionFactory.getAction(IntrospectorAction.class));
		add(ActionFactory.getAction(DebugLogAction.class));
	}

}
