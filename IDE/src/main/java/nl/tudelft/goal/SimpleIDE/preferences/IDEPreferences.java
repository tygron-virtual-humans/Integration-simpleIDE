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

import java.awt.Dimension;
import java.awt.Point;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

/**
 * This is a static interface into the IDE preference settings.
 *
 * @author W.Pasman 28may14 pulled this out of the core. Using Java preferences
 *         #3102
 */
public class IDEPreferences {

	/**
	 * Hide constructor. utility class.
	 */
	private IDEPreferences() {
	}

	public enum Pref {
		winwidth, winheight, consolesize, lastusedmass, lastusedgoals, rememberwinsize, rememberconsolesize, maxlines, reopenmass, reopenspurious, laf, consolefontsize, autoswitch, winx, winy, rememberwinpos, openDebugTraceTabAtAgentLaunch, eclipseActionHistory, eclipseAgentConsoles, eclipseDebug
	}

	private static Preferences myPrefs = Preferences
			.userNodeForPackage(IDEPreferences.class);

	private static final int DEFAULT_WIDTH = 800;
	private static final int DEFAULT_HEIGHT = 600;
	private static final int DEFAULT_CONSOLE_HEIGHT = 120;
	private static final int DEFAULT_LINES_IN_TEXTFIELDS = 500;
	private static final int FONT_DEFAULT_SIZE = 12;
	private static final int TOOLBARHEIGHT = 100; // estimated toolbar height...

	/**
	 * get total height of IDE window
	 *
	 * @returns the total height of the IDE window.
	 */
	public static int getWinHeight() {
		int height = DEFAULT_HEIGHT;
		if (getRememberWinSize()) {
			height = myPrefs.getInt(Pref.winheight.toString(), height);
		}
		return height;
	}

	/**
	 * get total width of IDE window
	 *
	 * @return total width of IDE window
	 */
	public static int getWinWidth() {
		int width = DEFAULT_WIDTH;
		if (getRememberWinSize()) {
			width = myPrefs.getInt(Pref.winwidth.toString(), width);
		}
		return width;
	}

	/**
	 * get the height of the main area above the console, which is also the
	 * split position of the jsplitpane.
	 *
	 * @returns the height of the main area above the console, which is also the
	 *          split position of the jsplitpane.
	 */
	public static int getMainAreaHeight() {
		return getWinHeight() - TOOLBARHEIGHT - getConsoleAreaHeight();
	}

	/**
	 * Get the height of the consol/feedback area
	 *
	 * @return the height of the consol/feedback area
	 */
	public static int getConsoleAreaHeight() {
		int height = DEFAULT_CONSOLE_HEIGHT; // the default size.
		if (getRememberConsoleSize()) {
			height = myPrefs.getInt(Pref.consolesize.toString(), height);
		}
		return height;
	}

	/**
	 * check if we should open debug trace tab when agent is launched. Default
	 * is true (open the tab)
	 *
	 * @return true iff we should open debug trace tab when agent is launched
	 */
	public static boolean getOpenDebugTraceTabAtAgentLaunch() {
		return myPrefs.getBoolean(
				Pref.openDebugTraceTabAtAgentLaunch.toString(), true);
	}

	/**
	 * Get whether user wants to remember the window size.
	 *
	 * @return true when user wants us to remember winsize, else false
	 */
	public static boolean getRememberWinSize() {
		return myPrefs.getBoolean(Pref.rememberwinsize.toString(), true);
	}

	/**
	 * Get whether user wants to remember the window position.
	 *
	 * @return true when user wants us to remember window position, else false
	 */
	public static boolean getRememberWinPos() {
		return myPrefs.getBoolean(Pref.rememberwinpos.toString(), true);
	}

	/**
	 * get the remembered last window pos.
	 *
	 * @return last window pos
	 */
	public static int getWinX() {
		return myPrefs.getInt(Pref.winx.toString(), 0);
	}

	public static int getWinY() {
		return myPrefs.getInt(Pref.winy.toString(), 0);
	}

