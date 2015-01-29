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

/*
 *
 * @author W.Pasman --- hacked version from BufferPrintable from jEdit sources.
 *
 * BufferPrintable.java - Printable implementation
 * :tabSize=8:indentSize=8:noTabs=false:
 * :folding=explicit:collapseFolds=1:
 *
 * Copyright (C) 2001, 2003 Slava Pestov
 * Portions copyright (C) 2002 Thomas Dilts
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

//package org.gjt.sp.jedit.print;
package nl.tudelft.goal.SimpleIDE;

//{{{ Imports
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.geom.Rectangle2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterAbortException;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.SwingUtilities;
import javax.swing.text.TabExpander;

import org.gjt.sp.jedit.ViewSubstitute;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.buffer.JEditBuffer;
import org.gjt.sp.jedit.syntax.Chunk;
import org.gjt.sp.jedit.syntax.DisplayTokenHandler;
import org.gjt.sp.jedit.syntax.SyntaxStyle;
import org.gjt.sp.jedit.syntax.Token;
import org.gjt.sp.util.Log;

//}}}

/**
 * @version $Id: BufferPrintable.java 12504 2008-04-22 23:12:43Z ezust $
 */
class ModifiedBufferPrintable implements Printable {
	// {{{ BufferPrintable constructor
	ModifiedBufferPrintable(PrinterJob job, Object format, ViewSubstitute view,
			JEditBuffer buffer, Font font, boolean header, boolean footer,
			boolean lineNumbers, boolean color) {
		this.job = job;
		this.format = format;
		this.view = view;
		this.buffer = buffer;
		this.font = font;
		this.header = header;
		this.footer = footer;
		this.lineNumbers = lineNumbers;

		this.styles = org.gjt.sp.util.SyntaxUtilities.loadStyles(
				jEdit.getProperty("print.font"),
				jEdit.getIntegerProperty("print.fontsize", 10), color);
		this.styles[Token.NULL] = new SyntaxStyle(textColor, null, font);

		// Change any white text to black
		for (int i = 0; i < this.styles.length; i++) {
			SyntaxStyle s = this.styles[i];
			if (s.getForegroundColor().equals(Color.WHITE)
					&& s.getBackgroundColor() == null) {
				this.styles[i] = new SyntaxStyle(Color.BLACK,
						this.styles[i].getBackgroundColor(),
						this.styles[i].getFont());
			}
		}

		this.lineList = new ArrayList<Chunk>();

		this.tokenHandler = new DisplayTokenHandler();
	} // }}}

