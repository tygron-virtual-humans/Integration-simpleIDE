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

import javax.swing.JTextArea;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import org.gjt.sp.jedit.textarea.TextArea;

/**
 * Displays the last row of text in the lower end of the IDE.
 * 
 * It is an Observer and hence should be subscribed with an Observable. The
 * observable should update us with text messages that are then shown in the
 * bar.
 * 
 * @author W.Pasman 8apr09
 * @modified KH: now implements CaretListener
 */
public class StatusBar extends JTextArea implements CaretListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6374876827890477698L;

	/**
	 * DOC
	 */
	public StatusBar() {
		setEditable(false);
	}

	/**
	 * DOC
	 */
	public void caretUpdate(CaretEvent evt) {
		// Check org.gjt.sp.jedit.gui.statusbar.java line 306ev
		TextArea textArea = ((TextArea) evt.getSource());
		int caretPosition = textArea.getCaretPosition();
		int currLine = textArea.getCaretLine();

		// FIXME there must be a better way of fixing this...
		// the problem is that this method can sometimes
		// be called as a result of a text area scroll
		// event, in which case the caret position has
		// not been updated yet.
		if (currLine >= textArea.getBuffer().getLineCount()) {
			return; // TODO hopefully another caret update will come?
		}

		int start = textArea.getLineStartOffset(currLine);
		int dot = caretPosition - start;

		// see above
		if (dot < 0) {
			return;
		}

		String status = (currLine + 1) + "," + (dot + 1);
		setText(status);
	}
}
