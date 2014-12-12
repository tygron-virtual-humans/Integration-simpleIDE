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

import javax.swing.ImageIcon;
import javax.swing.tree.DefaultMutableTreeNode;

import nl.tudelft.goal.SimpleIDE.IDENode;
import nl.tudelft.goal.SimpleIDE.IconFactory;
import nl.tudelft.goal.SimpleIDE.NodeType;

/**
 * Data structure for file node in file panel which stores info related to the
 * nodes in the tree, and contains associated icon pictures to indicate file
 * type in file panel.
 */
public class FileNode extends DefaultMutableTreeNode implements IDENode {

	/** auto-generated serial version UID */
	private static final long serialVersionUID = 4268086550762376317L;

	/**
	 * Full path to file.
	 */
	private File baseFile;
	/**
	 * The type of filenode
	 */
	private NodeType type;

	/**
	 * Creates a new {@link FileNode}.
	 * 
	 * @param type
	 *            The type of this {@link FileNode}.
	 * @param baseFile
	 *            The {@link File} this node represents. Should not be
	 *            <code>null</code>, unless {@link #getObserverName()} and/or
	 *            {@link #toString()} is overridden.
	 */
	public FileNode(NodeType type, File baseFile) {
		this.type = type;
		this.baseFile = baseFile;
	}

	/**
	 * Gets the {@link File} represented by this {@link FileNode}.
	 * 
	 * @return The {@link File} this {@link FileNode} represents. May be
	 *         <code>null</code> for the root and {@link NullNode}.
	 */
	public File getBaseFile() {
		return this.baseFile;
	}

	/**
	 * Returns name that is displayed with file node in file pane.
	 * 
	 * @return name displayed in file pane.
	 */
	public String getNodeName() {
		return toString();
	}

	/**
	 * DOC
	 * 
	 * @return
	 */
	public String getFilename() {
		return this.baseFile.getAbsolutePath();
	}

	/**
	 * Sets the {@link File} this {@link FileNode} refers to.
	 */
	public void setBaseFile(File newBaseFile) {
		this.baseFile = newBaseFile;
	}

	/**
	 * DOC
	 * 
	 * @return DOC
	 */
	public NodeType getType() {
		return type;
	}

	/**
	 * DOC
	 * 
	 * @return DOC
	 */
	public ImageIcon getIcon() {
		switch (type) {
		case ROOT:
			return IconFactory.RUN.getIcon();
		case GOALFILE:
			if (this.baseFile.exists()) {
				return IconFactory.GOAL_FILE.getIcon();
			} else {
				return IconFactory.NO_GOAL_FILE.getIcon();
			}
		case MASFILE:
		case MODFILE: // same 'M'-icon as mas file
			if (this.baseFile.exists()) {
				return IconFactory.MAS_FILE.getIcon();
			} else {
				return IconFactory.NO_MAS_FILE.getIcon();
			}
		case PLFILE:
			if (baseFile.exists()) {
				return IconFactory.PL_FILE.getIcon();
			} else {
				return IconFactory.NO_PL_FILE.getIcon();
			}
		case TXTFILE:
		case NULLFILE:
			return IconFactory.OTHER_FILE.getIcon();
		default:
			throw new RuntimeException(
					"Internal error, Unknown type of filenode: " + type);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean getBoldPrinting() {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setBoldPrinting(boolean value) {
		throw new RuntimeException(
				"[FileNode] File nodes should not be printed BOLD.");
	}

	@Override
	public String toString() {
		return this.baseFile == null ? "null" : this.baseFile.getName();
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof FileNode)) {
			return false;
		}
		FileNode that = (FileNode) other;
		if (this.getType() != that.getType()) {
			return false;
		}
		if (this.baseFile == null) {
			return that.baseFile == null;
		}
		return this.baseFile.equals(that.baseFile);
	}

	@Override
	public int hashCode() {
		if (this.baseFile == null) {
			return 0;
		}
		return this.baseFile.hashCode();
	}

}
