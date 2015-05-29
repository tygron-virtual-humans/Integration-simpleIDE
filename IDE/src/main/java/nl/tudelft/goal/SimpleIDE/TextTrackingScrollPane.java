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

import goal.tools.errorhandling.Resources;
import goal.tools.errorhandling.Warning;
import goal.tools.errorhandling.WarningStrings;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;

import nl.tudelft.goal.SimpleIDE.preferences.IntrospectorPreferences;

/**
 * Creates a text area that supports scrolling. This may be very CPU intensive.
 * Therefore, if you need to print a lot of text, we recommend to use a
 * LogTextTrackingScrollPane.
 *
 * * appends text to the text area. We scroll the pane such that
 * <ul>
 * <li>The caret/scroll bar keeps at bottom, if it was already at bottom.</li>
 * <li>The caret scrolls up with the text, if it is somewhere halfway</li>
 * <li>The caret keeps at the top, if it is at the top</li>
 * </ul>
 *
 * @modified W.Pasman 27apr10, supported mouse wheel, added scrolling with mouse
 *           wheel even while text is scrolling. You can now hold the scrollbar
 *           also halfway to see text scroll by at that place. You can now drop
 *           the scrollbar halfway and the scrollbar will clamp to that piece of
 *           text until it is at the top.
 * @author W.Pasman 23sept10, actually recovered from the
 *         LogTextTrackingScrollPane that had been heavily extended in the code
 *         (instead of being extended via class extension)
 */
