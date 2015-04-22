/*
 * StandaloneTextArea.java - A TextArea that can be embedded in applications
 * :tabSize=8:indentSize=8:noTabs=false:
 * :folding=explicit:collapseFolds=1:
 *
 *
 * Wouter: general remark: was it not possible to create a JEditTextArea
 * but using the ViewInterface, instead of making a separate
 * StandaloneTextArea lacking functionality (eg gotoline, search/replace, etc?)
 *
 * Copyright (C) 1999, 2005 Slava Pestov
 * Portions copyright (C) 2000 Ollie Rutherfurd
 * Portions copyright (C) 2006 Matthieu Casanova
 * Portions copyright (C) 2008 Dakshinamurthy Karra
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
 * 
 */
package org.gjt.sp.jedit.textarea;

//{{{ Imports
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.WindowConstants;

import org.gjt.sp.jedit.IPropertyManager;
import org.gjt.sp.jedit.JEditActionSet;
import org.gjt.sp.jedit.JEditBeanShellAction;
import org.gjt.sp.jedit.Mode;
import org.gjt.sp.jedit.ViewInterface;
import org.gjt.sp.jedit.ViewSubstitute;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.buffer.Breakpoint;
import org.gjt.sp.jedit.buffer.DefaultFoldHandlerProvider;
import org.gjt.sp.jedit.buffer.DummyFoldHandler;
import org.gjt.sp.jedit.buffer.ExplicitFoldHandler;
import org.gjt.sp.jedit.buffer.FoldHandler;
import org.gjt.sp.jedit.buffer.IndentFoldHandler;
import org.gjt.sp.jedit.buffer.JEditBuffer;
import org.gjt.sp.jedit.buffer.KillRing;
import org.gjt.sp.jedit.input.AbstractInputHandler;
import org.gjt.sp.jedit.syntax.ModeProvider;
import org.gjt.sp.jedit.syntax.ParserRuleSet;
import org.gjt.sp.jedit.syntax.SyntaxStyle;
import org.gjt.sp.jedit.syntax.TokenMarker;
import org.gjt.sp.util.IOUtilities;
import org.gjt.sp.util.Log;
import org.gjt.sp.util.SyntaxUtilities;

//}}}

/**
 * jEdit's standalone text component.
 * <p>
 * 
 * Use this class to embed a jEdit text area into other applications.
 * 
 * Example: see the main() function in this class. You can run
 * "java org.gjt.sp.jedit.textarea.StandaloneTextArea" to get demo.
 * 
 * <code>
 		JFrame frame = new JFrame();
		ViewSubstitute viewSubst=new ViewSubstitute();
		viewSubst.setFrame(frame); 

		jEdit.initSystemProperties();		
		TextArea text = createTextArea(viewSubst); // this creates stand-alone textarea.
		viewSubst.setTextArea(text);
		
		Mode prologmode = new Mode("prolog"); // add prolog support as well. 
		prologmode.setProperty("file","modes/prolog.xml");
		ModeProvider.instance.addMode(prologmode);
		
		Mode mode = new Mode("goal");
		mode.setProperty("file","modes/goal.xml");
		ModeProvider.instance.addMode(mode);
		
		text.getBuffer().setMode(mode);
		frame.getContentPane().add(text);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);

 * </code>
 * 
 * See jedit.props for properties that can be set.
 * 
 * @author Slava Pestov
 * @author John Gellene (API documentation)
 * @version $Id: JEditTextArea.java 7148 2006-09-29 23:09:06 +0200 (ven., 29
 *          sept. 2006) kpouer $
 */
public class StandaloneTextArea extends TextArea {

	// {{{ Instance variables
	private final IPropertyManager propertyManager;
	// }}}

	ViewInterface the_view = null;

