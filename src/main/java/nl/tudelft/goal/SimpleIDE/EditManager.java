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

import goal.core.mas.MASProgram;
import goal.core.program.actions.ActionCombo;
import goal.parser.IParsedObject;
import goal.tools.BreakpointManager;
import goal.tools.PlatformManager;
import goal.tools.debugger.BreakPoint;
import goal.tools.debugger.BreakPoint.Type;
import goal.tools.errorhandling.Resources;
import goal.tools.errorhandling.Warning;
import goal.tools.errorhandling.WarningStrings;
import goal.tools.errorhandling.exceptions.GOALBug;
import goal.tools.errorhandling.exceptions.GOALCommandCancelledException;
import goal.tools.errorhandling.exceptions.GOALException;
import goal.tools.errorhandling.exceptions.GOALUserError;
import goal.tools.logging.InfoLog;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import nl.tudelft.goal.SimpleIDE.CloseTabbedPane.CloseTabbedPane;
import nl.tudelft.goal.SimpleIDE.CloseTabbedPane.TabCloseListener;
import nl.tudelft.goal.SimpleIDE.actions.CloseEditorAction;

/**
 * Provides the main edit area of the IDE, containing tabbed panes with active
 * editors.
 * <p>
 * This is a singleton as there can be only one editorManager in the system.
 * With multiple editorManagers it would be unclear what actions like open would
 * do.
 * <p>
 * FIXME I think we should remove the "extends JPanel" from EditManager. Maybe
 * there should be a getPanel() function giving that panel.
 * 
 * @author W.Pasman 2008
 * @modified 27jun2011 to get singleton pattern.
 * @modified W.Pasman 19apr2012 #2108 conditional breakpoints
 */
@SuppressWarnings("serial")
public class EditManager extends JPanel {

	private final CloseTabbedPane tabbedPane;
	private static IDEfunctionality myIDE;
	private boolean textEditorsAreEditable = true;

	private static EditManager theEditPanel = null;

	/**
	 * this must be called before you can get the singleton.
	 */
	public static void init(IDEfunctionality theIDE) {
		myIDE = theIDE;
	}

	/**
	 * creates the singleton. Get it with getInstance().
	 * 
	 * @param theIDE
	 */
	private EditManager() {
		setLayout(new BorderLayout());

		tabbedPane = new CloseTabbedPane();
		tabbedPane.setCloseListener(new TabCloseListener() {
			@Override
			public void closeOperation(AWTEvent e, int overTabIndex) {
				try {
					String filename = ((TextEditorInterface) tabbedPane
							.getComponentAt(overTabIndex)).getFilename();
					ActionFactory.getAction(CloseEditorAction.class).Execute(
							null, new ActionEvent(this, 1, filename));
				} catch (Exception ex) {
					new Warning(Resources.get(WarningStrings.FAILED_TAB_CLOSE),
							ex);
				}
			}
		});
		add(tabbedPane, BorderLayout.CENTER);
	}

	/**
	 * get an instance of the EditPanel singleton. You must call {@link #init}
	 * before calling this.
	 * 
	 * @return instance of EditPanel singleton.
	 * @throws GOALBug
	 *             when init was not called properly.
	 */
	public static EditManager getInstance() {
		if (myIDE == null) {
			throw new GOALBug(
					"EditPanel.getInstance requires init() to be called first"); //$NON-NLS-1$
		}
		if (theEditPanel == null) {
			theEditPanel = new EditManager();
		}
		return theEditPanel;
	}

	/**
	 * Returns the currently selected text editor. throws if there is no
	 * currently selected editor.
	 * 
	 * @return the component corresponding to the selected tab
	 * @throws GOALUserError
	 *             if there is no currently selected editor
	 */
	public TextEditorInterface getActiveEditor() throws GOALUserError {
		Component editor = tabbedPane.getSelectedComponent();
		if (editor == null) {
			throw new GOALUserError("there is no open editor"); //$NON-NLS-1$
		}
		return (TextEditorInterface) editor;
	}

	/**
	 * Check if there is any file being edited right now
	 * 
	 * @return true if there is file under edit right now.
	 */
	public boolean hasActiveEditor() {
		return tabbedPane.getSelectedComponent() != null;
	}

