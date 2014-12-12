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
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

import javax.swing.JTabbedPane;

import nl.tudelft.goal.SimpleIDE.MarkedReadable;

/**
 * A variant of a JTabbedPane with close buttons.
 *
 * @author W.Pasman 20jul2011
 *
 */
public class CloseTabbedPane extends JTabbedPane {

	/**
	 *
	 */
	private static final long serialVersionUID = -7359550356964131909L;
	private TabCloseListener closeListener = null;

	public CloseTabbedPane() {
	}

	/**
	 * Add a close listener. This will call back the listener when a tab is
	 * closed. Note, this is a pretty stupid interface, with only 1 listener
	 * allowed.
	 *
	 * @param l
	 *            is the {@link goal.tools.SimpleIDE.CloseListener}
	 */
	public void setCloseListener(TabCloseListener l) {
		this.closeListener = l;
	}

	@Override
	public Component add(String titel, Component component) {
		super.add(titel, component);
		setTabComponentAt(indexOfComponent(component), new ButtonTabComponent(
				this));
		return component;
	}

	/**
	 * The tab calls this when the user clicks the tab's close button.
	 *
	 * @param e
	 *            is event
	 * @param index
	 *            is index of that tab.
	 */
	protected void tabCloseClicked(AWTEvent e, int index) {
		if (this.closeListener != null) {
			this.closeListener.closeOperation(e, index);
		}
	}

	@Override
	public void paint(Graphics g) {
		for (int i = 0; i < getTabCount(); i++) {
			Component c = getComponentAt(i);

			if (c instanceof MarkedReadable) {
				if (((MarkedReadable) c).isUnread()) {
					setBackgroundAt(i, Color.YELLOW);

				} else {
					setBackgroundAt(i, Color.WHITE);
				}
			}
		}
		super.paint(g);
	}

}
