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

import goal.core.runtime.RuntimeManager;
import goal.tools.IDEDebugger;
import goal.tools.IDEGOALInterpreter;
import goal.tools.LaunchManager;
import goal.tools.PlatformManager;
import goal.tools.debugger.SteppingDebugger.RunMode;
import goal.tools.errorhandling.Resources;
import goal.tools.errorhandling.Warning;
import goal.tools.errorhandling.WarningStrings;
import goal.tools.errorhandling.exceptions.GOALException;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;

import krTools.errors.exceptions.ParserException;
import languageTools.program.agent.AgentProgram;
import languageTools.program.mas.MASProgram;
import nl.tudelft.goal.SimpleIDE.EditManager;
import nl.tudelft.goal.SimpleIDE.IDEMainPanel;
import nl.tudelft.goal.SimpleIDE.IDENode;
import nl.tudelft.goal.SimpleIDE.IconFactory;
import nl.tudelft.goal.SimpleIDE.NodeType;
import nl.tudelft.goal.SimpleIDE.ProcessNode;
import nl.tudelft.goal.SimpleIDE.TextEditorInterface;
import nl.tudelft.goal.SimpleIDE.files.FileNode;

/**
 * Run a process or mas file. IDE figures out whether run or pause action is
 * appropriate
 *
 * @author W.Pasman 20jun2011
 */
public class RunAction extends GOALAction {
	/**
	 *
	 */
	private static final long serialVersionUID = 6688356104736143198L;

	/**
	 *
	 */
	public RunAction() {
		setIcon(IconFactory.RUN.getIcon());
		setShortcut('R');
		setDescription("Launch multi-agent system"); //$NON-NLS-1$
	}

	@Override
	public void stateChangeEvent() {
		List<? extends IDENode> selection = this.currentState
				.getSelectedNodes();
		if (selection.isEmpty()) {
			setActionEnabled(false);
			return;
		}
		IDENode node = selection.get(0);
		NodeType nodeType = node.getType();

		switch (nodeType) {
		case NULLFILE:
		case TXTFILE:
		case REMOTE_AGENT_PROCESS:
			setActionEnabled(false);
			break;
		case ROOT: // enabled if only one MAS file available.
			setDescription("Launch multi-agent system"); //$NON-NLS-1$
			setActionEnabled(node.getChildCount() == 1);
			break;
		case MASFILE:
		case GOALFILE:
		case MODFILE:
		case PLFILE:
			setDescription("Launch multi-agent system"); //$NON-NLS-1$
			setActionEnabled(!this.currentState.isRuntimeEnvironmentAvailable());
			break;
		case MAS_PROCESS:
			setDescription("Run all agents"); //$NON-NLS-1$
			setActionEnabled(((ProcessNode) node).getProcessRunMode() != RunMode.RUNNING);
			break;
		case AGENT_PROCESS:
			setDescription("Run agent"); //$NON-NLS-1$
			setActionEnabled(((ProcessNode) node).getProcessRunMode() != RunMode.RUNNING);
			break;
		case ENVIRONMENT_PROCESS:
			setDescription("Run environment"); //$NON-NLS-1$
			setActionEnabled(((ProcessNode) node).getProcessRunMode() != RunMode.RUNNING);
			break;
		case REMOTE_ENVIRONMENT_PROCESS:
			setActionEnabled(false);
			break;
		}
	}

	@Override
	protected void execute(IDENode selectedNode, ActionEvent ae)
			throws GOALException {
		run(selectedNode);
	}

