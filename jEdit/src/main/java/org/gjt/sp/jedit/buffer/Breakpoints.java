package org.gjt.sp.jedit.buffer;

import java.util.Vector;

import org.gjt.sp.jedit.Marker;

/**
 * This class records all breakpoints for a buffer. Under the hood we use the
 * Position objects because they are automatically updated by JEdit.
 * 
 * Most code was copied from Buffer.java and is dealing with selection ranges;
 * however it seems that part is not working as I would expect. Maybe I should
 * read the JEdit doc on how this is supposed to work...
 * 
 * @author W.Pasman 21mar2011
 * 
 */
public class Breakpoints {

	JEditBuffer editbuffer; // needed for line location of breakpoint.
	Vector<Breakpoint> breakpoints = new Vector<Breakpoint>();

	public Breakpoints(JEditBuffer buffer) {
		editbuffer = buffer;
	}

	// {{{ addMarker() method
	/**
	 * Adds a marker to this buffer. Copied from Buffer.addMarker.
	 * 
	 * @param pos
	 *            The position of the marker
	 * @param shortcut
	 *            The shortcut ('\0' if none)
	 * @since jEdit 3.2pre1
	 */
	public void addBreakpoint(int pos, Breakpoint.Type type) {
		Breakpoint markerN = new Breakpoint(editbuffer, pos, type);
		boolean added = false;

		// don't sort markers while buffer is being loaded
		if (!editbuffer.isLoading()) {

			markerN.createPosition();

			for (int i = 0; i < breakpoints.size(); i++) {
				Marker marker = breakpoints.get(i);

				if (marker.getPosition() == pos) {
					breakpoints.removeElementAt(i);
					i--;
				}
			}

			for (int i = 0; i < breakpoints.size(); i++) {
				Marker marker = breakpoints.get(i);
				if (marker.getPosition() > pos) {
					breakpoints.insertElementAt(markerN, i);
					added = true;
					break;
				}
			}
		}

		if (!added)
			breakpoints.addElement(markerN);
	}

	/**
	 * Removes all breakpoints at the specified line. Copied from
	 * Buffer.removeMarker. NOTE I find it weird that remove only removes caret
	 * line breakpoint, while add adds to all selected lines.
	 * 
	 * @param line
	 *            The line number
	 * @since jEdit 3.2pre2
	 */
	public void removeBreakpoint(int line) {
		for (int i = 0; i < breakpoints.size(); i++) {
			Marker marker = breakpoints.get(i);
			if (editbuffer.getLineOfOffset(marker.getPosition()) == line) {
				marker.removePosition();
				breakpoints.removeElementAt(i);
				i--;
			}
		}

	}

	/**
	 * get breakpoints. Do NOT use this to change the array!
	 * 
	 * @return the breakpoints array.
	 */
	public Vector<Breakpoint> getBreakpoints() {
		return breakpoints;
	}

	/**
	 * Returns the first breakpoint within the specified range. Copied from
	 * Buffer.getMarkerInRange.
	 * 
	 * @return the first breakpoint within the specified range. null if no such
	 *         marker.
	 * @param start
	 *            The start offset
	 * @param end
	 *            The end offset
	 */
	public Breakpoint getBreakpointInRange(int start, int end) {
		for (int i = 0; i < breakpoints.size(); i++) {
			Breakpoint breakpoint = breakpoints.get(i);
			int pos = breakpoint.getPosition();
			if (pos >= start && pos < end)
				return breakpoint;
		}

		return null;
	}

}