	// {{{ StandaloneTextArea constructor
	/**
	 * Creates a new StandaloneTextArea. A reference to the propertyManager is
	 * saved and used to read the properties when
	 * {@link StandaloneTextArea#propertiesChanged()} is called.
	 * 
	 * @param propertyManager
	 *            the property manager that contains both shortcut bindings and
	 *            UI information
	 */
	public StandaloneTextArea(IPropertyManager propertyManager,
			ViewInterface view) {
		super(propertyManager, null);
		the_view = view;
		this.propertyManager = propertyManager;

		initInputHandler(view);

		setMouseHandler(new TextAreaMouseHandler(this));
		// todo : make TextareaTransferHandler standalone
		// textArea.setTransferHandler(new TextAreaTransferHandler());

		JEditActionSet<JEditBeanShellAction> actionSet = new StandaloneActionSet(
				propertyManager, this);

		addActionSet(actionSet);
		actionSet.load();
		actionSet.initKeyBindings();

		// {{{ init Style property manager
		if (SyntaxUtilities.propertyManager == null) {
			SyntaxUtilities.propertyManager = propertyManager;
		}
		// }}}

		initTextArea();

		DefaultFoldHandlerProvider foldHandlerProvider = new DefaultFoldHandlerProvider();

		FoldHandler.foldHandlerProvider = foldHandlerProvider;
		foldHandlerProvider.addFoldHandler(new ExplicitFoldHandler());
		foldHandlerProvider.addFoldHandler(new IndentFoldHandler());
		foldHandlerProvider.addFoldHandler(new DummyFoldHandler());
		// Wouter: would like to call Buffer(String path, boolean newFile,
		// boolean temp, Map props)
		JEditBuffer buffer = new JEditBuffer();
		TokenMarker tokenMarker = new TokenMarker();
		tokenMarker.addRuleSet(new ParserRuleSet("text", "MAIN"));
		buffer.setTokenMarker(tokenMarker);
		setBuffer(buffer);
		String property = propertyManager.getProperty("buffer.undoCount");
		int undoCount = 100;
		if (property != null)
			try {
				undoCount = Integer.parseInt(property);
			} catch (NumberFormatException e) {
			}
		this.buffer.setUndoLimit(undoCount);
		Mode mode = new Mode("text");
		mode.setTokenMarker(tokenMarker);
		ModeProvider.instance.addMode(mode);
		KillRing.setInstance(new KillRing());
		KillRing.getInstance().propertiesChanged(100);

	} // }}}

	// {{{ initTextArea() method
	/**
	 * Initializes the text area by re-reading the properties from the property
	 * manager passed to the constructor.
	 */
	private void initTextArea() {
		initPainter();
		initGutter();

		setCaretBlinkEnabled(getBooleanProperty("view.caretBlink"));

		setElectricScroll(getIntegerProperty("view.electricBorders", 0));

		if (buffer == null)
			return;

		String property = propertyManager.getProperty("buffer.undoCount");
		int undoCount = 100;
		if (property != null) {
			try {
				undoCount = Integer.parseInt(property);
			} catch (NumberFormatException e) {
			}
		}
		buffer.setUndoLimit(undoCount);
	} // }}}

	// {{{ initGutter() method
	private void initGutter() {
		Gutter gutter = getGutter();
		gutter.setExpanded(getBooleanProperty("view.gutter.lineNumbers"));
		int interval = getIntegerProperty("view.gutter.highlightInterval", 5);
		gutter.setHighlightInterval(interval);
		gutter.setCurrentLineHighlightEnabled(getBooleanProperty("view.gutter.highlightCurrentLine"));
		gutter.setStructureHighlightEnabled(getBooleanProperty("view.gutter.structureHighlight"));
		gutter.setStructureHighlightColor(getColorProperty("view.gutter.structureHighlightColor"));
		gutter.setBackground(getColorProperty("view.gutter.bgColor"));
		gutter.setForeground(getColorProperty("view.gutter.fgColor"));
		gutter.setHighlightedForeground(getColorProperty("view.gutter.highlightColor"));
		gutter.setFoldColor(getColorProperty("view.gutter.foldColor"));
		gutter.setCurrentLineForeground(getColorProperty("view.gutter.currentLineColor"));
		String alignment = getProperty("view.gutter.numberAlignment");
		if ("right".equals(alignment)) {
			gutter.setLineNumberAlignment(Gutter.RIGHT);
		} else if ("center".equals(alignment)) {
			gutter.setLineNumberAlignment(Gutter.CENTER);
		} else // left == default case
		{
			gutter.setLineNumberAlignment(Gutter.LEFT);
		}

		gutter.setFont(getFontProperty("view.gutter.font"));

		int width = getIntegerProperty("view.gutter.borderWidth", 3);
		gutter.setBorder(width,
				getColorProperty("view.gutter.focusBorderColor"),
				getColorProperty("view.gutter.noFocusBorderColor"),
				painter.getBackground());
		gutter.addExtension(new BreakpointHighlightExtension(this));
	} // }}}

