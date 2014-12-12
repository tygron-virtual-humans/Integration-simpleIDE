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

import java.util.EventListener;
import java.util.EventObject;

/**
 * <p>
 * GUI layout change events are passed through callbacks of SplitPositionChange
 * </p>
 * <p>
 * PropertyChangeListener cannot be used directly for the following reason: In
 * some cases, e.g. with the DatabasePanel, we want to route splitpos events
 * from multiple DatabasePanels to a single listener. However that listener
 * needs the split position which requires a call to
 * jsplitpane.getDividerLocation(). Overriding each database panel
 * propertyChangeListener would be quite some code. This LayoutChangeListener
 * provides the needed info straight away.
 * </p>
 * <p>
 * The listener is called LayoutChangeListener as in the future other layout
 * change events may be routed through this listener.
 * </p>
 * <p>
 * The panels may be made listeners as well, and pass resize information in the
 * other direction as well.
 * </p>
 * <p>
 * See http://www.jroller.com/santhosh/date/20050617
 * </p>
 *
 * @author W.Pasman 27mar09
 */
public interface LayoutChangeListener extends EventListener {

	/**
	 * This function is called when the split position of a splitpanel changes.
	 * The listener should override this function and handle appropriately.
	 *
	 * @param newsplitposition
	 *            is the new split position
	 * @param e
	 *            is the original event object
	 */
	void splitPositionChange(int newsplitposition, EventObject e);

}