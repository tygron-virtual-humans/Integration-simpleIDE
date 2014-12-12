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

package nl.tudelft.goal.SimpleIDE;

import goal.tools.errorhandling.Resources;
import goal.tools.errorhandling.Warning;
import goal.tools.errorhandling.WarningStrings;

import java.net.URL;

import javax.swing.ImageIcon;

/**
 * 
 * This factory provides all the IDE icons. It prevents multiple creation of the
 * same icon.
 * <p>
 * Although this technically is an enumeration, I think it fulfills enough of
 * the factory pattern to be called a factory. The main point being that it
 * decouples the actual icon from the code using the icon.
 * <p>
 * By using enumeration instead of class, we can avoid the issues associated
 * with initializing static objects, and looks much cleaner too than for example
 * the code in "Thinking in Java" p583.
 * <p>
 * Warnings concerning un-loadable icons are printed with a Warning during
 * loading of this class. At runtime, icons that failed to load will return null
 * as icon.
 * 
 * @author W.Pasman 24jun2011
 * 
 */
public enum IconFactory {

	/* process control icons */
	RUN("run.gif"),

	PAUSE("pause.gif"),

	STEP_PROCESS("step.gif"),

	DEBUG("debug_exc.png"),

	BACKSTEP_PROCESS("backstep.gif"),

	PARK_PROCESS("park.gif"),

	RESET_PROCESS("reset.gif"),

	KILL_PROCESS("kill.gif"),

	/* process state icons */

	RUNNING_PROCESS("runningprocess.gif"),

	STEPPING_PROCESS("steppingprocess.gif"),

	STEPPED_PROCESS("steppedprocess.gif"),

	PAUSED_PROCESS("pausedprocess.gif"),

	KILLED_PROCESS("killedprocess.gif"),

	QUESTIONMARK("questionmark.gif"),

	REMOTE_PROCESS("remoteprocess.gif"),

	/* file icons */

	MAS_FILE("masfile.gif"),

	NO_MAS_FILE("masfile-ne.png"),

	GOAL_FILE("goalfile.gif"),

	NO_GOAL_FILE("goalfile-ne.png"),

	PL_FILE("plfile.gif"),

	NO_PL_FILE("plfile-ne.png"),

	OTHER_FILE("typelessfile.gif"),
	/*
	 * file manipulation commands
	 */

	NEW_FILE("new.gif"),

	OPEN_FILE("open.gif"),

	/* text control command icons */

	UNDO_TEXT("undo.gif"),

	REDO_TEXT("redo.gif"),

	SAVE_TEXT("save.gif"),

	SAVE_ALL_TEXT("saveall.gif"),

	SAVE_AS_TEXT("saveas.gif"),

	COPY_TEXT("copy.gif"),

	CUT_TEXT("cut.gif"),

	PASTE_TEXT("paste.gif"),

	EDIT_TEXT("edit.gif");

	private ImageIcon icon = null;

	private IconFactory(String filename) {
		URL iconfile = getClass().getClassLoader().getResource(
				"nl/tudelft/goal/SimpleIDE/icons/" + filename);
		if (iconfile == null) {
			new Warning(String.format(
					Resources.get(WarningStrings.FAILED_ICON_GET), filename));
		} else {
			icon = new ImageIcon(iconfile);
		}
	}

	/**
	 * You use this by for example <code>
	 * IconFactory.RUN.getIcon() 
	 * </code>
	 * 
	 * @return the ImageIcon, or null if the icon could not be loaded.
	 */
	public ImageIcon getIcon() {
		return icon;
	}

}
