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

import java.util.List;

import javax.swing.JFrame;
import javax.swing.event.CaretListener;

/**
 * IDEFunctionality provides access to the various functions regarding the user
 * interface.
 * 
 * 
 * Several "external" (subpanel) programs call on the IDE, For instance, the IDE
 * has to open a file when you click on it in the FilesPanel. Also a lot of
 * functionality can be reached in multiple ways, eg via button, double clicking
 * and menu
 * 
 * This interface lists all PUBLIC methods that need to be available in classes
 * that are part of the IDE. Most functionality in fact is hidden inside the
 * executeCommand. See {@link UserCmd}
 */
public interface IDEfunctionality {

	/**
	 * Returns all the selected node in the file or process panel. If no node
	 * has been selected, the root node is returned as single element of the
	 * list.
	 * 
	 * @return the selected file or process nodes, or the root node if nothing
	 *         has been selected.
	 */
	List<? extends IDENode> getSelectedNodes();

	/**
	 * @return the status bar. See StatusBar.java
	 */
	CaretListener getStatusBar();

	/**
	 * Get a reference to the main panel. Both used to execute actions that need
	 * the panels' functionality, and to center dialogs
	 * 
	 * @return the root component of the main window.
	 */
	IDEMainPanel getMainPanel();

	/**
	 * get the frame of the top level window
	 */
	JFrame getFrame();

}