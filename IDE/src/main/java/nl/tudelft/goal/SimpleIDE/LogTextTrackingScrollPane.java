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

import goal.preferences.LoggingPreferences;
import goal.tools.logging.GOALBufferedHandler;
import goal.tools.logging.GOALLogRecord;
import goal.tools.logging.GOALLogger;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import nl.tudelft.goal.SimpleIDE.preferences.IntrospectorPreferences;

/**
 * Extends the {@link TextTrackingScrollPane} to
 * <ul>
 * <li>buffer the text until it is "safe to do so" (when a call to "flush" is
 * made, as far as I understand).</li>
 * <li>
 * show logged text (text streamed through a {@link GOALLogger}</li>
 *
 * @author N.Kraayenbrink<br>
 *         Text can now only be added using loggers, of which the output is only
 *         shown once {@link #flush} is called. Use {@link #subscribeTo} to
 *         subscribe to loggers. Subclasses may still use the old
 *         {@link #append} method, but by doing so the output may appear before
 *         log messages that were made earlier.
 * @author W.Pasman 23sept10 created this class as extension of the
 *         {@link TextTrackingScrollPane}
 */
public class LogTextTrackingScrollPane extends TextTrackingScrollPane {
	/**
	 *
	 */
	private static final long serialVersionUID = 932378235353669776L;
	private final GOALBufferedHandler myHandler;

	public LogTextTrackingScrollPane(String initialText) {
		this(initialText, IntrospectorPreferences.getMaxLines());
	}

	public LogTextTrackingScrollPane(String initialText, int maxLines) {
		super(initialText, maxLines);
		// buffer any incoming log messages, and display them only
		// when nothing is running
		// buffer only maxlines messages. Usually there is one line per message,
		// rarely less
		this.myHandler = new GOALBufferedHandler(new TextPaneHandler(),
				maxLines);
	}

	/**
	 * Subscribes this {@link LogTextTrackingScrollPane} to a {@link GOALLogger}
	 * by adding the {@link TextPaneHandler} to the given {@link GOALLogger}
	 * handler list.
	 *
	 * @param loggers
	 *            The {@link GOALLogger}s to subscribe this
	 *            {@link LogTextTrackingScrollPane} to.
	 */
	public void subscribeTo(GOALLogger... loggers) {
		for (GOALLogger logger : loggers) {
			logger.addHandler(this.myHandler);
		}
	}

	/**
	 * Unsubscribes this {@link LogTextTrackingScrollPane} from a
	 * {@link GOALLogger} by removing the {@link TextPaneHandler} from the given
	 * {@link GOALLogger}'s handler list.
	 *
	 * @param loggers
	 *            The {@link GOALLogger}s to unsubscribe from.
	 */
	public void unsubscribeFrom(GOALLogger... loggers) {
		for (GOALLogger logger : loggers) {
			logger.removeHandler(this.myHandler);
			logger.removeLogToFileHandler();
		}
	}

	/**
	 * Pushes and flushes any buffered incoming messages
	 */
	public void flush() {
		this.myHandler.flush();
	}

	/**
	 * Sets the minimum level of the displayed log messages
	 */
	public void setLevel(Level newLevel) {
		this.myHandler.setLevel(newLevel);
	}

	/**
	 * Sets the threshold level of record pushing. If a record is logged with a
	 * higher or equal level than the given one, the buffer is cleared and
	 * buffered records are published.
	 */
	public void setPushLevel(Level newLevel) {
		this.myHandler.setPushLevel(newLevel);
	}

	/**
	 * Simple log handler for a {@link LogTextTrackingScrollPane}. Displays
	 * published records using {@link LogTextTrackingScrollPane#append}, using a
	 * {@link Formatter} prescribed by the given {@link GOALLogRecord}. If a
	 * {@link LogRecord} is given that is not a {@link GOALLogRecord}, its raw
	 * message is displayed.
	 *
	 * @author N.Kraayenbrink
	 *
	 */
	private class TextPaneHandler extends Handler {

		/**
		 * A buffer for the lines to be appended, so that it is not necessary to
		 * append all lines separately.
		 */
		private StringBuffer lineBuffer;

		public TextPaneHandler() {
			this.lineBuffer = new StringBuffer();
		}

		@Override
		public void close() {
			// nothing to close
		}

		@Override
		public void flush() {
			if (this.lineBuffer.length() == 0) {
				return;
			}
			append(this.lineBuffer.toString());
			this.lineBuffer = new StringBuffer();
		}

		@Override
		public void publish(LogRecord record) {
			String text = "";
			final SimpleDateFormat timeFormatter = new SimpleDateFormat(
					"H:mm:ss:SSS");
			Formatter f = null;
			if (!(record instanceof GOALLogRecord)) {
				if (getFormatter() != null) {
					f = getFormatter();
				}
			} else {
				f = ((GOALLogRecord) record).getFormatter();
			}
			if (LoggingPreferences.getShowTime()) {
				Date date = new Date(record.getMillis());
				text += timeFormatter.format(date) + " ";
			}
			if (f != null) {
				text += f.getHead(this) + f.format(record) + f.getTail(this);
			} else {
				text += record.getMessage();
			}
			append(text);
			if (record.getLevel().intValue() > getLevel().intValue()) {
				flush();
			}
		}
	}
}
