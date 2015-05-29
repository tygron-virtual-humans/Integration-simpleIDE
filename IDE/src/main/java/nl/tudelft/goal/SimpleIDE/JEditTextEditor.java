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
import goal.tools.debugger.BreakPoint.Type;
import goal.tools.errorhandling.exceptions.GOALBug;
import goal.tools.errorhandling.exceptions.GOALCommandCancelledException;
import goal.tools.errorhandling.exceptions.GOALException;
import goal.tools.errorhandling.exceptions.GOALUserError;
import goal.util.Extension;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import nl.tudelft.goal.SimpleIDE.preferences.EditorPreferences;
import nl.tudelft.goal.SimpleIDE.prefgui.EditorPrefPanel;

import org.gjt.sp.jedit.Mode;
import org.gjt.sp.jedit.Registers;
import org.gjt.sp.jedit.ViewSubstitute;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.buffer.Breakpoint;
import org.gjt.sp.jedit.buffer.JEditBuffer;
import org.gjt.sp.jedit.guistandalone.CompleteWord;
import org.gjt.sp.jedit.options.TextAreaOptionPane;
import org.gjt.sp.jedit.searchstandalone.SearchAndReplace;
import org.gjt.sp.jedit.searchstandalone.SearchDialog;
import org.gjt.sp.jedit.syntax.ModeProvider;
import org.gjt.sp.jedit.syntax.SyntaxStyle;
import org.gjt.sp.jedit.textarea.AntiAlias;
import org.gjt.sp.jedit.textarea.StandaloneTextArea;

/**
 * DOC This is an adapter to meet jEdit pane with a TextEditorInterface. see
 * also org/gjt/sp/jedit/actions.xml in the jEdit code.
 *
 * This builds on a edited and recompiled version of jEdit v4.3.pre16
 * <p>
 * This class is final because it calls 'public' functions during construction,
 * to load the file, set the cursor etc. If these were overridden, the
 * constructor behaviour would become puzzling.
 *
 * @author W.Pasman
 * @modified W.Pasman 19apr2012 #2108 conditional breakpoints
 * @modified W.Pasman jan2015 #3238 breakpoints in GOAL now 1-based.
 */
@SuppressWarnings("serial")
public final class JEditTextEditor extends TextEditorInterface {

	/**
	 *
	 */
	private static final long serialVersionUID = -6002920516792609116L;
	private ViewSubstitute view;
	private static final String FILE_KEY = "file";
	private static final int UNDO_LIMIT = 100;

	/**
	 * @param filename
	 *            is the file to open an editor for.
	 */
	public JEditTextEditor(String filename) throws IOException {
		super(filename);

		this.view = new ViewSubstitute();

		jEdit.initSystemProperties();
		/*
		 * this creates stand-alone text area.
		 */
		setTextArea(StandaloneTextArea.createTextArea(this.view));
		this.view.setTextArea(getTextArea());

		Mode mode; // the mode of this file
		// TODO use the catalog to guess the file type.

		Extension ext = Extension.getFileExtension(filename);
		if (ext == null) {
			mode = new Mode("plaintext");
			mode.setProperty(FILE_KEY, "modes/plaintext.xml");
			ModeProvider.instance.addMode(mode);
		} else {
			switch (ext) {
			case MAS:
				mode = new Mode("mas");
				mode.setProperty(FILE_KEY, "modes/mas.xml");
				ModeProvider.instance.addMode(mode);
				break;
			case GOAL:
			case MODULES:
				Mode prologmode = new Mode("prolog");
				prologmode.setProperty(FILE_KEY, "modes/prolog.xml");
				ModeProvider.instance.addMode(prologmode);

				mode = new Mode("goal");
				mode.setProperty(FILE_KEY, "modes/goal.xml");
				ModeProvider.instance.addMode(mode);
				break;
			case PROLOG:
				mode = new Mode("prolog");
				mode.setProperty(FILE_KEY, "modes/prolog.xml");
				ModeProvider.instance.addMode(mode);
				break;
			default:
				new GOALBug("Unknown/unhandled extension " + ext);
				return;
			}
		}

		getTextArea().getBuffer().setUndoLimit(0); // HACK, see trac 630.
		// disable Undo of
		// load-file.
		getTextArea().getBuffer().insert(0, readFile(filename));
		getTextArea().getBuffer().setUndoLimit(UNDO_LIMIT); // trac 630. default
		// seems
		// 100.
		getTextArea().getBuffer().setDirty(false); // not dirty, we now match
		// the file.
		getTextArea().getBuffer().setMode(mode);
		goToLine(0); // otherwise we would get at the last line (TRAC 523), as
		// result of the insert.

		setLayout(new BorderLayout());
		add(getTextArea(), BorderLayout.CENTER);

		// setupObserver(text);
	}

	@Override
	public void save() throws IOException {
		saveAs(getFilename()); // use the initial filename.
	}

	@Override
	public void saveAs(String filename) throws IOException {
		JEditBuffer buffer = this.view.getBuffer();
		writeFile(filename, buffer.getText(0, buffer.getLength()));
		this.view.getBuffer().setDirty(false);
		setFileName(filename);
	}