	// {{{ initPainter() method
	/**
	 * Init the painter of this textarea.
	 * 
	 */
	private void initPainter() {
		TextAreaPainter painter = getPainter();
		painter.setBlockCaretEnabled(false);

		painter.setFont(getFontProperty("view.font"));
		painter.setStructureHighlightEnabled(getBooleanProperty("view.structureHighlight"));
		painter.setStructureHighlightColor(getColorProperty("view.structureHighlightColor"));
		painter.setEOLMarkersPainted(getBooleanProperty("view.eolMarkers"));
		painter.setEOLMarkerColor(getColorProperty("view.eolMarkerColor"));
		painter.setWrapGuidePainted(getBooleanProperty("view.wrapGuide"));
		painter.setWrapGuideColor(getColorProperty("view.wrapGuideColor"));
		painter.setCaretColor(getColorProperty("view.caretColor"));
		painter.setSelectionColor(getColorProperty("view.selectionColor"));
		painter.setMultipleSelectionColor(getColorProperty("view.multipleSelectionColor"));
		painter.setBackground(getColorProperty("view.bgColor"));
		painter.setForeground(getColorProperty("view.fgColor"));
		painter.setBlockCaretEnabled(getBooleanProperty("view.blockCaret"));
		painter.setThickCaretEnabled(getBooleanProperty("view.thickCaret"));
		painter.setLineHighlightEnabled(getBooleanProperty("view.lineHighlight"));
		painter.setLineHighlightColor(getColorProperty("view.lineHighlightColor"));
		painter.setAntiAlias(new AntiAlias(getProperty("view.antiAlias")));
		painter.setFractionalFontMetricsEnabled(getBooleanProperty("view.fracFontMetrics"));

		String defaultFont = getProperty("view.font");
		int defaultFontSize = getIntegerProperty("view.fontsize", 12);
		painter.setStyles(SyntaxUtilities.loadStyles(defaultFont,
				defaultFontSize));

		SyntaxStyle[] foldLineStyle = new SyntaxStyle[4];
		for (int i = 0; i <= 3; i++) {
			foldLineStyle[i] = SyntaxUtilities.parseStyle(
					getProperty("view.style.foldLine." + i), defaultFont,
					defaultFontSize, true);
		}
		painter.setFoldLineStyle(foldLineStyle);
	} // }}}

	// {{{
	// The following methods are copied from jEdit.java and refer to the
	// propertyManager passed
	// to the constructor.
	// }}}

	// {{{ getProperty() method
	public String getProperty(String name) {
		return propertyManager.getProperty(name);
	} // }}}

	// {{{ getBooleanProperty() method
	/**
	 * Returns the value of a boolean property.
	 * 
	 * @param name
	 *            The property
	 */
	private boolean getBooleanProperty(String name) {
		return getBooleanProperty(name, false);
	} // }}}

	// {{{ getBooleanProperty() method
	/**
	 * Returns the value of a boolean property.
	 * 
	 * @param name
	 *            The property
	 * @param def
	 *            The default value
	 */
	private boolean getBooleanProperty(String name, boolean def) {
		String value = getProperty(name);
		if (value == null)
			return def;
		else if (value.equals("true") || value.equals("yes")
				|| value.equals("on"))
			return true;
		else if (value.equals("false") || value.equals("no")
				|| value.equals("off"))
			return false;
		else
			return def;
	} // }}}

