package org.gjt.sp.jedit;

import java.awt.event.KeyListener;

import javax.swing.JFrame;

import org.gjt.sp.jedit.buffer.JEditBuffer;
import org.gjt.sp.jedit.gui.StatusBar;
import org.gjt.sp.jedit.input.AbstractInputHandler;
import org.gjt.sp.jedit.textarea.TextArea;

/** 
 * @author Wouter: added 27nov2008
 * We need something like a View in order to call the completeWord function.
 * We can not extend View as a View extends a JFrame. 
 * Therefore we use AbstractInputHandler which seems to be sufficient for most jobs.
 * 
 * 
 */
public class ViewSubstitute implements ViewInterface {
	JFrame the_frame=null;
	TextArea text_area=null;
	AbstractInputHandler<JEditBeanShellAction> inputhandler;
	Macros.Recorder recorder=null;
	
	public ViewSubstitute() {
	}
	
	public void setFrame(JFrame frame) {
		the_frame=frame;
	}
	
	/** @return the_frame, or if the_frame was never set, 
	 * it returns the parent of the root pane of text_area
	 */
	public JFrame getFrame() {
		if (the_frame!=null) return the_frame;
		return (JFrame)text_area.getRootPane().getParent();
	}
	
	public void setTextArea(TextArea txtarea) {
		text_area=txtarea;
		inputhandler=txtarea.getInputHandler(); 
			//Wouter: HACK. I think we should
			//make a copy but AbstractInputHandler has protected constructor....
	}
	
	public TextArea getTextArea() {
		return text_area;
	}
	
	public JEditBuffer getBuffer() {
		return text_area.getBuffer();
	}
	
	public KeyListener getKeyEventInterceptor() {
		return inputhandler.getKeyEventInterceptor();
	}
	
	public void setKeyEventInterceptor(KeyListener listener){
		inputhandler.setKeyEventInterceptor(listener);
	}
	
	public AbstractInputHandler<JEditBeanShellAction> getInputHandler() {
		return inputhandler;
	}
	
	/** Wouter: dumb try: don't return anything.
	 * This will fail if a word has a 'description', but I don't know how to add
	 * descriptions to words for the autocompletion anyway.
	 * TODO We may have to provide a status area somwehere if this is important. */
	public StatusBar getStatus() { return null; }

	public Macros.Recorder getMacroRecorder() { 
		return recorder;
	}
	
	/* Wouter: no need to set this */
	public void setMacroRecorder(Macros.Recorder r) { 
		recorder=r; 
	}
}