	@Override
	public boolean isDirty() {
		return this.view.getBuffer().isDirty();
	}

	@Override
	public void setEditable(boolean editable) {
		this.view.getTextArea().setEnabled(editable);
	}

	@Override
	public void undo() {
		this.view.getBuffer().undo(this.view.getTextArea());
	}

	@Override
	public void redo() {
		this.view.getBuffer().redo(this.view.getTextArea());
	}

	@Override
	public void cut() {
		Registers.cut(this.view.getTextArea(), '$');
	}

	@Override
	public void copy() {
		Registers.copy(this.view.getTextArea(), '$');
	}

	@Override
	public void paste() {
		Registers.paste(this.view.getTextArea(), '$', false);
	}

	@Override
	public void searchReplace() {
		SearchDialog.showSearchDialog(this.view, this.view.getTextArea()
				.getSelectedText(), SearchDialog.CURRENT_BUFFER);
	}

	@Override
	public void findNext() {
		SearchAndReplace.find(this.view);
	}

	@Override
	public void goToLine(int lineNumber) {
		this.view.getTextArea().setCaretPosition(
				this.view.getBuffer().getLineStartOffset(lineNumber));
	}

	@Override
	public void autoComplete() {
		CompleteWord.completeWord(this.view);
	}

	@Override
	public void close() throws GOALException {
		if (this.view.getBuffer().isDirty()) {
			int choice = JOptionPane.showConfirmDialog(this,
					"Save changes before closing?", "Save File Before Close?",
					JOptionPane.YES_NO_CANCEL_OPTION);

			switch (choice) {
			case JOptionPane.YES_OPTION:
				try {
					save();
				} catch (IOException e) {
					throw new GOALUserError("save of file failed", e);
				}
				break;
			case JOptionPane.NO_OPTION:
				break;
			case JOptionPane.CANCEL_OPTION:
				throw new GOALCommandCancelledException(
						"Close cancelled by user");
			default:
				throw new GOALBug("Choice pane returned unknown choice: "
						+ choice);
			}
		}
	}

	@Override
	public void reload() throws IOException {

		// OK, confirmation received. reload.
		// No need to check time etc, just reload.
		this.view.getBuffer().insert(0, readFile(getFilename()));
		this.view.getBuffer().setDirty(false); // not dirty, we now match the
		// file.
		goToLine(0); // otherwise we would get at the last line (TRAC 523), as
		// result of the insert.
	}

	@Override
	public void print() {
		ModifiedBufferPrinter1_4.print(this.view, this.view.getBuffer(), false);
	}

	@Override
	public void pageSetup() {
		ModifiedBufferPrinter1_4.pageSetup(null);
	}