	// {{{ getIntegerProperty() method
	/**
	 * Returns the value of an integer property.
	 * 
	 * @param name
	 *            The property
	 */
	private int getIntegerProperty(String name) {
		return getIntegerProperty(name, 0);
	} // }}}

	// {{{ getIntegerProperty() method
	/**
	 * Returns the value of an integer property.
	 * 
	 * @param name
	 *            The property
	 * @param def
	 *            The default value
	 * @since jEdit 4.0pre1
	 */
	private int getIntegerProperty(String name, int def) {
		String value = getProperty(name);
		if (value == null)
			return def;
		else {
			try {
				return Integer.parseInt(value.trim());
			} catch (NumberFormatException nf) {
				return def;
			}
		}
	} // }}}

	// {{{ getFontProperty() method
	/**
	 * Returns the value of a font property. The family is stored in the
	 * <code><i>name</i></code> property, the font size is stored in the
	 * <code><i>name</i>size</code> property, and the font style is stored in
	 * <code><i>name</i>style</code>. For example, if <code><i>name</i></code>
	 * is <code>view.gutter.font</code>, the properties will be named
	 * <code>view.gutter.font</code>, <code>view.gutter.fontsize</code>, and
	 * <code>view.gutter.fontstyle</code>.
	 * 
	 * @param name
	 *            The property
	 * @since jEdit 4.0pre1
	 */
	private Font getFontProperty(String name) {
		return getFontProperty(name, null);
	} // }}}

	/**
	 * Returns the value of a font property. The family is stored in the
	 * <code><i>name</i></code> property, the font size is stored in the
	 * <code><i>name</i>size</code> property, and the font style is stored in
	 * <code><i>name</i>style</code>. For example, if <code><i>name</i></code>
	 * is <code>view.gutter.font</code>, the properties will be named
	 * <code>view.gutter.font</code>, <code>view.gutter.fontsize</code>, and
	 * <code>view.gutter.fontstyle</code>.
	 * 
	 * @param name
	 *            The property
	 * @param def
	 *            The default value
	 * @since jEdit 4.0pre1
	 */
	private Font getFontProperty(String name, Font def) {
		String family = getProperty(name);
		String sizeString = getProperty(name + "size");
		String styleString = getProperty(name + "style");

		if (family == null || sizeString == null || styleString == null)
			return def;
		else {
			int size;

			try {
				size = Integer.parseInt(sizeString);
			} catch (NumberFormatException nf) {
				return def;
			}

			int style;
			try {
				style = Integer.parseInt(styleString);
			} catch (NumberFormatException nf) {
				return def;
			}

			return new Font(family, style, size);
		}
	} // }}}

	// {{{ getColorProperty() method
	/**
	 * Returns the value of a color property.
	 * 
	 * @param name
	 *            The property name
	 * @since jEdit 4.0pre1
	 */
	private Color getColorProperty(String name) {
		return getColorProperty(name, Color.black);
	} // }}}

	// {{{ getColorProperty() method
	/**
	 * Returns the value of a color property.
	 * 
	 * @param name
	 *            The property name
	 * @param def
	 *            The default value
	 * @since jEdit 4.0pre1
	 */
	private Color getColorProperty(String name, Color def) {
		String value = getProperty(name);
		if (value == null)
			return def;
		else
			return SyntaxUtilities.parseColor(value, def);
	} // }}}

	// {{{ propertiesChanged() method
	/**
	 * Reinitializes the textarea by reading the properties from the property
	 * manager
	 */
	@Override
	public void propertiesChanged() {
		initBuffer();
		initTextArea();
		super.propertiesChanged();
	} // }}}

	// {{{ initBuffer() method
	/**
	 * Reinitializes the buffer by reading the properties from the property
	 * manager
	 */
	private void initBuffer() {
		String[] bufferProperties = { "lineSeparator", "encodingAutodetect",
				"tabSize", "indentSize", "noTabs", "defaultMode", "undoCount",
				"wrap", "maxLineLen", "wordBreakChars", "noWordSep",
				"camelCasedWords", "folding", "collapseFolds" };
		for (int i = 0; i < bufferProperties.length; i++) {
			String value = getProperty("buffer." + bufferProperties[i]);
			if (value == null)
				buffer.unsetProperty(bufferProperties[i]);
			else
				buffer.setProperty(bufferProperties[i], value);
		}
		buffer.propertiesChanged();
	}