@SuppressWarnings("serial")
public class TextTrackingScrollPane extends JScrollPane implements
		ActionListener, MouseWheelListener, AdjustmentListener, MarkedReadable {

	/**
	 *
	 */
	private static final long serialVersionUID = -865628312651851555L;
	private final JTextArea textarea;
	private final JScrollBar vscrollbar;
	private final JPopupMenu popup;
	private Integer wheelrotation = 0; // mouse wheel rotation. Updated via
	// mousewheellistener.
	private boolean atEnd = true; // true if we lock view at the end.

	private int maxNrOfLines;

	private boolean isUnread;

	/**
	 * minimum number of lines in the pane.
	 */
	private static final int MINIMUM_LINES = 10;

	/*
	 * above here we snap to 1
	 */
	private static final double END_SNAP_POSITION = 0.97;

	/**
	 * Sets up scrolling pane with text with a maximum number of lines of text
	 * that are stored. Retrieves the maximum number of lines from the user
	 * preference settings stored by the Introspector preference panel.
	 *
	 * @param initialText
	 *            initial text added to the text area when the panel is created.
	 */
	public TextTrackingScrollPane(String initialText) {
		this(initialText, IntrospectorPreferences.getMaxLines());
	}

	/**
	 * Sets up scrolling pane with text with a maximum number of lines of text
	 * that are stored. Uses second parameter to set the maximum number of lines
	 * stored by the panel.
	 *
	 * @param initialText
	 *            initial text added to the text area when the panel is created.
	 * @param maxlines
	 *            maximum number of lines stored by the area. Oldest lines are
	 *            removed when lines are appended beyond this limit. Note:
	 *            Should be set to {@link #MINIMUM_LINES} or more lines.
	 */
	public TextTrackingScrollPane(String initialText, int maxlines) {
		if (maxlines <= MINIMUM_LINES) {
			throw new IllegalArgumentException(
					"max number of lines must be at least " + MINIMUM_LINES); //$NON-NLS-1$
		}
		this.isUnread = false;

		this.maxNrOfLines = maxlines;
		this.textarea = new JTextArea(initialText);
		this.textarea.setEditable(false);
		// textarea.getCaret().setVisible(true);
		this.setViewportView(this.textarea);
		this.vscrollbar = getVerticalScrollBar();

		// set up pop up menu
		this.popup = new JPopupMenu();
		JMenuItem menuItem = new JMenuItem("Clear"); //$NON-NLS-1$
		menuItem.setActionCommand(UserCmd.CLEARTXT.toString());
		menuItem.addActionListener(this);
		this.popup.add(menuItem);

		this.vscrollbar.addAdjustmentListener(this);

		this.textarea.addMouseListener(new MouseAdapter() {
			/**
			 * Handles right-mouse clicks to show the pop up menu.
			 *
			 * @param event
			 */
			@Override
			public void mousePressed(MouseEvent event) {
				if (event.isPopupTrigger()) {
					TextTrackingScrollPane.this.popup.show(
							event.getComponent(), event.getX(), event.getY());
				}
			}

			@Override
			public void mouseReleased(MouseEvent event) {
				if (event.isPopupTrigger()) {
					TextTrackingScrollPane.this.popup.show(
							event.getComponent(), event.getX(), event.getY());
				}
			}
		});

		this.textarea.addMouseWheelListener(this);

	}

	/** handle scroll wheel events */
	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		synchronized (this.wheelrotation) {
			if (e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL) {
				this.wheelrotation = this.wheelrotation + e.getUnitsToScroll();
			} else {
				this.wheelrotation += e.getScrollAmount();
			}
		}
		updateCaret();
	}

	@Override
	public void adjustmentValueChanged(AdjustmentEvent e) {
		updateCaret();
	}

	/**
	 * Sets maximum number of lines stored by panel. Additional lines added on
	 * top of this maximum means that lines printed earlier are removed again.
	 *
	 * @param max
	 *            maximum number of lines to be stored by panel.
	 */
	public void setMaxLines(int max) {
		this.maxNrOfLines = max;
	}

	/**
	 * @return Maximum number of lines visible on this pane
	 */
	protected int getMaxLines() {
		return this.maxNrOfLines;
	}

	@Override
	public boolean isUnread() {
		return this.isUnread;
	}

	@Override
	public void markRead() {
		this.isUnread = false;
	}

	/**
	 * Marks this panel as unread. Will not mark this as unread when:
	 * <ul>
	 * <li>The parent component is a {@link JTabbedPane}, and this is the
	 * currently visible tab component.</li>
	 * <li>The parent component is a {@link ConsoleTextPanel}, whose parent is a
	 * {@link JTabbedPane}, and the {@link ConsoleTextPanel} is the currently
	 * visible tab component.</li>
	 * </ul>
	 */
	@Override
	public void markUnread() {
		JComponent parent = (JComponent) this.getParent();
		JComponent thisComp = this;
		// this panel is usually directly below the tabbed panel
		// however the text panel for console output has the ConsoleTextPanel
		// in-between.
		if (parent instanceof ConsoleTextPanel) {
			thisComp = parent;
			parent = (JComponent) parent.getParent();
		}
		if (parent instanceof JTabbedPane
				&& ((JTabbedPane) parent).getSelectedComponent() == thisComp) {
			// do not mark as unread if this panel is currently visible
			return;
		}
		this.isUnread = true;
		// make sure to repaint the parent, such that any unread-indicator
		// will be drawn
		if (parent != null) {
			// repaint is thread safe according to
			// http://da2i.univ-lille1.fr/doc/tutorial-java/uiswing/painting/concepts.html
			parent.repaint();
		}
	}

	/**
	 * Sets text in the text area. Thread safe.
	 *
	 * @param text
	 *            string to be displayed in text area.
	 */
	public void setText(final String text) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				TextTrackingScrollPane.this.textarea.setText(text);
			}
		});
	}

	/**
	 * Appends text to text in text area in panel. Erases lines at beginning
	 * whenever line count exceeds maximum number of lines allowed.
	 *
	 * @param text
	 *            text to be appended to text area in panel.
	 */

	/**
	 * Appends text to the text area. Thread safe - it uses
	 * {@link SwingUtilities#invokeLater(Runnable)}. The scroll bar of the pane
	 * is controlled as follows:
	 * <ul>
	 * <li>The caret/scroll bar stays at the bottom, if it was already at
	 * bottom.</li>
	 * <li>The caret scrolls up with the text, if it is somewhere halfway</li>
	 * <li>The caret keeps at the top, if it is at the top</li>
	 * </ul>
	 *
	 * For efficiency, it's better to join strings first and then call append 1
	 * time only instead of multiple times.
	 */
	protected void append(final String text) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				append1(text);
			}
		});
	}

	/**
	 * Internal function doing the real append. Does Swing calls and therefore
	 * should always be called through #app.
	 *
	 * @param text
	 */
	private void append1(String text) {
		// preferably, we'd not append everything if the text has too many
		// lines, but the speedup is probably small (if at all noticeable).
		this.textarea.append(text);

		// check if there are too many lines visible
		int superfluousLineCount = this.textarea.getLineCount()
				- this.maxNrOfLines;

		if (superfluousLineCount > 0) {
			// get the index of the first character on the first
			// line that should be visible
			int firstVisibleLineStart;
			try {
				firstVisibleLineStart = this.textarea
						.getLineStartOffset(superfluousLineCount);
			} catch (BadLocationException e) {
				// we can't throw RuntimeException because inside Swing
				new Warning(String.format(
						Resources.get(WarningStrings.FAILED_GET_LINESTART),
						"" + superfluousLineCount)); //$NON-NLS-1$
				return;
			}
			// replace everything before the obtained index with null,
			// thus removing everything before that index
			this.textarea.replaceRange(null, 0, firstVisibleLineStart);
		}

		updateCaret1();

		this.markUnread();
	}

	/**
	 * Update caret position and view/scrollbar location in the text panel. When
	 * user drags scrollbar, reposition caret. Set at_end when user drags to
	 * end. When not dragging, keep caret at end of text when at_end is set.
	 * when at_end is not set, scroll the caret up with the text. When user hits
	 * scroll wheel, scroll caret in wheel direction accordingly.
	 *
	 * Thread safe: we call {@link SwingUtilities#invokeLater(Runnable)}.
	 */
	private void updateCaret() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				updateCaret1();
			}
		});
	}

	/**
	 * Internal function for updating Caret. Must always be called from the
	 * Swing thread.
	 */
	private void updateCaret1() {
		// scrollbar position
		double relPos = ((double) this.vscrollbar.getValue() / (double) (this.vscrollbar
				.getMaximum() - this.vscrollbar.getVisibleAmount()));
		int maxLineNr = this.textarea.getLineCount() - 1;
		int caretLine = this.textarea.getDocument().getDefaultRootElement()
				.getElementIndex(this.textarea.getCaretPosition());

		/***** after here no cutting/pasting - relocating the caret *****/

		synchronized (this.wheelrotation) {
			if (this.wheelrotation != 0) {
				caretLine += this.wheelrotation;
				this.wheelrotation = 0;
				this.atEnd = false; // scroll wheel unlocks the caret.
			}
		}

		// check if user is dragging the scrollbar. If so, we manipulate the
		// caret position.
		if (this.vscrollbar.getValueIsAdjusting()) {
			if (relPos > END_SNAP_POSITION) {
				this.atEnd = true; // snap to end.
			} else {
				caretLine = (int) (relPos * maxLineNr);
				this.atEnd = false;
			}
		}

		if (caretLine >= maxLineNr) {
			this.atEnd = true;
		}

		if (this.atEnd) {
			caretLine = maxLineNr;
		}

		if (caretLine < 0) {
			caretLine = 0;
		}

		try {
			this.textarea.setCaretPosition(this.textarea
					.getLineStartOffset(caretLine));
		} catch (BadLocationException e) {
			// CHECK technically you indeed want to throw here,
			// but the exception just will end up in Swing
			// instead of propagating through our own code.
			// What is the proper way here?
			// throw new RuntimeException("BUG caret position failure", e);
			// catch silently, this is bad but see #1234. Happens very rare and
			// has no side effects.
		}
	}

	/**
	 * Handles events generated by selecting items in pop up menu. Clears text
	 * area if Clear has been selected.
	 *
	 * @param event
	 *            event generated by pop up menu.
	 */
	@Override
	public void actionPerformed(ActionEvent event) {
		if (event.getActionCommand().equals(UserCmd.CLEARTXT.toString())) {
			setText(""); //$NON-NLS-1$
		}
	}

}
