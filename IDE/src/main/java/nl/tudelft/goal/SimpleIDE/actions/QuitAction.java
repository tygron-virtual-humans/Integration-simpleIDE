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

package nl.tudelft.goal.SimpleIDE.actions;

import goal.tools.errorhandling.exceptions.GOALException;

import java.awt.event.ActionEvent;

import nl.tudelft.goal.SimpleIDE.FilePanel;
import nl.tudelft.goal.SimpleIDE.IDENode;
import nl.tudelft.goal.SimpleIDE.preferences.IDEPreferences;

/**
 * Quits the IDE. Executing this action does NOT return.
 *
 * @author W.Pasman 20jun2011
 */
public class QuitAction extends GOALAction {
	/**
	 *
	 */
	private static final long serialVersionUID = 8576516187961874379L;

	/**
	 * Creates a short cut for this action.
	 *
	 * @see: {@link #setShortcut(char, int)}.
	 */
	public QuitAction() {
		setShortcut('Q');
	}

	@Override
	public void stateChangeEvent() {
		setActionEnabled(true);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void execute(IDENode selectedNode, ActionEvent ae)
			throws GOALException {
		FilePanel filePanel = developmentEnvironment.getMainPanel()
				.getFilePanel();

		IDEPreferences.setMASs(filePanel.getMASFiles());
		IDEPreferences.setOtherFiles(filePanel.getOtherPaths());
		IDEPreferences.setLastWinSize(developmentEnvironment.getFrame()
				.getSize());
		IDEPreferences.setLastWinPos(developmentEnvironment.getFrame()
				.getLocation());

		// Exit.
		developmentEnvironment.getMainPanel().closeAll();
		System.exit(0);

		// TODO close nicely.
		// closing nicely might imply closing all panels and thread,
		// and leaving it to Java to decide to exit.
	}
}
