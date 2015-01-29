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

import javax.swing.Icon;
import javax.swing.tree.TreeNode;

public interface IDENode extends TreeNode {

	/**
	 * get the Node's name. FIXME on several places this is used for other
	 * things like filename and agent name. We may have to reconsider that.
	 *
	 * @return the node's name.
	 */
	String getNodeName();

	/**
	 * get the type for this node.
	 *
	 * @return node type
	 */
	NodeType getType();

	/**
	 * Get icon showing the node and state of node.
	 *
	 * @return
	 */
	Icon getIcon();

	/**
	 * Returns whether node label should be print in BOLD or not.
	 *
	 * @return true if label is to be printed in BOLD, otherwise false.
	 */
	boolean getBoldPrinting();

	/**
	 * Sets value for BOLD printing of node label.
	 *
	 * @param value
	 *            true if label should be printed BOLD, otherwise false.
	 */
	void setBoldPrinting(boolean value);

}
