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

import goal.tools.debugger.BreakPoint;
import goal.tools.errorhandling.exceptions.GOALCommandCancelledException;
import goal.tools.errorhandling.exceptions.GOALException;

import java.awt.Frame;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.gjt.sp.jedit.textarea.TextArea;

/**
 * This is the interface for the editor. This interface allows us to support
 * multiple editors and to separate GOAL from the editor implementation.
 *
 * @author W.Pasman 25jun08
 * @modified KH removed observable1
 * @modified W.Pasman 19apr2012 #2108 conditional breakpoints
 */
public abstract class TextEditorInterface extends JPanel {
	/**
	 *
	 */
	private static final long serialVersionUID = 9043138222605026480L;
	private TextArea textArea;
	private String filename;

	/**
	 * Counter to generate new "Untitled-X' filenames.
	 */
	private static int serial = 1;

	/**
	 * DOC it is allowed to give null as filename, in which case a new filename
	 * is generated and an empty window is supposed to be opened. We do not
	 * provide a TextEditorInterface() constructor, to remind you that we really
	 * expect the filename to start with.
	 */
	public TextEditorInterface(String filename) {
		if (filename == null) {
			this.filename = "Untitled-" + serial++;
		} else {
			this.filename = filename;
		}
	}

	/**
	 * Returns the text area.
	 */
	public TextArea getTextArea() {
		return this.textArea;
	}

	/**
	 * Sets the text area.
	 */
	public void setTextArea(TextArea textArea) {
		this.textArea = textArea;
	}

	/**
	 * Returns the file label DOC ...
	 */
	public String getFilename() {
		return this.filename;
	}

	/**
	 * @return A {@link File} with {@link #getFilename()} as path.
	 */
	public File getFile() {
		return new File(getFilename());
	}

	/**
	 * Sets the file label DOC ...
	 */
	public void setFileName(String name) {
		this.filename = name;
	}

	// ******************* IO Handling ********************/
	/**
	 * DOC Reads file and returns content as a string. CHECK: we dropped all
	 * save functionality from jEdit: autosave, special character support, etc.
	 * The save routines in jEdit are extensive and intricately linked with
	 * View, Buffer (which is NOT the JEditBuffer) etc. However all it takes to
	 * do a basic load and save is a few lines of code as below.
	 *
	 * @throws DOC
	 *             exceptions handled by calling method to be able to clean up
	 *             e.g. editor window
	 */
	public static String readFile(String filename) throws IOException {
		String filecontents = "";
		String line;

		File thefile = new File(filename);
		// if the file does not exist, ask if it should be created.
		if (!thefile.exists()) {
			int result = JOptionPane
					.showConfirmDialog(
							Frame.getFrames()[0],
							"The selected file ("
									+ thefile.getName()
									+ ") does not exist.\n"
									+ "Do you want to create it? The path to the new file will be:\n"
									+ thefile.getPath(), "No such file.",
									JOptionPane.YES_NO_OPTION);
			switch (result) {
			case JOptionPane.YES_OPTION: {
				boolean createResult = thefile.createNewFile();
				if (!createResult) {
					throw new IOException("Unable to create a new file "
							+ filename + ".");
				}
				break;
			}
			default: // anything other than 'yes' is 'no'
				throw new FileNotFoundException();
			}
		}

		// FileReader always assumes default encoding is OK!
		BufferedReader input = new BufferedReader(new FileReader(filename));

		/*
		 * readLine returns: (1) the content of a line WITHOUT the newline, (2)
		 * an empty string if two newlines appear in a row, and (3) null for the
		 * END of the stream.
		 */
		while ((line = input.readLine()) != null) {
			filecontents = filecontents + line + "\n";
		}
		input.close();

		return filecontents;
	}

	/**
	 * Writes a string to a file.
	 *
	 * @param filename
	 *            is the name of the file to be written
	 * @param contents
	 *            is the string contents to be written
	 * @throws IOException
	 *             if error occurs.
	 */
	public static void writeFile(String filename, String contents)
			throws IOException {
		FileWriter fstream = new FileWriter(filename);
		BufferedWriter out = new BufferedWriter(fstream);
		out.write(contents);
		out.close();
	}

