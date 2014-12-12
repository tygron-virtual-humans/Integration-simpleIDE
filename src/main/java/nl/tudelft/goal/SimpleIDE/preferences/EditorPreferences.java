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
 * This is a (static) interface to the editor's preference settings.
 * 
 * @author W.Pasman 28may14 pulled this out of the core. Using Java preferences
 *         #3102
 * 
 */
public class EditorPreferences {

	/**
	 * Hide constructor. Utility class.
	 */
	private EditorPreferences() {

	}

	public enum Pref {
		fontname, fontsize, antialias
	}

	private static Preferences myPrefs = Preferences
			.userNodeForPackage(EditorPreferences.class);

	private static final int DEFAULT_FONT_SIZE = 12;

	/**
	 * get name of font that the user selected for use with editor. Default
	 * value is "Courier New", which is the font that we thought acceptable on
	 * all platforms.
	 * 
	 * @return name of selected font.
	 */
	public static String getFontName() {
		return myPrefs.get(Pref.fontname.toString(), "Courier New");
	}

	/**
	 * get selected font size
	 * 
	 * @return selected font size
	 */
	public static int getFontSize() {
		return myPrefs.getInt(Pref.fontsize.toString(), DEFAULT_FONT_SIZE);
	}

	public static boolean isAntiAliased() {
		return myPrefs.getBoolean(Pref.antialias.toString(), true);
	}

	/**
	 * name of font that the user selected for use with editor. Default value is
	 * "Courier New", which is the font that we thought acceptable on all
	 * platforms.
	 */
	public static void setFontName(String fontname) {
		myPrefs.put(Pref.fontname.toString(), fontname);
	}

	/**
	 * selected font size
	 */
	public static void setFontSize(int fontsize) {
		myPrefs.putInt(Pref.fontsize.toString(), fontsize);
	}

	public static void isAntiAliased(boolean antialias) {
		myPrefs.putBoolean(Pref.antialias.toString(), antialias);
	}

}
