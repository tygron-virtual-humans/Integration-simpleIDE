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

package nl.tudelft.goal.SimpleIDE.files;

import java.io.File;

import nl.tudelft.goal.SimpleIDE.NodeType;

/**
 * {@link FileNode} representing an agent file.
 */
public class GOALNode extends FileNode {

	/** Auto-generated serial version UID */
	private static final long serialVersionUID = 1070680457615449383L;

	/**
	 * Creates a new GOAL-file node.
	 *
	 * @param goalFile
	 *            The GOAL-file the new node represents.
	 */
	public GOALNode(File goalFile) {
		super(NodeType.GOALFILE, goalFile);
		this.allowsChildren = true;
	}

}
