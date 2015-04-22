/*
 * View.java - jEdit view
 * :tabSize=8:indentSize=8:noTabs=false:
 * :folding=explicit:collapseFolds=1:
 *
 * Copyright (C)2008 W.Pasman
 * This interface allows flexible plugging of the View.
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

package org.gjt.sp.jedit;

//{{{ Imports
import java.awt.event.KeyListener;

import javax.swing.JFrame;

import org.gjt.sp.jedit.buffer.JEditBuffer;
import org.gjt.sp.jedit.gui.StatusBar;
import org.gjt.sp.jedit.input.AbstractInputHandler;
import org.gjt.sp.jedit.textarea.TextArea;

/**
 * A <code>Viewinterface</code> is an interface to the top-level frame window.<p>
 * In the end a view is 
 * It is needed a.o. by Actions that need to spawn new interfaces, 
 * eg autocompletion pop-ups and searchdialog.
 *
 *
 *
 * @author W.Pasman
 * @version 27nov08
 */

public interface ViewInterface //extends EBComponent, InputHandlerProvider // implements JFrame
{
	/** @author W.Pasman. I would like to have ViewInterface implements JFrame but unfortunately t
	 * that is not allowed as interfaces can not extend a class...
	 * Therefore I decided to use setFrame and getFrame functions. */
	
	/** sets the frame of this view (the 'parent') */
	public void setFrame(JFrame frame); 
	public JFrame getFrame(); // getss the frame
	
	 /** Wouter: to match View.java this should return a jEditTextArea
	  * but we only have a TextArea in the StandAloneEditor 
	  */
	public TextArea getTextArea();

		/** Wouter: to match View.java this should return Buffer
		 	but we do not have a Buffer in the StandAloneEditor. */
	public JEditBuffer getBuffer();			
	
	public KeyListener getKeyEventInterceptor();
	public void setKeyEventInterceptor(KeyListener listener);
	
	/** Wouter: this is the interface to the BeanShell stuff.
	 */
	public AbstractInputHandler getInputHandler();
	public StatusBar getStatus();
	
	/** these are used by Search&Replace deep down, to do repeated actions etc.
	 * Probably just returns null to indicate that there is no recorder. */
	public Macros.Recorder getMacroRecorder();
	public void setMacroRecorder(Macros.Recorder recorder);


	
}