	// {{{ createPopupMenu() method
	/**
	 * Creates the popup menu.
	 * 
	 * @since 4.3pre15
	 */
	@Override
	public void createPopupMenu(MouseEvent evt) {
		popup = new JPopupMenu();
		// addMenuItem("cut-append", "Cut");
		// popup.addSeparator();
		addMenuItem("breakpoint-add", "add breakpoint");
		addMenuItem("condbreakpoint-add", "add conditional breakpoint");
		addMenuItem("breakpoint-remove", "remove breakpoint");

	} // }}}

	// {{{ addMenuItem() method
	/**
	 * Adds a menu item from the action context to the popup menu and returns
	 * the item.
	 * 
	 * Wouter; this appears broken, because shellAction = null
	 * 
	 * @return the menu item added
	 */
	public JMenuItem addMenuItem(String action, String label) {
		final JEditBeanShellAction shellAction = getActionContext().getAction(
				action);
		if (shellAction == null)
			return null;
		JMenuItem item = new JMenuItem(label);
		item.setAction(new AbstractAction(label) {
			public void actionPerformed(ActionEvent e) {
				shellAction.invoke(the_view);
			}
		});
		popup.add(item);
		return item;
	} // }}}

	// {{{ createTextArea() method
	/**
	 * Create a standalone TextArea. If you want to use it in jEdit, please use
	 * {@link org.gjt.sp.jedit.jEdit#createTextArea()}
	 * 
	 * Wouter: added viewinterface parameter 2dec08
	 * 
	 * @param view
	 *            is the view interface. It may not yet be completely set up.
	 * 
	 * @return a textarea
	 * @since 4.3pre13
	 */
	public static StandaloneTextArea createTextArea(ViewInterface view) {
		final Properties props = new Properties();
		props.putAll(loadProperties("/org/gjt/sp/jedit/jedit_keys.props"));
		props.putAll(loadProperties("/org/gjt/sp/jedit/jedit.props"));
		IPropertyManager propmgr = new IPropertyManager() {
			public String getProperty(String name) {
				return props.getProperty(name);
			}
		};
		StandaloneTextArea textArea = new StandaloneTextArea(propmgr, view);
		textArea.getBuffer().setProperty("folding", "explicit");

		return textArea;
	} // }}}

	// {{{ loadProperties() method.
	static Properties loadProperties(String fileName) {
		Properties props = new Properties();
		InputStream in = TextArea.class.getResourceAsStream(fileName);
		try {
			props.load(in);
		} catch (IOException e) {
			Log.log(Log.ERROR, TextArea.class, e);
		} finally {
			IOUtilities.closeQuietly(in);
		}
		return props;
	} // }}}

	// {{{ StandaloneActionSet class
	/**
	 * The actionSet for standalone textArea.
	 * 
	 * @author Matthieu Casanova
	 */
	private static class StandaloneActionSet extends
			JEditActionSet<JEditBeanShellAction> {
		private final IPropertyManager iPropertyManager;
		private final TextArea textArea;

		StandaloneActionSet(IPropertyManager iPropertyManager, TextArea textArea) {
			super(null, TextArea.class.getResource("textarea.actions.xml"));
			this.iPropertyManager = iPropertyManager;
			this.textArea = textArea;
		}

		@Override
		protected JEditBeanShellAction[] getArray(int size) {
			return new JEditBeanShellAction[size];
		}

		@Override
		protected String getProperty(String name) {
			return iPropertyManager.getProperty(name);
		}

		public AbstractInputHandler getInputHandler() {
			return textArea.getInputHandler();
		}

		@Override
		protected JEditBeanShellAction createBeanShellAction(String actionName,
				String code, String selected, boolean noRepeat,
				boolean noRecord, boolean noRememberLast) {
			return new JEditBeanShellAction(actionName, code, selected,
					noRepeat, noRecord, noRememberLast);
		}
	} // }}}