	/**
	 * DOC
	 */
	public void print() {
		try {
			// buffer.readLock();

			if (this.format == null) {
				this.job.print();
			} else {
				Method method = PrinterJob.class
						.getMethod(
								"print",
								new Class[] { Class
										.forName("javax.print.attribute.PrintRequestAttributeSet") });
				method.invoke(this.job, new Object[] { this.format });
			}
		} catch (PrinterAbortException ae) {
			Log.log(Log.DEBUG, this, ae);
		} catch (Exception e) {
			Log.log(Log.ERROR, this, e);
			final String[] args = { e.toString() };
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					// GUIUtilities.error(view,"print-error",args);
					System.out.println("print error:" + args);
				}
			});
		}
		// finally
		// {
		// buffer.readUnlock();
		// }
	} // }}}

	// {{{ print() method
	@Override
	public int print(Graphics _gfx, PageFormat pageFormat, int pageIndex)
			throws PrinterException {
		// we keep the first non-null frc we get, since sometimes
		// we get invalid ones on subsequent pages on Windows
		if (this.frc == null) {
			this.frc = ((Graphics2D) _gfx).getFontRenderContext();
			Log.log(Log.DEBUG, this, "Font render context is " + this.frc);
		}

		Log.log(Log.DEBUG, this, "Asked to print page " + pageIndex);
		Log.log(Log.DEBUG, this, "Current page is " + this.currentPage);

		if (pageIndex > this.currentPage) {
			for (int i = this.currentPage; i < pageIndex; i++) {
				Log.log(Log.DEBUG, this, "Current physical line is now "
						+ this.currentPageStart);
				this.currentPhysicalLine = this.currentPageStart;
				printPage(_gfx, pageFormat, i, true);
			}

			this.currentPage = pageIndex - 1;
			Log.log(Log.DEBUG, this, "Current page is now " + this.currentPage);
		}

		if (pageIndex == this.currentPage + 1) {
			if (this.end) {
				Log.log(Log.DEBUG, this, "The end");
				return NO_SUCH_PAGE;
			}

			this.currentPageStart = this.currentPhysicalLine;
			Log.log(Log.DEBUG, this, "#2 - Current physical line is now "
					+ this.currentPageStart);
			this.currentPage = pageIndex;
			Log.log(Log.DEBUG, this, "#2 - Current page is now "
					+ this.currentPage);
		} else if (pageIndex == this.currentPage) {
			this.currentPhysicalLine = this.currentPageStart;
			Log.log(Log.DEBUG, this, "#3 - Current physical line is now "
					+ this.currentPageStart);
		}

		printPage(_gfx, pageFormat, pageIndex, true);

		return PAGE_EXISTS;
	} // }}}

	// {{{ Private members

	// {{{ Static variables
	private static Color headerColor = Color.lightGray;
	private static Color headerTextColor = Color.black;
	private static Color footerColor = Color.lightGray;
	private static Color footerTextColor = Color.black;
	private static Color lineNumberColor = Color.gray;
	private static Color textColor = Color.black;
	// }}}

	// {{{ Instance variables
	private final PrinterJob job;
	private final Object format;

	private final ViewSubstitute view;
	private final JEditBuffer buffer;
	private final Font font;
	private final SyntaxStyle[] styles;
	private final boolean header;
	private final boolean footer;
	private final boolean lineNumbers;

	private int currentPage;
	private int currentPageStart;
	private int currentPhysicalLine;
	private boolean end;

	private LineMetrics lm;
	private final List<Chunk> lineList;

	private FontRenderContext frc;

	private final DisplayTokenHandler tokenHandler;

	// }}}

	// {{{ printPage() method
	private void printPage(Graphics grafix, PageFormat pageFormat,
			int pageIndex, boolean actuallyPaint) {
		Log.log(Log.DEBUG, this, "printPage(" + pageIndex + ',' + actuallyPaint
				+ ')');
		Graphics2D gfx = (Graphics2D) grafix;
		gfx.setFont(this.font);

		double pageX = pageFormat.getImageableX();
		double pageY = pageFormat.getImageableY();
		double pageWidth = pageFormat.getImageableWidth();
		double pageHeight = pageFormat.getImageableHeight();

		Log.log(Log.DEBUG, this, "#1 - Page dimensions: " + pageWidth + 'x'
				+ pageHeight);

		if (this.header) {
			double headerHeight = paintHeader(gfx, pageX, pageY, pageWidth,
					actuallyPaint);
			pageY += headerHeight;
			pageHeight -= headerHeight;
		}

		if (this.footer) {
			double footerHeight = paintFooter(gfx, pageX, pageY, pageWidth,
					pageHeight, pageIndex, actuallyPaint);
			pageHeight -= footerHeight;
		}

		boolean glyphVector = jEdit.getBooleanProperty("print.glyphVector");
		double lineNumberWidth;

		// {{{ determine line number width
		if (this.lineNumbers) {
			// the +1's ensure that 99 gets 3 digits, 103 gets 4 digits,
			// and so on.
			int lineNumberDigits = (int) Math.ceil(Math.log(this.buffer
					.getLineCount() + 1) / Math.log(10)) + 1;

			// now that we know how many chars there are, get the width.
			char[] chars = new char[lineNumberDigits];
			for (int i = 0; i < chars.length; i++) {
				chars[i] = ' ';
			}
			lineNumberWidth = this.font.getStringBounds(chars, 0,
					lineNumberDigits, this.frc).getWidth();
		} else {
			lineNumberWidth = 0.0;
		}
		// }}}

		Log.log(Log.DEBUG, this, "#2 - Page dimensions: "
				+ (pageWidth - lineNumberWidth) + 'x' + pageHeight);

		// {{{ calculate tab size
		int tabSize = jEdit.getIntegerProperty("print.tabSize", 8);
		char[] chars = new char[tabSize];
		for (int i = 0; i < chars.length; i++) {
			chars[i] = ' ';
		}
		double tabWidth = this.font
				.getStringBounds(chars, 0, tabSize, this.frc).getWidth();
		PrintTabExpander e = new PrintTabExpander(tabWidth);
		// }}}

		this.lm = this.font.getLineMetrics("gGyYX", this.frc);
		Log.log(Log.DEBUG, this, "Line height is " + this.lm.getHeight());

		double y = 0.0;
		print_loop: for (;;) {
			if (this.currentPhysicalLine == this.buffer.getLineCount()) {
				Log.log(Log.DEBUG, this, "Finished buffer");
				this.end = true;
				break print_loop;
			}
			if (!jEdit.getBooleanProperty("print.folds", true)
					&& !this.view.getTextArea().getDisplayManager()
					.isLineVisible(this.currentPhysicalLine)) {

				Log.log(Log.DEBUG, this, "Skipping invisible line");
				this.currentPhysicalLine++;
				continue;
			}

			this.lineList.clear();

			this.tokenHandler.init(this.styles, this.frc, e, this.lineList,
					(float) (pageWidth - lineNumberWidth));

			this.buffer.markTokens(this.currentPhysicalLine, this.tokenHandler);
			if (this.lineList.isEmpty()) {
				this.lineList.add(null);
			}

			if (y + (this.lm.getHeight() * this.lineList.size()) >= pageHeight) {
				Log.log(Log.DEBUG, this, "Finished page before line "
						+ this.currentPhysicalLine);
				break print_loop;
			}

			if (this.lineNumbers && actuallyPaint) {
				gfx.setFont(this.font);
				gfx.setColor(lineNumberColor);
				gfx.drawString(String.valueOf(this.currentPhysicalLine + 1),
						(float) pageX,
						(float) (pageY + y + this.lm.getHeight()));
			}

			for (int i = 0; i < this.lineList.size(); i++) {
				y += this.lm.getHeight();
				Chunk chunks = this.lineList.get(i);
				if (chunks != null && actuallyPaint) {
					Chunk.paintChunkBackgrounds(chunks, gfx,
							(float) (pageX + lineNumberWidth),
							(float) (pageY + y));
					Chunk.paintChunkList(chunks, gfx,
							(float) (pageX + lineNumberWidth),
							(float) (pageY + y), glyphVector);
				}
			}

			this.currentPhysicalLine++;
		}
	} // }}}

	// {{{ paintHeader() method
	private double paintHeader(Graphics2D gfx, double pageX, double pageY,
			double pageWidth, boolean actuallyPaint) {
		String headerText = jEdit.getProperty("print.headerText",
				new String[] { this.buffer.getName() });
		// CHECK why use a new frc while there already is a global frc field?
		FontRenderContext frc1 = gfx.getFontRenderContext();
		this.lm = this.font.getLineMetrics(headerText, frc1);

		Rectangle2D bounds = this.font.getStringBounds(headerText, frc1);
		Rectangle2D headerBounds = new Rectangle2D.Double(pageX, pageY,
				pageWidth, bounds.getHeight());

		if (actuallyPaint) {
			gfx.setColor(headerColor);
			gfx.fill(headerBounds);
			gfx.setColor(headerTextColor);
			gfx.drawString(headerText,
					(float) (pageX + (pageWidth - bounds.getWidth()) / 2),
					(float) (pageY + this.lm.getAscent()));
		}

		return headerBounds.getHeight();
	}

	// }}}

	// {{{ paintFooter() method
	private double paintFooter(Graphics2D gfx, double pageX, double pageY,
			double pageWidth, double pageHeight, int pageIndex,
			boolean actuallyPaint) {
		String footerText = jEdit.getProperty("print.footerText", new Object[] {
				new Date(), Integer.valueOf(pageIndex + 1) });
		// CHECK why use a new frc while there already is a global frc field?
		FontRenderContext frc1 = gfx.getFontRenderContext();
		this.lm = this.font.getLineMetrics(footerText, frc1);

		Rectangle2D bounds = this.font.getStringBounds(footerText, frc1);
		Rectangle2D footerBounds = new Rectangle2D.Double(pageX, pageY
				+ pageHeight - bounds.getHeight(), pageWidth,
				bounds.getHeight());

		if (actuallyPaint) {
			gfx.setColor(footerColor);
			gfx.fill(footerBounds);
			gfx.setColor(footerTextColor);
			gfx.drawString(footerText, (float) (pageX + (pageWidth - bounds
					.getWidth()) / 2),
					(float) (pageY + pageHeight - bounds.getHeight() + this.lm
							.getAscent()));
		}

		return footerBounds.getHeight();
	} // }}}

	// }}}

	// {{{ PrintTabExpander class
	static class PrintTabExpander implements TabExpander {
		private final double tabWidth;

		// {{{ PrintTabExpander constructor
		PrintTabExpander(double tabWidth) {
			this.tabWidth = tabWidth;
		} // }}}

		// {{{ nextTabStop() method
		@Override
		public float nextTabStop(float x, int tabOffset) {
			int ntabs = (int) ((x + 1) / this.tabWidth);
			return (float) ((ntabs + 1) * this.tabWidth);
		} // }}}
	} // }}}
}