	/**
	 * Reloads the current file from the file system. This may be necessary in
	 * some cases, for example
	 * <ul>
	 * <li>user edited content of this editor
	 * <li>someone replaced the file in the file system.
	 * <p>
	 *
	 * @throws IOException
	 */
	public abstract void reload() throws IOException;

	/**
	 * show dialog allowing user to pick filename and saves current text in
	 * editor
	 *
	 */
	public abstract void save() throws IOException;

	/**
	 * Saves file under new label and adopts the new filename for future
	 * references. IDE switches the panel's label to the new label
	 */
	public abstract void saveAs(String filename) throws IOException;

	/**
	 * close the editor and the files that it is manipulating. Note, this does
	 * not close tabs that were holding the editor panel.
	 *
	 * @throws GOALCommandCancelledException
	 *             when user cancels the close.
	 * @throws GOALException
	 *             if something else goes wrong, e.g. there is a problem in the
	 *             dialog with the user during the closedown.
	 */
	public abstract void close() throws GOALException;

	// *************** editor functionality *****************/
	/**
	 * Returns whether one of the open editor panels contains content that has
	 * been changed but not has been saved yet.
	 *
	 * @return true if file content has been edited, but not yet saved.
	 */
	public abstract boolean isDirty();

	/**
	 * Enables or disables the ability of the user to edit the contents of this
	 * text editor. Breakpoints should still be editable.
	 *
	 * @param editable
	 *            If the text area should be editable.
	 */
	public abstract void setEditable(boolean editable);

	/**
	 * undo the last action
	 */
	public abstract void undo();

	/**
	 * redo last undo-ed action
	 */
	public abstract void redo();

	/**
	 * move selected text into system clipboard.
	 */
	public abstract void cut();

	/**
	 * copy selected text into system clipboard.
	 */
	public abstract void copy();

	/**
	 * paste the clipboard into the text
	 */
	public abstract void paste();

	/**
	 * show search/replace dialog and execute search/replace
	 */
	public abstract void searchReplace();

	/**
	 * repeat last find method
	 */
	public abstract void findNext();

	/**
	 * move cursor to given line number.
	 */
	public abstract void goToLine(int linenr);

	/**
	 * execute auto-complete command using current cursor position etc.
	 */
	public abstract void autoComplete();

	/**
	 * print the document. Includes all dialogs that may be needed.
	 */
	public abstract void print();

	/**
	 * launch dialog with user about the page setup of his document for printing
	 */
	public abstract void pageSetup();

	/**
	 * Get the preference panel of the text editor
	 *
	 * @return a JPanel with a GUI for the Text Editor.
	 */
	public abstract JPanel getEditorPreferencePanel();

	/**
	 * Get all the current breakpoints.
	 *
	 * @return all the enabled breakpoints.
	 */
	public abstract Set<BreakPoint> getBreakpoints();

	/**
	 * set breakpoint.
	 *
	 * @param breakpoint
	 *            to be set.
	 * @throws ArrayIndexOutOfBoundsException
	 *             when line does not exist.
	 */
	public abstract void setBreakpoint(BreakPoint breakpoint)
			throws ArrayIndexOutOfBoundsException;

	/**
	 * removes breakpoint.
	 *
	 * @param breakpoint
	 *            is breakpoint to be removed.
	 * @throws ArrayIndexOutOfBoundsException
	 *             when line does not exist.
	 */
	public abstract void removeBreakpoint(BreakPoint breakpoint)
			throws ArrayIndexOutOfBoundsException;

	/**
	 * comments out the selected text area. We prepend all lines in the selected
	 * range with the lineComment property (see the mode files)
	 */
	public abstract void comment();

	/**
	 * removes line comments from all lines in the current selection. Lines that
	 * have no comment mark at the start are ignored.
	 */
	public abstract void uncomment();
}
