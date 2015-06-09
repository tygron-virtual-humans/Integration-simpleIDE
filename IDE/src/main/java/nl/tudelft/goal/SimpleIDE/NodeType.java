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

/**
 * Represents the type of nodes that can occur in a file or process tree.
 */
public enum NodeType {
	/** File type */
	EMOFILE,
	/** File type */
	GOALFILE,
	/** File type */
	MASFILE,
	/** File type */
	MODFILE,
	/** File type */
	PLFILE,
	/** File type */
	TXTFILE,
	/**
	 * File type, used to put goal files without parent mas under in the
	 * FilePanel
	 */
	NULLFILE,
	/** File or process type */
	ROOT,
	/** Process type */
	MAS_PROCESS,
	/** Process type */
	AGENT_PROCESS,
	/** Process type */
	REMOTE_AGENT_PROCESS,
	/** Process type */
	ENVIRONMENT_PROCESS,
	/** remote env process */
	REMOTE_ENVIRONMENT_PROCESS;
}
