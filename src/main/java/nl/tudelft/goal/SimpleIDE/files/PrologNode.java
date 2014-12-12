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
 * {@link FileNode} representing an imported .pl file.
 */
public class PrologNode extends FileNode {

	/** Auto-generated serial version UID */
	private static final long serialVersionUID = 2502116947369784423L;

	/**
	 * Creates a new Prolog-file node.
	 * 
	 * @param modFile
	 *            The Modules-file the new node represents.
	 */
	public PrologNode(File modFile) {
		super(NodeType.PLFILE, modFile);
		this.allowsChildren = false;
	}
}
