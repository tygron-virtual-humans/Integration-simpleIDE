package org.gjt.sp.jedit.buffer;

import java.awt.Color;

import org.gjt.sp.jedit.Marker;

/**
 * 
 * Defines a breakpoint setting.
 * 
 * @author wouter 31mar2011
 * 
 */
public class Breakpoint extends Marker {

	/**
	 * The type of this breakpoint
	 */
	Type type;

	public enum Type {
		/**
		 * always stop when breakpoint reached
		 */
		ALWAYS(Color.RED),
		/**
		 * stop only when the condition holds. Used in GOAL for
		 * if-condition-then rules
		 */
		CONDITIONAL(Color.ORANGE);

		private Color color;

		private Type(Color color) {
			this.color = color;
		}

		/**
		 * get the color for this type of marker
		 * 
		 * @return color for this type of marker.
		 */
		public Color getColor() {
			return color;
		}
	}

	/**
	 * @param buffer
	 * @param shortcut
	 *            is a tag, used for menus and tooltips
	 * @param position
	 *            is the character index in the file.
	 */
	Breakpoint(JEditBuffer buffer, int position, Type type) {
		// '\0' means no shortcut available.
		super(buffer, '\0', position);
		this.type = type;
		createPosition();
	}

	public String toString() {
		return "Breakpoint[" + getPosition() + "]";
	}

	/**
	 * get the type
	 * 
	 * @return the type of this breakpoint
	 */
	public Type getType() {
		return type;
	}

	/**
	 * get the color of this breakpoint.
	 * 
	 * @return Color of this breakpoint
	 */
	public Color getColor() {
		return type.getColor();
	}

	/**
	 * Get the line number on which this breakpoint is located
	 * 
	 * @return line number
	 */
	public int getLine() {
		return buffer.getLineOfOffset(getPosition());
	}
}