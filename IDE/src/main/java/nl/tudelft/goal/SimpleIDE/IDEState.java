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

import java.awt.Component;
import java.util.List;

/**
 * An abstract representation of the IDE state as far as relevant for actions.
 * It contains the selected files in the files panel, the selected mode (edit or
 * run mode), the selected component (editor of a file, etc), selected
 * introspector etc.
 *
 * The IDEstate is needed to easen the interaction with the actions, that do not
 * want to depend on the entire IDE
 *
 * @author W.Pasman 14jun2011
 */
public interface IDEState {

	/**
	 * Get the base component of the IDE, as center point for dialogs etc.
	 *
	 * @return {@link Component} that can be used to center a dialog on.
	 *
	 */
	Component getRootComponent();

	/**
	 * get the currently selected nodes in files panel/process panel. It seems
	 * we should change this into getSelectedFiles () and
	 * getSelectedProcesses(); but we tried this before and ran into problems
	 * therefore I postponed this. I can't remember where we put the info on
	 * what the problems were...
	 *
	 *
	 */
	List<? extends IDENode> getSelectedNodes();

	// public void setSelectedNodes(List<? extends IDENode> selection);

	/**
	 * returns 0 for edit mode, 1 for debug mode.
	 */
	int getViewMode();

	/**
	 * check if there currently is a MAS running
	 *
	 * @return true if there currently is a MAS running
	 */
	boolean isRuntimeEnvironmentAvailable();

}
