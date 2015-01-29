/*
 * @author W.Pasman 3dec08
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

import org.gjt.sp.jedit.bsh.NameSpace;
import org.gjt.sp.jedit.bsh.UtilEvalError;
import org.gjt.sp.util.Log;


public class StandAloneBeanShellFacade extends BeanShellFacade<ViewInterface>
{
	ViewSubstitute viewSubst;
	
	protected void init()
	{
		super.init();
		// we use special versions of the actions, that use our ViewInterface instead of a View.
		global.importPackage("org.gjt.sp.jedit.guistandalone"); // special version of gui
		global.importPackage("org.gjt.sp.jedit.searchstandalone"); // special version of search
	}
	
	@Override
	protected void setupDefaultVariables(NameSpace namespace, ViewInterface view) throws UtilEvalError 
	{
		if(view != null)
		{
			namespace.setVariable("view",view, false);
			namespace.setVariable("buffer",view.getBuffer(), false);
			namespace.setVariable("textArea",view.getTextArea(), false);

		}
	}

	@Override
	protected void resetDefaultVariables(NameSpace namespace) throws UtilEvalError
	{
		namespace.setVariable("view",null, false);
		namespace.setVariable("buffer",null, false);
		namespace.setVariable("textArea",null, false);
	}

	@Override
	protected void handleException(ViewInterface view, String path, Throwable t)
	{
		Log.log(Log.ERROR,this, t, t);
//			new BeanShellErrorDialog(null,t);
	}
	
} 




