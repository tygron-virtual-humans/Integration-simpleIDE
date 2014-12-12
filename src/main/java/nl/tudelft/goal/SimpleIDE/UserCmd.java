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
 * Represents the commands that the IDE supports.
 * 
 * @author KH
 */
public enum UserCmd {
	// commands related to file menu
	NEW, OPEN, RELOAD(SelectionSupport.MULTIPLE), SAVE,
	/** SaveAs saves the file open in current editor with a new name. */
	SAVEAS, SAVEALL,
	/**
	 * edit files that are selected in files panel
	 */
	EDIT(SelectionSupport.MULTIPLE), GETPROGINFO, CLOSE,
	/** remove file from file panel */
	CLOSEANDREMOVE(SelectionSupport.MULTIPLE),
	/** delete files that are selected in files panel */
	DELETE(SelectionSupport.MULTIPLE),
	/**
	 * rename a file selected in the files panel.
	 */
	RENAME,
	/** print file currently being edited */
	PRINT,
	/** save file selected in file panel AND currently being edited. */
	SAVEFILE(SelectionSupport.MULTIPLE),
	/** Command to DOC */
	PAGESETUP,
	/** Command to quit IDE. */
	QUIT,
	// commands related to edit menu
	UNDO, REDO, CUT, COPY, PASTE, FIND, FINDNEXT, GOTOLINE, COMPLETEWORD, COMMENT, UNCOMMENT,
	// commands related to run menu
	RUN, PARK, STEP, PAUSE, BACKSTEP, KILL, RUNLOCAL, RUNJADE, RUNRMI, RESET, MEMSTATS,
	// commands related to debug menu
	DEBUG, CLOSEINTROSPECTOR, DEBUGLOG, EXPORTDB, MODELCHECKER,
	// commands related to sniffer menu
	SNIFF, STOPSNIFF, CLEARSNIFF, CLOSESNIFF,
	// command to switch between edit and debug view
	SWITCHPANEL,
	// commands related to help menu
	/** Command to show preference panel. */
	PREFERENCES,
	/** Command to move IDE panel to background. */
	TOBACK,
	/** Command to show 'About GOAL' panel. */
	ABOUT,
	// special commands
	/** Command to clear text area. */
	CLEARTXT;

	private SelectionSupport selectionSupport;

	/**
	 * get the selection support that this command supports.
	 * 
	 * @return
	 */
	public SelectionSupport getSelectionSupport() {
		return selectionSupport;
	}

	private UserCmd(SelectionSupport sel) {
		selectionSupport = SelectionSupport.MULTIPLE;
	}

	/** Default constructor gives SINGLE selection support */
	private UserCmd() {
		selectionSupport = SelectionSupport.SINGLE;
	}
}
