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

import goal.tools.PlatformManager;
import goal.tools.codeanalysis.ProgAnalysisResultGUI;
import goal.tools.codeanalysis.ProgramAnalysis;
import goal.tools.errorhandling.Resources;
import goal.tools.errorhandling.Warning;
import goal.tools.errorhandling.WarningStrings;
import goal.tools.errorhandling.exceptions.GOALException;
import goal.tools.errorhandling.exceptions.GOALUserError;

import java.awt.event.ActionEvent;
import java.io.File;

import nl.tudelft.goal.SimpleIDE.EditManager;
import nl.tudelft.goal.SimpleIDE.IDEMainPanel;
import nl.tudelft.goal.SimpleIDE.IDENode;

/**
 * Show GOAL agent program info and statistics for a GOAL program that is opened
 * in an editor. Silently fails if file to be analysed is not a GOAL agent file.
 *
 * TODO: change this so a GOAL agent file (or even better, an entire MAS
 * project) can be handled here, and drop requirement that the file must be
 * opened in editor. Do this by adding Program info option to right-click menu
 * in file panel.
 *
 *
 * @author W.Pasman 20jun2011
 */
public class GetProgInfoAction extends GOALAction {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public GetProgInfoAction() {
		setName("Static Code Analysis"); //$NON-NLS-1$
	}

	@Override
	/**
	 * {@inheritDoc}
	 */
	public void stateChangeEvent() {
		boolean isUserEditingGoalFile = true;
		boolean fileIsInMAS = true;
		try {
			String file = EditManager.getInstance().getActiveEditor()
					.getFilename();
			if (!file.endsWith(".goal")) { //$NON-NLS-1$
				isUserEditingGoalFile = false;
			} else {
				File theFile = EditManager.getInstance().getActiveEditor()
						.getFile();
				if (PlatformManager.getCurrent()
						.getMASProgramsThatUseFile(theFile).isEmpty()) {
					fileIsInMAS = false;
				}
			}

		} catch (GOALUserError e) {
			isUserEditingGoalFile = false;
		}

		setActionEnabled(this.currentState.getViewMode() == IDEMainPanel.EDIT_VIEW
				&& isUserEditingGoalFile && fileIsInMAS);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void execute(IDENode selectedNode, ActionEvent ae)
			throws GOALException {

		// This action is only enabled if the user is editing a .goal file.
		File theFile = EditManager.getInstance().getActiveEditor().getFile();

		// new InfoLog("Retrieving information for file: " + theFile);
		try {
			ProgramAnalysis analysis = new ProgramAnalysis(theFile);
			new ProgAnalysisResultGUI(developmentEnvironment.getMainPanel(),
					analysis);
		} catch (Exception e) {
			new Warning(
					Resources.get(WarningStrings.FAILED_PROGANALYSIS_GENERATE),
					e);
		}
	}
}
