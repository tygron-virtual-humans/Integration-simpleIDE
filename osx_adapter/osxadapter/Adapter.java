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


package osxadapter;

import com.apple.eawt.Application;
import com.apple.eawt.ApplicationAdapter;
import com.apple.eawt.ApplicationEvent;

import nl.tudelft.goal.SimpleIDE.ActionFactory;
import nl.tudelft.goal.SimpleIDE.IDEfunctionality;
import nl.tudelft.goal.SimpleIDE.UserCmd;
import nl.tudelft.goal.SimpleIDE.actions.QuitAction;
/**
 * This OSXAdapter enables us to handle the Quit command on OSX properly.
 * Without this, the Apple-Q shortcut kills java instantly, no questions asked.
 * But we want to have a callback on the Quit action so that we can check
 * whether files still need to be saved.
 * <br>
 * The OSXAdapter is a special OSX dependent java file containing OSX dependent tweaks.
 *	<p>
 *	You can compile this only on OSX.
 *	 It can be included with any system but it can be INSTANTIATED only on OSX.
 *	So you should test if you are on OSX before instantiating, by doing this somethign like:
 *	</p>
 *	<tt>
 *		if (System.getProperty("os.name").equals("Mac OS X")) {<br>
 *		&nbsp;&nbsp;	adapter=new Adapter();<br>
 *		}<br>
 *	</tt>
 *
 * @author W.Pasman 22mar10. 
 * @modified W.Pasman 24jun2011 after overhaul of IDE. Noted that some stuff we use is officially
 * deprecated and made ticket #1805
 */
public class Adapter extends Application {

	public Adapter(final IDEfunctionality IDE) {
		
		// reroute the QUIT function.
		this.addApplicationListener(new ApplicationAdapter() {
			public void handleQuit(ApplicationEvent arg0) {
				System.out.println("Command+Q pressed");
				try {
					ActionFactory.getAction(QuitAction.class).Execute(null,null);
				} catch (Exception e) {
					System.out.println("QUIT failed");
					e.printStackTrace();
				}
			}
		});
		
	}
}
