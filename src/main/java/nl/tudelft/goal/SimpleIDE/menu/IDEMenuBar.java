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

package nl.tudelft.goal.SimpleIDE.menu;

import goal.preferences.RunPreferences;

import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;

import nl.tudelft.goal.SimpleIDE.ActionFactory;
import nl.tudelft.goal.SimpleIDE.actions.AboutAction;
import nl.tudelft.goal.SimpleIDE.actions.BackstepAction;
import nl.tudelft.goal.SimpleIDE.actions.ClearPreferencesAction;
import nl.tudelft.goal.SimpleIDE.actions.CloseAction;
import nl.tudelft.goal.SimpleIDE.actions.CloseAndRemoveAction;
import nl.tudelft.goal.SimpleIDE.actions.CommentAction;
import nl.tudelft.goal.SimpleIDE.actions.CompleteWordAction;
import nl.tudelft.goal.SimpleIDE.actions.CopyAction;
import nl.tudelft.goal.SimpleIDE.actions.CutAction;
import nl.tudelft.goal.SimpleIDE.actions.DebugLogAction;
import nl.tudelft.goal.SimpleIDE.actions.DeleteAction;
import nl.tudelft.goal.SimpleIDE.actions.DocumentationAction;
import nl.tudelft.goal.SimpleIDE.actions.EditAction;
import nl.tudelft.goal.SimpleIDE.actions.FindAction;
import nl.tudelft.goal.SimpleIDE.actions.FindNextAction;
import nl.tudelft.goal.SimpleIDE.actions.GetProgInfoAction;
import nl.tudelft.goal.SimpleIDE.actions.GotoLineAction;
import nl.tudelft.goal.SimpleIDE.actions.IntrospectorAction;
import nl.tudelft.goal.SimpleIDE.actions.KillAction;
import nl.tudelft.goal.SimpleIDE.actions.MemStatsAction;
import nl.tudelft.goal.SimpleIDE.actions.NewFileAction;
import nl.tudelft.goal.SimpleIDE.actions.OpenFileAction;
import nl.tudelft.goal.SimpleIDE.actions.PageSetupAction;
import nl.tudelft.goal.SimpleIDE.actions.PasteAction;
import nl.tudelft.goal.SimpleIDE.actions.PauseAction;
import nl.tudelft.goal.SimpleIDE.actions.PreferencesAction;
import nl.tudelft.goal.SimpleIDE.actions.PrintAction;
import nl.tudelft.goal.SimpleIDE.actions.QuitAction;
import nl.tudelft.goal.SimpleIDE.actions.RedoAction;
import nl.tudelft.goal.SimpleIDE.actions.ReloadFileAction;
import nl.tudelft.goal.SimpleIDE.actions.RenameAction;
import nl.tudelft.goal.SimpleIDE.actions.ResetAction;
import nl.tudelft.goal.SimpleIDE.actions.RunAction;
import nl.tudelft.goal.SimpleIDE.actions.RunLocalAction;
import nl.tudelft.goal.SimpleIDE.actions.RunRmiAction;
import nl.tudelft.goal.SimpleIDE.actions.SaveAllFileAction;
import nl.tudelft.goal.SimpleIDE.actions.SaveAsFileAction;
import nl.tudelft.goal.SimpleIDE.actions.SaveFileAction;
import nl.tudelft.goal.SimpleIDE.actions.SaveSelectedFileAction;
import nl.tudelft.goal.SimpleIDE.actions.StepAction;
import nl.tudelft.goal.SimpleIDE.actions.SwitchPanelAction;
import nl.tudelft.goal.SimpleIDE.actions.ToBackAction;
import nl.tudelft.goal.SimpleIDE.actions.UnCommentAction;
import nl.tudelft.goal.SimpleIDE.actions.UndoAction;

/**
 * This is the Main IDE menu bar.
 * 
 */
@SuppressWarnings("serial")
public class IDEMenuBar extends JMenuBar {

	private static final boolean OSX = System.getProperty("os.name").equals(
			"Mac OS X");

