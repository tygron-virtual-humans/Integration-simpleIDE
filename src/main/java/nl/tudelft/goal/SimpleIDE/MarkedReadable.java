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
 * Interface for objects that can be marked as (un)read. Created for use in
 * {@link goal.tools.SimpleIDE.TextTrackingScrollPane TextTrackingScrollPane}.
 * 
 * @author N.Kraayenbrink
 */
public interface MarkedReadable {
	/**
	 * Marks this object as unread.
	 */
	void markUnread();

	/**
	 * Marks this object as read.
	 */
	void markRead();

	/**
	 * Checks if this object is read or not.
	 * 
	 * @return {@code true} iff this object is marked as unread.
	 */
	boolean isUnread();
}