	@Override
	public JPanel getEditorPreferencePanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());

		TextAreaOptionPane prefpane = new TextAreaOptionPane();
		prefpane._init();
		panel.add(prefpane, BorderLayout.CENTER);

		JButton apply = new JButton("Apply");
		apply.addActionListener(new ApplyActionListener(prefpane));
		panel.add(apply, BorderLayout.SOUTH);
		return panel;
	}

	/**
	 * convenience function to create all the style objects.
	 *
	 * @param type
	 *            is {@link Font#BOLD}, {@link Font#PLAIN} etc.
	 * @return SyntaxStyle with given colors and style.
	 */
	private SyntaxStyle style(Font font, Color fg, Color bg) {
		return new SyntaxStyle(fg, bg, font);
	}

	/**
	 * style with background white.
	 *
	 * @param font
	 * @param fg
	 * @return
	 */
	private SyntaxStyle style(Font font, Color fg) {
		return new SyntaxStyle(fg, Color.white, font);
	}

	/**
	 * set the styles for the editing, matching the {@link EditorPrefPanel}.
	 * This overrides the jedit.props.
	 */
	public void setStyles() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				Font bold = new Font(EditorPreferences.getFontName(),
						Font.BOLD, EditorPreferences.getFontSize());
				Font plain = new Font(EditorPreferences.getFontName(),
						Font.PLAIN, EditorPreferences.getFontSize());
				Font italic = new Font(EditorPreferences.getFontName(),
						Font.ITALIC, EditorPreferences.getFontSize());

				// Color white = Color.white;
				Color black = Color.black;
				Color red = new Color(.8f, 0f, 0f);
				Color orange = new Color(1f, .5f, 0f);
				Color purple = new Color(.4f, 0f, .8f);
				Color green = new Color(0f, .73f, 0f);
				Color blue = new Color(0f, 0f, 1f);
				Color cyan = new Color(1f, 0f, .8f);
				Color brown = new Color(.8f, .4f, 0f);
				Color grayblue = new Color(0f, .5f, .5f);
				Color brightlime = new Color(.85f, 1f, .85f);
				Color brightyellow = new Color(1f, 1f, .8f);
				Color brightpurple = new Color(.85f, .85f, 1f);
				Color brightpink = new Color(1f, .94f, .98f);
				Color darkgreen = new Color(0f, .5f, 0f);
				Color darkblue = new Color(0f, 0f, .56f);
				Color darkpurple = new Color(.6f, 0f, .8f);
				Color darkerpurple = new Color(.4f, 0f, .8f);

				Color darkcyan = new Color(.8f, 0f, .8f);
				/**
				 * each of the text modes has an index in this array. see
				 * jedit.props file in jedit project. cc=.8 ff=1 84=80==.5 66=.4
				 */
				SyntaxStyle[] styles = { /* see jedit.props also */
						style(plain, black), // NONE
						style(plain, red), // view.style.comment1=color:#cc0000
						style(plain, orange),// view.style.comment2=color:#ff8400
						style(plain, purple),// view.style.comment3=color:#6600cc
						style(plain, brown),// view.style.comment4=color:#cc6600
						style(plain, grayblue),// view.style.digit=color:#008080
						style(italic, darkgreen),// view.style.function=color:#008000
						// style:i
						style(plain, red, brightyellow),// view.style.invalid=color:#ff0066
						// bgColor:#ffffcc
						style(bold, black),// view.style.keyword1=color:#000000
						// style:b
						style(plain, darkblue),// view.style.keyword2=color:#000090
						style(plain, blue),// view.style.keyword3=color:#0000ff
						style(bold, blue),// view.style.keyword4=color:#0000ff
						// style:b
						style(plain, green),// view.style.label=color:#02b902
						style(plain, cyan),// view.style.literal1=color:#ff00cc
						style(plain, darkcyan),// view.style.literal2=color:#cc00cc
						style(plain, darkpurple),// view.style.literal3=color:#9900cc
						style(plain, darkerpurple),// view.style.literal4=color:#6600cc
						style(plain, blue),// view.style.markup=color:#0000ff
						style(bold, black), // view.style.operator=color:#000000
						// style:b
						style(bold, black, brightlime),// view.style.foldLine.0=color:#000000
						// bgColor:#dafeda
						// style:b
						style(bold, black, brightyellow),// view.style.foldLine.1=color:#000000
						// bgColor:#fff0cc
						// style:b
						style(bold, black, brightpurple),// view.style.foldLine.2=color:#000000
						// bgColor:#e7e7ff
						// style:b
						style(bold, black, brightpink) /*
						 * view.style.foldLine.3=
						 * color:#000000
						 * bgColor:#ffe0f0
						 * style:b
						 */
				};

				getTextArea().getPainter().setStyles(styles);
				getTextArea().getPainter().setAntiAlias(
						new AntiAlias(EditorPreferences.isAntiAliased()));

				// WE NEED TO ENABLE THIS TO REFRESH STYLES.
				// getTextArea().foldStructureChanged();
			}
		});
	}

	@Override
	public Set<BreakPoint> getBreakpoints() {
		// translate jEdit to GOAL breakpoints
		Set<BreakPoint> breakpoints = new HashSet<BreakPoint>();
		for (Breakpoint bp : this.view.getBuffer().getBreakpoints()
				.getBreakpoints()) {
			BreakPoint.Type bptype;
			switch (bp.getType()) {
			case ALWAYS:
				bptype = Type.ALWAYS;
				break;
			case CONDITIONAL:
				bptype = Type.CONDITIONAL;
				break;
			default:
				throw new IllegalArgumentException(
						"inconsistency between GOAL and jEdit: unknown breakpoint type encountered "
								+ bp.getType());
			}
			// #3238 our bpts are 0-based, GOAL bpts are 1=based
			breakpoints.add(new BreakPoint(new File(getFilename()), bp
					.getLine() + 1, bptype));
		}
		return breakpoints;
	}

	@Override
	public void setBreakpoint(BreakPoint breakpoint)
			throws ArrayIndexOutOfBoundsException {
		Breakpoint.Type type;
		switch (breakpoint.getType()) {
		case ALWAYS:
			type = Breakpoint.Type.ALWAYS;
			break;
		case CONDITIONAL:
			type = Breakpoint.Type.CONDITIONAL;
			break;
		default:
			throw new IllegalArgumentException("unknown breakpoint type "
					+ breakpoint.getType());
		}
		// #3238 GOAL bpts are 1=based, our bpts are 0-based
		int line = this.view.getBuffer().getLineStartOffset(
				breakpoint.getLine() - 1);
		this.view.getBuffer().getBreakpoints().addBreakpoint(line, type);
	}

	@Override
	public void removeBreakpoint(BreakPoint breakpoint)
			throws ArrayIndexOutOfBoundsException {
		this.view.getBuffer().getBreakpoints()
		.removeBreakpoint(breakpoint.getLine() - 1);
	}

	@Override
	public void comment() {
		getTextArea().lineComment();

	}

	@Override
	public void uncomment() {
		getTextArea().lineUncomment();

	}
}

/**
 * Handles the apply button event from the pref pane Waits for the apply button
 * and then calls prefpane.save()
 */
class ApplyActionListener implements ActionListener {
	TextAreaOptionPane theprefpane;

	ApplyActionListener(TextAreaOptionPane prefpane) {
		this.theprefpane = prefpane;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		this.theprefpane._save();
	}
}