	/**
	 * Get whether user wants us to remember console size
	 *
	 * @return true when user wants us to remember console size, else false
	 */
	public static boolean getRememberConsoleSize() {
		return myPrefs.getBoolean(Pref.rememberconsolesize.toString(), true);
	}

	/**
	 * get max number of lines that console can show.
	 *
	 * @return max number of lines that console can show
	 */
	public static int getMaxLines() {
		return myPrefs.getInt(Pref.maxlines.toString(),
				DEFAULT_LINES_IN_TEXTFIELDS);
	}

	/**
	 * get the font size to be used for the console.
	 *
	 * @return the font size to be used for the console
	 */
	public static int getConsoleFontSize() {
		return myPrefs.getInt(Pref.consolefontsize.toString(),
				FONT_DEFAULT_SIZE);
	}

	/**
	 * Check whether user wants at start-up of GOAL to re-open MASs that were
	 * open last time.
	 *
	 * @return true if user wants to re-open MASs, else false.
	 */
	public static boolean getReopenMASs() {
		return myPrefs.getBoolean(Pref.reopenmass.toString(), true);
	}

	/**
	 * get whether user wants at start-up to re-open spurious agent files at
	 * start-up of GOAL
	 *
	 * @return true when user wants to re-open, else false.
	 */
	public static boolean getReopenSpurious() {
		return myPrefs.getBoolean(Pref.reopenspurious.toString(), true);
	}

	/**
	 * Get whether user wants to switch automatically to agent debug tabs when
	 * they are spawned
	 *
	 * @return true when user wants to switch automatically, false if not.
	 */
	public static boolean getAutoSwitchDebugTab() {
		return myPrefs.getBoolean(Pref.autoswitch.toString(), true);
	}

	/**
	 * Returns selected Look and Feel (LAF). "Nimbus" and "Default" are
	 * supported. Default is Nimbus on Windows, and Default elsewhere.
	 *
	 * @return requested LAF.
	 */
	public static String getLAF() {
		String os = System.getProperty("os.name").toLowerCase();
		return myPrefs.get(Pref.laf.toString(),
				os.contains("windows") ? "Nimbus" : "Default");
	}

	/**
	 * Returns paths to the MAS files that were loaded into the IDE previously.
	 *
	 * @return A list of paths to MAS files.
	 */
	public static List<String> getMASs() {
		return getFiles(myPrefs.get(Pref.lastusedmass.toString(), "[]"));
	}

	/**
	 * Returns paths to the spurious files that were loaded into the IDE
	 * previously.
	 *
	 * @return A list of paths to other files.
	 */
	public static List<String> getOtherFiles() {
		return getFiles(myPrefs.get(Pref.lastusedgoals.toString(), "[]"));
	}

	/**
	 * Internal utility function. Returns list of paths retrieved from given
	 * string.
	 *
	 * @param input
	 *            String representing one or more paths between double quotes
	 *            separated by ",".
	 * @return List of paths to files.
	 */
	private static List<String> getFiles(String input) {
		List<String> filepaths = new ArrayList<String>();

		if (!input.isEmpty()) {
			for (String path : input.split(File.pathSeparator)) {
				filepaths.add(path.trim());
			}
		}
		return filepaths;
	}

	/**
	 * The height of the console/feedback area
	 */
	public static void setConsoleAreaHeight(int consolesize) {
		myPrefs.putInt(Pref.consolesize.toString(), consolesize);
	}

	/**
	 * If we should open debug trace tab when agent is launched. Default is true
	 * (open the tab)
	 */
	public static void setOpenDebugTraceTabAtAgentLaunch(
			boolean openDebugTraceTabAtAgentLaunch) {
		myPrefs.putBoolean(Pref.openDebugTraceTabAtAgentLaunch.toString(),
				openDebugTraceTabAtAgentLaunch);
	}

	/**
	 * Whether user wants to remember the window size.
	 */
	public static void setRememberWinSize(boolean rememberwinsize) {
		myPrefs.putBoolean(Pref.rememberwinsize.toString(), rememberwinsize);
	}