	/**
	 * DOC
	 * 
	 * @return TextEditorInterface for given node.
	 * @throws GOALUserError
	 *             if file is not being edited.
	 */
	public TextEditorInterface getEditor(File file) throws GOALUserError {
		for (int i = 0; i < tabbedPane.getTabCount(); i++) {
			TextEditorInterface editor = ((TextEditorInterface) tabbedPane
					.getComponentAt(i));

			if (tabbedPane.getTitleAt(i).equals(file.getName())) {
				return editor;
			}
		}
		throw new GOALUserError("the file " + file + " is not being edited"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * check if there is open editor for given node.
	 * 
	 * @param node
	 * @return true if there is editor, else false.
	 */
	public boolean isOpenEditor(File node) {
		for (int i = 0; i < tabbedPane.getTabCount(); i++) {
			if (tabbedPane.getTitleAt(i).equals(node.getName())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Save file open in this editor.
	 * 
	 * @throws GOALUserError
	 *             DOC
	 */
	public File save() throws GOALUserError {
		File savedFile = getActiveEditor().getFile();
		try {
			getActiveEditor().save();
		} catch (IOException e) {
			// CHECK is it a user error?
			throw new GOALUserError("Save failed", e); //$NON-NLS-1$
		}

		new InfoLog("File %s saved.", savedFile.getName()); //$NON-NLS-1$

		return savedFile;
	}

	/**
	 * save ALL files in the edit panel. errors are fatal, we immediately throw
	 * when problem occurs so that user regains control (assuming IDE handles it
	 * gracely)
	 * 
	 * @throws IOException
	 *             if save fails for some reason.
	 */
	public void saveAll() throws GOALException {
		try {
			for (int i = 0; i < tabbedPane.getTabCount(); i++) {
				((TextEditorInterface) tabbedPane.getComponentAt(i)).save();
			}
			new InfoLog("All files saved."); //$NON-NLS-1$
		} catch (IOException e) {
			throw new GOALUserError("saveALL failed", e); //$NON-NLS-1$
		}
	}

	/**
	 * Returns whether one of the open editor panels contains content that has
	 * been changed but not has been saved yet.
	 * 
	 * @return true if file content has been edited, but not yet saved.
	 */
	public boolean isDirty() {
		boolean dirty = false;

		for (int i = 0; i < tabbedPane.getTabCount(); i++) {
			dirty = dirty
					| ((TextEditorInterface) tabbedPane.getComponentAt(i))
							.isDirty();
		}

		return dirty;
	}

	/**
	 * Sets editability of the text areas of the text editors.
	 * 
	 * @param editable
	 *            If the text areas should be editable or not.
	 */
	public void setEditable(boolean editable) {
		this.textEditorsAreEditable = editable;
		for (int i = 0; i < tabbedPane.getTabCount(); i++) {
			((TextEditorInterface) tabbedPane.getComponentAt(i))
					.setEditable(editable);
		}
	}

	/**
	 * DOC will do nothing if given node not open in edit panel
	 * 
	 * @param node
	 */
	public void close(File node) throws GOALException {
		if (!isOpenEditor(node)) {
			return;
		}
		close(getEditor(node));
	}

	/**
	 * more distant interfaces (CloseEditorAction) can pass only strings.
	 * 
	 * @param filename
	 * @throws GOALException
	 */
	public void close(String filename) throws GOALException {
		close(getEditorPane(filename));
	}

	/**
	 * close the given editor This will close the files and maybe the editor but
	 * not the tab.
	 * 
	 * @param editor
	 * @throws GOALCommandCancelledException
	 *             if user cancels the close.
	 */
	public void close(TextEditorInterface editor) throws GOALException {
		editor.close();
		tabbedPane.remove(editor);
	}

	/**
	 * close the currently active editor
	 * 
	 * @throws GOALCommandCancelledException
	 *             if user cancels the close.
	 */
	public void close() throws GOALException {
		close(getActiveEditor());
	}

	/**
	 * close all edit panels.
	 * 
	 * @throws GOALCommandCancelledException
	 *             if user cancels the close.
	 */
	public void closeAll() throws GOALException {
		while (tabbedPane.getTabCount() > 0) {
			TextEditorInterface editor = ((TextEditorInterface) tabbedPane
					.getComponentAt(0));
			tabbedPane.setSelectedComponent(editor);
			close(editor);
		}
	}

	/**
	 * print the active document.
	 */
	public void print() throws GOALUserError {
		getActiveEditor().print();
	}

	/**
	 * 
	 */
	public void pageSetup() throws GOALUserError {
		getActiveEditor().pageSetup();
	}

	/**
	 * Opens a new tab in the editor, with last part of given filename.
	 * 
	 * @param filename
	 *            full path to file (should not be null), with '/' as
	 *            separators.
	 */
	public void editFile(File theFile) {
		// check if file with same label is already being edited
		TextEditorInterface editor = getEditorPane(theFile.getAbsolutePath());
		if (editor != null) { // select existing editor for viewing
			tabbedPane.setSelectedComponent(editor);
			return;
		}

		// file not yet open in editor; open new editor
		try {
			if (!theFile.canWrite()) {
				/** read only, issue a warning. #1485 */
				int selection = JOptionPane.showConfirmDialog(this, "The file " //$NON-NLS-1$
						+ theFile + " is read-only. Proceed?", "Proceed Edit?", //$NON-NLS-1$ //$NON-NLS-2$
						JOptionPane.OK_CANCEL_OPTION);
				if (selection == JOptionPane.CANCEL_OPTION) {
					return;
				}
			}
			TextEditorInterface newEditor = new JEditTextEditor(
					theFile.getAbsolutePath());
			tabbedPane.add(theFile.getName(), newEditor);
			tabbedPane.setSelectedComponent(newEditor);
			// connect the status bar to the text area of the editor to display
			// cursor position
			newEditor.getTextArea().addCaretListener(myIDE.getStatusBar());
			// make sure the editability of the new text editor is properly set
			newEditor.setEditable(this.textEditorsAreEditable);
		} catch (FileNotFoundException e) {
			new Warning(String.format(
					Resources.get(WarningStrings.FAILED_FILE_FIND),
					theFile.getAbsolutePath()), e);
		} catch (IOException e) {
			new Warning(String.format(
					Resources.get(WarningStrings.FAILED_FILE_OPEN),
					theFile.getAbsolutePath()), e);
		}
	}

	/**
	 * get the editor pane that contains an editor for the given file.
	 * 
	 * @return TextEditorInterface holding editor for given file, or null if no
	 *         such editor
	 */
	public TextEditorInterface getEditorPane(String filename) {
		// check if already being edited.
		for (int i = 0; i < tabbedPane.getTabCount(); i++) {
			TextEditorInterface c = (TextEditorInterface) (tabbedPane
					.getComponentAt(i));
			if (filename.equals(c.getFilename())) {
				return c;
			}
		}
		return null;
	}

	/**
	 * Get list with all active editors
	 * 
	 * @return list with all active editors.
	 */
	public List<TextEditorInterface> getEditors() {
		List<TextEditorInterface> editors = new ArrayList<TextEditorInterface>();
		// tabbedPane.getComponents[] returns more than just the tabedpane
		// components...
		for (int i = 0; i < tabbedPane.getTabCount(); i++) {
			editors.add((TextEditorInterface) (tabbedPane.getComponentAt(i)));
		}
		return editors;
	}

	/**
	 * DOC
	 */
	public void undo() throws GOALUserError {
		getActiveEditor().undo();
	}

	/**
	 * DOC
	 */
	public void redo() throws GOALUserError {
		getActiveEditor().redo();
	}

	/**
	 * DOC
	 * 
	 * @see goal.tools.SimpleIDE.TextEditorInterface#cut()
	 */
	public void cut() throws GOALUserError {
		getActiveEditor().cut();
	}

	/**
	 * DOC
	 * 
	 * @see goal.tools.SimpleIDE.TextEditorInterface#copy()
	 */
	public void copy() throws GOALUserError {
		getActiveEditor().copy();
	}

	/**
	 * @see goal.tools.SimpleIDE.TextEditorInterface#paste()
	 */
	public void paste() throws GOALUserError {
		getActiveEditor().paste();
	}

	/**
	 * @see goal.tools.SimpleIDE.TextEditorInterface#comment()
	 */
	public void comment() throws GOALUserError {
		getActiveEditor().comment();
	}

	/**
	 * @see goal.tools.SimpleIDE.TextEditorInterface#uncomment()
	 */
	public void uncomment() throws GOALUserError {
		getActiveEditor().uncomment();
	}

	/**
	 * pop up the search/replace dialog.
	 * 
	 * @see goal.tools.SimpleIDE.TextEditorInterface#searchReplace()
	 */
	public void searchReplace() throws GOALUserError {
		getActiveEditor().searchReplace();
	}

	/**
	 * repeat last find action
	 * 
	 * @see goal.tools.SimpleIDE.TextEditorInterface#findNext()
	 */
	public void findNext() throws GOALUserError {
		getActiveEditor().findNext();
	}

	/**
	 * aks user which line to go to and jump to that line.
	 * 
	 * @see goal.tools.SimpleIDE.TextEditorInterface#goToLine(int) see also
	 *      org.gjt.sp.jedit.GUIUtilities.input and
	 *      org.gjt.sp.jedit.textarea.JEditTextArea.
	 */
	public void goToLine() throws GOALUserError {
		String line = JOptionPane.showInputDialog(this, "Line", "Go To Line", //$NON-NLS-1$ //$NON-NLS-2$
				JOptionPane.QUESTION_MESSAGE);
		if (line == null) {
			return;
		}
		int lineNumber = Integer.parseInt(line) - 1;
		getActiveEditor().goToLine(lineNumber);
	}

	/**
	 * DOC
	 * 
	 * @see goal.tools.SimpleIDE.TextEditorInterface#autoComplete()
	 */
	public void autoComplete() throws GOALUserError {
		getActiveEditor().autoComplete();
	}

	/**
	 * Update the breakpoints for a given goal file. Updating involves checking
	 * where the breakpoints can really be placed, and moving them there.
	 */
	public void updateBreakpoints(File goalfile) {
		// FIXME CHECK can we move this to edit panel?
		final PlatformManager platform = PlatformManager.getCurrent();
		for (MASProgram reg : platform.getMASProgramsThatUseFile(goalfile)) {
			// first get the files used by the agent file
			Set<File> childFiles = null;
			childFiles = platform.getImportedFiles(goalfile);
			// the list isn't shared, so we can add other files. add the
			// agent file such that all files to be updated are in the list
			childFiles.add(goalfile);

			// update the breakpoints for all related files
			for (File file : childFiles) {
				updateRelatedBreakpoints(reg, file);
			}
		}
	}

	/**
	 * update breakpoints in related file. This will first copy the breakpoints
	 * to the MAS registry, which will find out the real line numbers for each
	 * breakpoint. Then the real line numbers are used to fix the breakpoint
	 * positions in the editor.
	 * 
	 * @param reg
	 *            is file registry
	 * @param file
	 *            is related file that may have breakpoints.
	 */
	private void updateRelatedBreakpoints(MASProgram reg, File file) {
		Set<BreakPoint> breakpoints = null;
		TextEditorInterface editor = null;
		try {
			editor = EditManager.getInstance().getEditor(file);
		} catch (GOALUserError err) {
			// editor is not open -> no breakpoints for the file
			editor = null;
		}
		if (editor == null) {
			breakpoints = new HashSet<BreakPoint>();
		} else {
			breakpoints = editor.getBreakpoints();
		}
		
		final BreakpointManager breaks = PlatformManager.getCurrent().getBreakpointManager();
		// set the breakpoints in the registry. this sets them to
		// the proper locations.
		breaks.setBreakpoints(file, breakpoints);

		// get the actual breakpoints back
		Set<IParsedObject> realBreakpoints = breaks.getBreakpoints(file);

		// don't bother updating the breakpoints if there aren't any
		if (breakpoints.isEmpty() || realBreakpoints == null) {
			return;
		}
		// also don't bother updating nonexistant editors
		if (editor == null) {
			return;
		}

		// set the actual breakpoints in the editor
		// first remove all old ones
		for (BreakPoint oldBP : breakpoints) {
			editor.removeBreakpoint(oldBP);
		}
		// then add all new ones, but only if there are any
		for (IParsedObject newBP : realBreakpoints) {
			// make sure to convert between 1-based and 0-based indices
			// HACK. We need to keep the type intact!!
			editor.setBreakpoint(new BreakPoint(file, newBP.getSource()
					.getLineNumber() - 1,
					newBP instanceof ActionCombo ? Type.CONDITIONAL
							: Type.ALWAYS));
		}
		// make sure to visually update the breakpoints as well
		editor.repaint();
	}

}
