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

package nl.tudelft.goal.SimpleIDE.CloseTabbedPane;

import java.awt.AWTEvent;
import java.util.EventListener;

public interface TabCloseListener extends EventListener {
	/**
	 * This function is called when the user pressed the close button on the
	 * tab. Note that the user clicking on that button has no side effects
	 * except for this function being called. If you want the tab to close you
	 * need to attach code to this button that actually closes the tab.
	 *
	 * @param e
	 *            is the original AWT event of the user clicking the button
	 * @param overTabIndex
	 *            is the index of the tab in the CloseTabbedPane that the user
	 *            clicked on.
	 */
	void closeOperation(AWTEvent e, int overTabIndex);
}