	/**
	 * Whether user wants to remember the window position.
	 */
	public static void setRememberWinPos(boolean rememberwinpos) {
		myPrefs.putBoolean(Pref.rememberwinpos.toString(), rememberwinpos);
	}

	/**
	 * Whether user wants us to remember console size
	 */
	public static void setRememberConsoleSize(boolean rememberconsolesize) {
		myPrefs.putBoolean(Pref.rememberconsolesize.toString(),
				rememberconsolesize);
	}

	/**
	 * The max number of lines that console can show.
	 */
	public static void setMaxLines(int maxlines) {
		myPrefs.putInt(Pref.maxlines.toString(), maxlines);
	}

	/**
	 * The font size to be used for the console.
	 */
	public static void setConsoleFontSize(int consolefontsize) {
		myPrefs.putInt(Pref.consolefontsize.toString(), consolefontsize);
	}

	/**
	 * Whether user wants at start-up of GOAL to re-open MASs that were open
	 * last time.
	 */
	public static void setReopenMASs(boolean reopenmass) {
		myPrefs.putBoolean(Pref.reopenmass.toString(), reopenmass);
	}

	/**
	 * Whether user wants at start-up to re-open spurious agent files at
	 * start-up of GOAL
	 */
	public static void setReopenSpurious(boolean reopenspurious) {
		myPrefs.putBoolean(Pref.reopenspurious.toString(), reopenspurious);
	}

	/**
	 * Whether user wants to switch automatically to agent debug tabs when they
	 * are spawned
	 */
	public static void setAutoSwitchDebugTab(boolean autoswitch) {
		myPrefs.putBoolean(Pref.autoswitch.toString(), autoswitch);
	}

	/**
	 * Look and Feel (LAF). "Nimbus" and "Default" are supported. Default is
	 * Nimbus on Windows, and Default elsewhere.
	 */
	public static void setLAF(String laf) {
		myPrefs.put(Pref.laf.toString(), laf);
	}

	/**
	 * Saves a list of MAS filenames that are to be reloaded when the IDE is
	 * restarted. Filenames should not contain commas.
	 *
	 * @param masFilenames
	 *            a list of MAS filenames.
	 */
	public static void setMASs(List<File> masFilenames) {
		StringBuilder paths = new StringBuilder();

		// Put * between path names (to allow for "," in file paths).
		for (int i = 0; i < masFilenames.size(); i++) {
			paths.append(i == 0 ? "" : File.pathSeparator);
			paths.append(masFilenames.get(i));
		}
		// Save filepaths.
		myPrefs.put(Pref.lastusedmass.toString(), paths.toString());
	}

	/**
	 * Saves a list of .goal filenames that are to be reloaded when the IDE is
	 * restarted. Filenames should not contain commas.
	 *
	 * @param goalFilenames
	 *            A list of agent filename.
	 */
	public static void setOtherFiles(List<File> goalFilenames) {
		StringBuilder paths = new StringBuilder();

		// Put * between path names (to allow for "," in file paths).
		for (int i = 0; i < goalFilenames.size(); i++) {
			paths.append(i == 0 ? "" : File.pathSeparator);
			paths.append(goalFilenames.get(i));
		}
		// Save filepaths.
		myPrefs.put(Pref.lastusedgoals.toString(), paths.toString());
	}

	/**
	 * set the last window pos to remember for next time
	 *
	 * @param location
	 *            is last window location
	 */
	public static void setLastWinPos(Point location) {
		myPrefs.putInt(Pref.winx.toString(), location.x);
		myPrefs.putInt(Pref.winy.toString(), location.y);
	}

	/**
	 * set the last window size to remember for next time
	 *
	 * @param size
	 *            is last window size
	 */
	public static void setLastWinSize(Dimension size) {
		myPrefs.putInt(Pref.winwidth.toString(), size.width);
		myPrefs.putInt(Pref.winheight.toString(), size.height);
	}

}
