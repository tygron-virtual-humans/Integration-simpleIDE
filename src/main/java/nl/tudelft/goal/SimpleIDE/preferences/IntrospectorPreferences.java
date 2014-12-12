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
package nl.tudelft.goal.SimpleIDE.preferences;

import java.util.prefs.Preferences;

/**
 * This is a static interface into the Introspector preference settings.
 *
 * @author W.Pasman 28may14 pulled this out of the core. Using Java preferences
 *         #3102
 */

public class IntrospectorPreferences {

	/**
	 * Hide constructor. this is utility class.
	 */
	private IntrospectorPreferences() {
	}

	public enum Pref {
		rememberdbsize, couplequerysize, maxlines, dbsize
	}

	private static Preferences myPrefs = Preferences
			.userNodeForPackage(IntrospectorPreferences.class);

	/**
	 * Get max number of lines for the debug trace output panel
	 *
	 * @return max number of lines for the debug trace output panel
	 */
	public static int getMaxLines() {
		return myPrefs.getInt(Pref.maxlines.toString(), 100);
	}

	/**
	 * get the size of the database area.
	 *
	 * CHECK dbsize seems not used anymore. Is this obsolete?
	 *
	 * @return height of database area size, which is also the position of the
	 *         split of the Jsplitpane
	 */
	public static int getDBContentSize() {
		int height = IDEPreferences.getMainAreaHeight() * 7 / 10; // default
		if (getRememberDBSize()) {
			height = myPrefs.getInt(Pref.dbsize.toString(), height);
		}
		return height;
	}

	/**
	 * remember the introspector size on the screen CHECK this seems not used
	 * anymore. Is that a bug?
	 *
	 * @return true when we need to remember the introspector size on the screen
	 */
	public static boolean getRememberDBSize() {
		return myPrefs.getBoolean(Pref.rememberdbsize.toString(), true);
	}

	/**
	 * Check if query sizes of all introspector panels should be equal and
	 * coupled. If the user then drags one of them, the others follow.
	 *
	 * @return true if panels are coupled
	 */
	public static boolean getCoupleQuerySize() {
		return myPrefs.getBoolean(Pref.couplequerysize.toString(), true);
	}

	/**
	 * max number of lines for the debug trace output panel
	 */
	public static void setMaxLines(int maxlines) {
		myPrefs.putInt(Pref.maxlines.toString(), maxlines);
	}

	/**
	 * the size of the database area.
	 *
	 * CHECK dbsize seems not used anymore. Is this obsolete?
	 */
	public static void setDBContentSize(int dbsize) {
		myPrefs.putInt(Pref.dbsize.toString(), dbsize);
	}

	/**
	 * remember the introspector size on the screen? CHECK this seems not used
	 * anymore. Is that a bug?
	 */
	public static void setRememberDBSize(boolean rememberdbsize) {
		myPrefs.putBoolean(Pref.rememberdbsize.toString(), rememberdbsize);
	}

	/**
	 * If query sizes of all introspector panels should be equal and coupled. If
	 * the user then drags one of them, the others follow.
	 */
	public static void setCoupleQuerySize(boolean couplequerysize) {
		myPrefs.putBoolean(Pref.couplequerysize.toString(), couplequerysize);
	}

}
