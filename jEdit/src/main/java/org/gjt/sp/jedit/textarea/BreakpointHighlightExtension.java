package org.gjt.sp.jedit.textarea;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.buffer.Breakpoint;
import org.gjt.sp.jedit.buffer.Breakpoints;

/**
 * This extension paints a red box on lines where we have enabled a breakpoint.
 * 
 * @author W.Pasman 31mar2011
 * 
 */
public class BreakpointHighlightExtension extends TextAreaExtension {

	private Color markerHighlightColor = Color.red;

	TextArea textArea;

	/**
	 * 
	 * @param ta
	 *            is the TextArea. The textArea is passed both for painting and
	 *            for getting the enabled breakpoints in the area.
	 */
	public BreakpointHighlightExtension(TextArea ta) {
		textArea = ta;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void paintValidLine(Graphics2D gfx, int screenLine,
			int physicalLine, int start, int end, int y) {
		Breakpoints breakpoints = textArea.getBuffer().getBreakpoints();
		Breakpoint bpt = breakpoints.getBreakpointInRange(start, end);
		if (bpt != null) {
			gfx.setColor(bpt.getColor());
			FontMetrics fm = textArea.getPainter().getFontMetrics();
			gfx.fillRect(0, y, textArea.getGutter().getWidth(), fm.getHeight());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getToolTipText(int x, int y) {
		int lineHeight = textArea.getPainter().getFontMetrics().getHeight();
		if (lineHeight == 0)
			return null;

		int line = y / lineHeight;
		int start = textArea.getScreenLineStartOffset(line);
		int end = textArea.getScreenLineEndOffset(line);
		if (start == -1 || end == -1)
			return null;

		Breakpoint marker = textArea.getBuffer().getBreakpoints()
				.getBreakpointInRange(start, end);
		if (marker != null) {
			char shortcut = marker.getShortcut();
			if (shortcut == '\0')
				return jEdit.getProperty("view.gutter.breakpoint.no-name");
			else {
				String[] args = { String.valueOf(shortcut) };
				return jEdit.getProperty("view.gutter.breakpoint", args);
			}
		}

		return null;
	}

}