	/**
	 * Add breakpoint(s) to current selection & caret. TODO fix labels
	 */
	public void addBreakpoint() {
		addBreakpoints(Breakpoint.Type.ALWAYS);
	}

	/**
	 * add a conditional breakpoint to current selection & caret.
	 */
	public void addConditionalBreakpoint() {
		addBreakpoints(Breakpoint.Type.CONDITIONAL);
	}

	/**
	 * Add breakpoints of given type to current caret line and all selected
	 * lines.
	 * 
	 * @param type
	 */
	public void addBreakpoints(Breakpoint.Type type) {
		buffer.getBreakpoints().addBreakpoint(getCaretPosition(), type);

		// always add markers on selected lines
		for (Selection s : getSelection()) {
			for (int line = s.getStartLine(); line < s.getEndLine(); line++) {
				buffer.getBreakpoints().addBreakpoint(
						buffer.getLineStartOffset(line), type);
			}
		}
	}

	/**
	 * Remove breakpoint(s) from current caret line and the current selection.
	 * TODO fix labels.
	 */
	public void removeBreakpoint() {
		buffer.getBreakpoints().removeBreakpoint(getCaretLine());

		// remove markers on selected lines
		Selection[] selection = getSelection();
		for (int i = 0; i < selection.length; i++) {
			Selection s = selection[i];
			for (int line = s.getStartLine(); line < s.getEndLine(); line++) {
				buffer.getBreakpoints().removeBreakpoint(line);
			}
		}

	}

	/*
	 * 
	 * public static String ReadFile(String filename) throws Exception { String
	 * filecontents=""; System.out.println("reading file "+filename);
	 * 
	 * try { //use buffering, reading one line at a time //FileReader always
	 * assumes default encoding is OK! BufferedReader input = new
	 * BufferedReader(new FileReader(filename)); // readLine is a bit quirky :
	 * // it returns the content of a line MINUS the newline. // it returns null
	 * only for the END of the stream. // it returns an empty String if two
	 * newlines appear in a row. String line; while (( line = input.readLine())
	 * != null){ filecontents=filecontents+line+"\n"; } input.close(); } catch
	 * (Exception ex){ ex.printStackTrace(); } return filecontents; }
	 */

	// {{{ main() method
	public static void main(String[] args) {
		// Wouter: viewSubst and TextArea refer to each other in a cyclic way.
		// that makes the init rather difficult.
		// I hope the garbage collector can still cope with this.
		// TODO figure out how this can be done in a nicer way.

		JFrame frame = new JFrame();
		/* Wouter: create a view substitute, we need it to create the MyTextArea */
		ViewSubstitute viewSubst = new ViewSubstitute();
		// viewSubst.setFrame(frame); test, it should now work without setting
		// this

		jEdit.initSystemProperties();
		/*
		 * Wouter: seems not needed? final Properties props = new Properties();
		 * props.putAll(loadProperties("/org/gjt/sp/jedit/jedit_keys.props"));
		 * props.putAll(loadProperties("/org/gjt/sp/jedit/jedit.props"));
		 */

		TextArea text = createTextArea(viewSubst); // this creates stand-alone
													// textarea.
		viewSubst.setTextArea(text);

		Mode prologmode = new Mode("prolog"); // add prolog support as well.
		prologmode.setProperty("file", "modes/prolog.xml");
		ModeProvider.instance.addMode(prologmode);

		Mode mode = new Mode("goal");
		mode.setProperty("file", "modes/goal.xml");
		ModeProvider.instance.addMode(mode);

		/*
		 * try { if (args.length>0) text.getBuffer().insert(0,
		 * ReadFile(args[0])); } catch (Exception e) {
		 * System.out.println("failure opening file "+args[0]+":"+e); }
		 */

		text.getBuffer().setMode(mode);
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(text, BorderLayout.CENTER);
		frame.getContentPane().add(panel);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
	} // }}}
}