	/**
	 * creates the IDE's menubar. This only involves layout, all the actual menu
	 * items are just {@link goal.core.action} objects.
	 * 
	 * @param theIDE
	 * @throws InstantiationException
	 */
	public IDEMenuBar() throws IllegalAccessException, InstantiationException {

		JMenuItem runLocalItem, runRMIItem;
		ButtonGroup middlewareGroup;

		// File menu
		JMenu fMenu = new JMenu("File");
		fMenu.add(new JMenuItem(ActionFactory.getAction(NewFileAction.class)));
		fMenu.add(new JMenuItem(ActionFactory.getAction(OpenFileAction.class)));
		fMenu.add(new JMenuItem(ActionFactory.getAction(SaveFileAction.class)));
		fMenu.add(new JMenuItem(ActionFactory.getAction(SaveAsFileAction.class)));
		fMenu.add(new JMenuItem(ActionFactory
				.getAction(SaveAllFileAction.class)));
		fMenu.add(new JMenuItem(ActionFactory
				.getAction(SaveSelectedFileAction.class)));
		fMenu.addSeparator();
		fMenu.add(new JMenuItem(ActionFactory.getAction(EditAction.class)));
		fMenu.add(new JMenuItem(ActionFactory.getAction(ReloadFileAction.class)));
		fMenu.add(new JMenuItem(ActionFactory.getAction(CloseAction.class)));
		fMenu.addSeparator();
		fMenu.add(new JMenuItem(ActionFactory
				.getAction(CloseAndRemoveAction.class)));
		fMenu.add(new JMenuItem(ActionFactory.getAction(DeleteAction.class)));
		fMenu.add(new JMenuItem(ActionFactory.getAction(RenameAction.class)));
		fMenu.addSeparator();
		fMenu.add(new JMenuItem(ActionFactory.getAction(PrintAction.class)));
		fMenu.add(new JMenuItem(ActionFactory.getAction(PageSetupAction.class)));

		// Edit menu
		JMenu eMenu = new JMenu("Edit");
		eMenu.add(new JMenuItem(ActionFactory.getAction(UndoAction.class)));
		eMenu.add(new JMenuItem(ActionFactory.getAction(RedoAction.class)));
		eMenu.addSeparator();
		eMenu.add(new JMenuItem(ActionFactory.getAction(CutAction.class)));
		eMenu.add(new JMenuItem(ActionFactory.getAction(CopyAction.class)));
		eMenu.add(new JMenuItem(ActionFactory.getAction(PasteAction.class)));
		eMenu.addSeparator();
		eMenu.add(new JMenuItem(ActionFactory.getAction(FindAction.class)));
		eMenu.add(new JMenuItem(ActionFactory.getAction(FindNextAction.class)));
		eMenu.add(new JMenuItem(ActionFactory.getAction(GotoLineAction.class)));
		eMenu.addSeparator();
		eMenu.add(new JMenuItem(ActionFactory
				.getAction(CompleteWordAction.class)));
		eMenu.addSeparator();
		eMenu.add(new JMenuItem(ActionFactory.getAction(CommentAction.class)));
		eMenu.add(new JMenuItem(ActionFactory.getAction(UnCommentAction.class)));

		// Run menu
		JMenu runMenu = new JMenu("Run");
		runMenu.add(new JMenuItem(ActionFactory.getAction(RunAction.class)));
		runMenu.add(new JMenuItem(ActionFactory.getAction(StepAction.class)));
		runMenu.add(new JMenuItem(ActionFactory.getAction(PauseAction.class)));
		runMenu.add(new JMenuItem(ActionFactory.getAction(BackstepAction.class)));
		runMenu.add(new JMenuItem(ActionFactory.getAction(ResetAction.class)));
		runMenu.add(new JMenuItem(ActionFactory.getAction(KillAction.class)));
		runMenu.addSeparator();

		runLocalItem = new JRadioButtonMenuItem(
				ActionFactory.getAction(RunLocalAction.class));
		runRMIItem = new JRadioButtonMenuItem(
				ActionFactory.getAction(RunRmiAction.class));

		runMenu.add(runLocalItem);
		runMenu.add(runRMIItem);

		// Radio button group and initialize it.
		middlewareGroup = new ButtonGroup();
		middlewareGroup.add(runLocalItem);
		middlewareGroup.add(runRMIItem);
		if (RunPreferences.getUsedMiddleware().equals("LOCAL")) {
			middlewareGroup.setSelected(runLocalItem.getModel(), true);
		} else if (RunPreferences.getUsedMiddleware().equals("RMI")) {
			middlewareGroup.setSelected(runRMIItem.getModel(), true);
		}

		runMenu.addSeparator();
		runMenu.add(new JMenuItem(ActionFactory.getAction(MemStatsAction.class)));

		// Debug menu
		JMenu dMenu = new JMenu("Debug");

		dMenu.add(new JMenuItem(ActionFactory
				.getAction(IntrospectorAction.class)));
		dMenu.add(new JMenuItem(ActionFactory.getAction(DebugLogAction.class)));
		// #2681 disabled
		// dMenu.add(new
		// JMenuItem(ActionFactory.getAction(ModelCheckerAction.class)));
		dMenu.addSeparator();
		dMenu.add(new JMenuItem(ActionFactory
				.getAction(GetProgInfoAction.class)));

		// Help menu
		JMenu helpMenu; // help menu on windows/linux CHECK how is help
						// facilitated on MAC?
		helpMenu = new JMenu(OSX ? "GOAL-IDE" : "Help");
		helpMenu.add(new JMenuItem(ActionFactory
				.getAction(PreferencesAction.class)));
		helpMenu.addSeparator();
		helpMenu.add(new JMenuItem(ActionFactory
				.getAction(SwitchPanelAction.class)));
		helpMenu.add(new JMenuItem(ActionFactory.getAction(ToBackAction.class)));
		helpMenu.add(new JMenuItem(ActionFactory
				.getAction(ClearPreferencesAction.class)));
		helpMenu.addSeparator();
		helpMenu.add(new JMenuItem(ActionFactory
				.getAction(DocumentationAction.class)));
		helpMenu.addSeparator();
		helpMenu.add(new JMenuItem(ActionFactory.getAction(AboutAction.class)));

		// insert items that have a system-dependent place in the menu
		if (OSX) {
			helpMenu.addSeparator();
			helpMenu.add(new JMenuItem(ActionFactory
					.getAction(QuitAction.class)));
		} else {
			fMenu.addSeparator();
			fMenu.add(new JMenuItem(ActionFactory.getAction(QuitAction.class)));
		}

		// finally insert the menus in the (system-dependent) order.
		if (OSX) {
			add(helpMenu);
		}
		add(fMenu);
		add(eMenu);
		add(runMenu);
		add(dMenu);
		if (!OSX) {
			add(helpMenu);
		}

	}
}
