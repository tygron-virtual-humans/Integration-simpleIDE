package goal;

import java.awt.Frame;

import javax.swing.JFrame;

import com.apple.eawt.AppEvent.QuitEvent;
import com.apple.eawt.Application;
import com.apple.eawt.QuitHandler;
import com.apple.eawt.QuitResponse;

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
 *		&nbsp;&nbsp;	OSXAdapter.setQuitHandler(.. your quit handler runnable..);<br>
 *		}<br>
 *	</tt>
 *
 * @author W.Pasman 22mar10. 
 * @modified W.Pasman 24jun2011 after overhaul of IDE. Noted that some stuff we use is officially
 * deprecated and made ticket #1805
 * @rewritten W.Pasman 15dec14
 */


@SuppressWarnings("restriction")
public class OSXAdapter {

	/**
	 * Attach your quit handler. When user presses ctrl-Q, handler#run will be called
	 * @param handler a {@link Runnable}.
	 */
	public static void setQuitHandler(final Runnable handler) {

		@SuppressWarnings("restriction")
		Application a = Application.getApplication();

		a.setQuitHandler(new QuitHandler() {

			public void handleQuitRequestWith(QuitEvent arg0, QuitResponse arg1) {
				handler.run();
			}
		});
	}

}