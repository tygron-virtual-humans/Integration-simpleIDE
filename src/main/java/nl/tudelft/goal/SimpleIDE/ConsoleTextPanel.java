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
import goal.tools.logging.Loggers;

import java.awt.BorderLayout;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.logging.Level;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import nl.tudelft.goal.SimpleIDE.preferences.IDEPreferences;

/**
 * <p>
 * Shows the output that normally goes to the console. That is, all
 * system.out.print calls as well as java error messages and log messages.
 * </p>
 * <p>
 * Note that printf() calls from C are not rerouted but still appear in the
 * "boot shell" window.
 * </p>
 * 
 * @author W.Pasman
 */
@SuppressWarnings("serial")
public final class ConsoleTextPanel extends JPanel implements MarkedReadable {
	private LogTextTrackingScrollPane consoleoutput = new LogTextTrackingScrollPane(
			"", IDEPreferences.getMaxLines());

	/**
	 * DOC
	 */
	public ConsoleTextPanel() {
		setLayout(new BorderLayout());
		add(consoleoutput, BorderLayout.CENTER);

		refreshOutputRedirection();
		// for now, subscribe to everything that used to go to System.out or
		// System.err
		this.consoleoutput.subscribeTo(Loggers.getAllLoggers());
		// _except_ for the parser info, that has its own tab
		this.consoleoutput.unsubscribeFrom(Loggers.getParserLogger());
		// display everything when it happens
		this.consoleoutput.setPushLevel(Level.ALL);
	}

	/**
	 * To be called at creation of this {@link ConsoleTextPanel}.
	 */
	private void refreshOutputRedirection() {
		if (LoggingPreferences.getShowLogsInConsole()) {
			return;
		}
		/**
		 * turn on redirection. re-route stdio and stderr make the PrintStream
		 * call flush() when a newline is printed
		 */
		PrintStream out = new PrintStream(new MyOutputStream(consoleoutput),
				true);
		System.setOut(out);
		System.setErr(out);

		/** else we already set the redirection. */
		// if (originalOutputChannel != null) {
		// System.setOut(originalOutputChannel);
		// originalOutputChannel = null;
		// }
	}

	@Override
	public void markRead() {
		this.consoleoutput.markRead();
	}

	@Override
	public void markUnread() {
		this.consoleoutput.markUnread();
	}

	@Override
	public boolean isUnread() {
		return this.consoleoutput.isUnread();
	}

}

/**
 * Handles streams coming from multiple processes. It buffers calls to SWING
 * until we received a full line. It still can happen that characters from
 * multiple processes are mixed, if they print simultaneously. We sometimes see
 * this happen if exceptions are printed... CHECK we may want to use this in
 * other stream printers too. If so, make separate file? CHECK: why buffer
 * lines? it may happen that a message is printed without line-end, which will
 * only appear once something else (with line-end) is printed.
 */
final class MyOutputStream extends OutputStream {
	private LogTextTrackingScrollPane consoleoutput;

	/**
	 * DOC
	 * 
	 * @param output
	 */
	public MyOutputStream(LogTextTrackingScrollPane output) {
		consoleoutput = output;
	}

	// maybe messy if two threads print parallel.
	private static StringBuffer linecache = new StringBuffer();

	@Override
	public void write(int b) throws IOException {
		linecache = linecache.append((char) b);
		// PrintStream form ConsoleTextPanel already flushes on newline
		// characters
	}

	@Override
	public void flush() throws IOException {
		// no need to flush nothing
		if (linecache.length() == 0) {
			return;
		}

		SwingUtilities.invokeLater(new Runnable() {
			private String toprint = linecache.toString();

			public void run() {
				consoleoutput.append(toprint);
			}
		});
		linecache = new StringBuffer();
	}
}
