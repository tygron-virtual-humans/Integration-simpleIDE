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

import goal.tools.debugger.SteppingDebugger.RunMode;
import goal.tools.errorhandling.exceptions.GOALBug;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

/**
 * Custom renderer, to add our custom status icons to process nodes.
 */
@SuppressWarnings("serial")
public class IDENodeRenderer extends DefaultTreeCellRenderer {

	/**
	 *
	 */
	private static final long serialVersionUID = -6941912036333576503L;

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean sel, boolean expanded, boolean leaf, int row,
			boolean hasFocus) {

		if (!(value instanceof IDENode)) {
			throw new GOALBug("Encountered node of unknown type." + value);
		}

		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf,
				row, hasFocus);
		setIcon(((IDENode) value).getIcon());
		if (value instanceof ProcessNode) {
			if (((IDENode) value).getBoldPrinting()
					&& (((ProcessNode) value).getProcessRunMode() != RunMode.RUNNING)) {
				setFont(getFont().deriveFont(Font.BOLD));
				setForeground(Color.blue);
			} else {
				setFont(getFont().deriveFont(Font.PLAIN));
				setForeground(Color.black);
			}
			if (((ProcessNode) value).getType() == NodeType.ENVIRONMENT_PROCESS) {
				setFont(getFont().deriveFont(Font.ITALIC));
			}
		}

		return this;
	}
}
