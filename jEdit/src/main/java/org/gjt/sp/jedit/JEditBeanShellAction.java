/*
 * JEditBeanShellAction.java - jEdit BeanShell action
 * :tabSize=8:indentSize=8:noTabs=false:
 * :folding=explicit:collapseFolds=1:
 *
 *
 * * Wouter: I think this is used only from the standalonetextarea.
 * 
 * 
 * Copyright (C) 2007 Matthieu Casanova
 * Portions Copyright (C) 2000, 2003 Slava Pestov
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

import org.gjt.sp.jedit.bsh.*;
import org.gjt.sp.jedit.gui.InputHandler;
import org.gjt.sp.jedit.gui.StatusBar;

import java.awt.Component;
import java.awt.event.KeyListener;

import javax.swing.JFrame;

import org.gjt.sp.jedit.textarea.JEditTextArea;
import org.gjt.sp.jedit.textarea.TextArea;
import org.gjt.sp.util.Log;

/**
 * An action that evaluates BeanShell code when invoked. BeanShell actions are
 * usually loaded from <code>actions.xml</code> and
 * <code>browser.actions.xml</code> files; see {@link ActionSet} for syntax
 * information.
 * 
 * Wouter: I think this is used only from the standalonetextarea.
 *
 * @see jEdit#getAction(String)
 * @see jEdit#getActionNames()
 * @see ActionSet
 *
 * @author Slava Pestov
 * @author Matthieu Casanova
 * @version $Id: BeanShellAction.java 10803 2007-10-04 20:45:31Z kpouer $
 */
public class JEditBeanShellAction extends JEditAbstractEditAction<ViewInterface>
{
	//{{{ BeanShellAction constructor
	public JEditBeanShellAction(String name, String code, String isSelected,
		boolean noRepeat, boolean noRecord, boolean noRememberLast)
	{
		super(name);

		this.code = code;
		this.isSelected = isSelected;
		this.noRepeat = noRepeat;
		this.noRecord = noRecord;
		this.noRememberLast = noRememberLast;

		/* Some characters that we like to use in action names
		 * ('.', '-') are not allowed in BeanShell identifiers. */
		sanitizedName = name.replace('.','_').replace('-','_');
	} //}}}

	//{{{ invoke() method
	public void invoke(ViewInterface view)
	{
		try
		{
			if(cachedCode == null)
			{
				String cachedCodeName = "action_" + sanitizedName;
				cachedCode = bsh.cacheBlock(cachedCodeName,code,true);
			}

			bsh.runCachedBlock(cachedCode,view,
				new NameSpace(bsh.getNameSpace(),
				"BeanShellAction.invoke()"));
		}
		catch(Throwable e)
		{
			Log.log(Log.ERROR,this,e);
		}
	} //}}}

	//{{{ isSelected() method
	public boolean isSelected(Component comp)
	{
		if(isSelected == null)
			return false;

		NameSpace global = bsh.getNameSpace();

		try
		{
			if(cachedIsSelected == null)
			{
				String cachedIsSelectedName = "selected_" + sanitizedName;
				cachedIsSelected = bsh.cacheBlock(cachedIsSelectedName,
					isSelected,true);
			}

			// undocumented hack to allow browser actions to work.
			// XXX - clean up in 4.3
			global.setVariable("_comp",comp);

			return Boolean.TRUE.equals(bsh.runCachedBlock(
				cachedIsSelected,null,
				new NameSpace(bsh.getNameSpace(),
				"BeanShellAction.isSelected()")));
		}
		catch(Throwable e)
		{
			Log.log(Log.ERROR,this,e);

			// dialogs fuck things up if a menu is visible, etc!
			//new BeanShellErrorDialog(view,e);

			// so that in the future we don't see streams of
			// exceptions
			isSelected = null;

			return false;
		}
		finally
		{
			try
			{
				global.setVariable("_comp",null);
			}
			catch(UtilEvalError err)
			{
				Log.log(Log.ERROR,this,err);
			}
		}
	} //}}}

	//{{{ noRepeat() method
	public boolean noRepeat()
	{
		return noRepeat;
	} //}}}

	//{{{ noRecord() method
	public boolean noRecord()
	{
		return noRecord;
	} //}}}

	//{{{ noRememberLast() method
	/**
	 * Returns if this edit action should not be remembered as the most
	 * recently invoked action.
	 * @since jEdit 4.2pre1
	 */
	public boolean noRememberLast()
	{
		return noRememberLast;
	} //}}}

	//{{{ getCode() method
	public String getCode()
	{
		return code.trim();
	} //}}}

	//{{{ Private members
	private boolean noRepeat;
	private boolean noRecord;
	private boolean noRememberLast;
	private String code;
	private String isSelected;
	private BshMethod cachedCode;
	private BshMethod cachedIsSelected;
	private String sanitizedName;
	private static final BeanShellFacade<ViewInterface> bsh = new StandAloneBeanShellFacade();
	//}}}
	
	 // Wouter: changed, now using StandAloneBeanShellFacade. instead of private class.

}