	/**
	 *
	 * @param node
	 * @throws GOALException
	 */
	private void run(IDENode node) throws GOALException {
		FileNode fileNode;

		// Check first whether we're in edit view and a MAS is already running.
		// In that case, simply switch view to debug view.
		if (this.currentState.getViewMode() == IDEMainPanel.EDIT_VIEW
				&& LaunchManager.getCurrent().isRuntimeEnvironmentAvailable()) {
			developmentEnvironment.getMainPanel().switchView();
			return;
		}

		switch (this.currentState.getViewMode()) {
		case IDEMainPanel.EDIT_VIEW:
			// Do nothing if the user canceled the save all action.
			// TODO: only require to save all used files.
			if (!checkAllSaved()) {
				return;
			}
			// If the node is the root node, check if there is a single unique
			// MAS we can launch.
			if (node.getType().equals(NodeType.ROOT)) {
				if (node.getChildCount() == 1) { // get unique MAS node
					node = (IDENode) node.getChildAt(0);
				} else {
					new Warning(Resources.get(WarningStrings.FAILED_RUN_NO_MAS));
					return;
				}
			}

			// Locate a MAS file node.
			fileNode = (FileNode) node;
			fileNode = developmentEnvironment.getMainPanel().getFilePanel()
					.getParentMASNode(fileNode);
			if (fileNode == null) {
				// Could not find a MAS file node; canceling.
				new Warning(Resources.get(WarningStrings.FAILED_RUN_NO_MAS));
				return;
			}

			// Parse MAS file. Ensures latest version is ran.
			final PlatformManager platform = PlatformManager.getCurrent();
			MASProgram mas = null;
			try {
				mas = platform.parseMASFile(fileNode.getBaseFile());
			} catch (ParserException e1) {
			}

			// check before proceeding, to avoid locking non-existant files.
			if (mas == null || !mas.isValid()) {
				new Warning(Resources.get(WarningStrings.FAILED_RUN_MAS_ERRORS));
				return;
			}

			// We're going to launch a MAS, disable editing of all involved
			// files.
			for (File file : platform.getMASProgram(fileNode.getBaseFile())
					.getAgentFiles()) {
				TextEditorInterface editor = EditManager.getInstance()
						.getEditorPane(file.toString());
				// TODO: QUICK HACK. #1717. We need to lock ALL files, not
				// just agent files.
				if (editor != null) {
					editor.setEditable(false);
				}
			}

			// Launch the MAS.
			try {
				List<File> involvedFiles = platform.getMASProgram(
						fileNode.getBaseFile()).getAgentFiles();

				// TODO: code does not belong here, needs to be moved...
				// make sure the breakpoints are up-to-date for all
				// agents before launching
				for (File agentFile : involvedFiles) {
					EditManager.getInstance().updateBreakpoints(agentFile);
				}

				MASProgram masprog = platform.getMASProgram(fileNode
						.getBaseFile());
				Map<File, AgentProgram> allPrograms = platform
						.getParsedAgentPrograms();
				Map<File, AgentProgram> programs = new HashMap<File, AgentProgram>();
				for (File agentfile : masprog.getAgentFiles()) {
					programs.put(agentfile, allPrograms.get(agentfile));
				}

				RuntimeManager<IDEDebugger, IDEGOALInterpreter> runtime = LaunchManager
						.createNew().launchMAS(masprog, programs);

				// Update view.
				developmentEnvironment.getMainPanel().getProcessPanel().init();
				developmentEnvironment.getMainPanel().switchView();
				// Subscribe feedback and process panels as observers.
				runtime.addObserver(developmentEnvironment.getMainPanel()
						.getFeedbackPanel());
				runtime.addObserver(developmentEnvironment.getMainPanel()
						.getProcessPanel());
				runtime.start(false);
			} catch (Exception e) { // TODO: distinguish between types of
				// exceptions...
				new Warning(Resources.get(WarningStrings.FAILED_RUN_MAS), e);
				for (TextEditorInterface editor : EditManager.getInstance()
						.getEditors()) {
					editor.setEditable(true);
				}
			}
			break;
		case IDEMainPanel.DEBUG_VIEW:
			developmentEnvironment.getMainPanel().getProcessPanel()
			.runProcessNode((ProcessNode) node);
			break;
		}
	}

	/**
	 * Save all files.
	 *
	 * @return {@code false} if user cancelled or a problem while saving
	 *         occurred; {@code true} otherwise.
	 */
	private boolean checkAllSaved() {
		if (EditManager.getInstance().isDirty()) { // content has been
			// edited
			int selection = JOptionPane.showConfirmDialog(
					developmentEnvironment.getMainPanel(), "Save all files?\n" //$NON-NLS-1$
					+ "Some files were edited but are not yet saved. " //$NON-NLS-1$
					+ "All files must be saved before launching.", //$NON-NLS-1$
					"Save all files?", JOptionPane.OK_CANCEL_OPTION); //$NON-NLS-1$

			switch (selection) {
			case JOptionPane.CANCEL_OPTION:
				// cancel -> exit
				return false;
			case JOptionPane.OK_OPTION:
				// OK -> save all and continue
				try {
					EditManager.getInstance().saveAll();
				} catch (GOALException e) {
					new Warning(Resources.get(WarningStrings.FAILED_SAVEALL), e);
					return false;
				}
				break;
			}
		}
		return true;
	}
}
