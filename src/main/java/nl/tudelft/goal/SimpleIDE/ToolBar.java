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

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JToolBar;

import nl.tudelft.goal.SimpleIDE.actions.BackstepAction;
import nl.tudelft.goal.SimpleIDE.actions.CopyAction;
import nl.tudelft.goal.SimpleIDE.actions.CutAction;
import nl.tudelft.goal.SimpleIDE.actions.EditAction;
import nl.tudelft.goal.SimpleIDE.actions.GOALAction;
import nl.tudelft.goal.SimpleIDE.actions.IntrospectorAction;
import nl.tudelft.goal.SimpleIDE.actions.KillAction;
import nl.tudelft.goal.SimpleIDE.actions.NewFileAction;
import nl.tudelft.goal.SimpleIDE.actions.OpenFileAction;
import nl.tudelft.goal.SimpleIDE.actions.PasteAction;
import nl.tudelft.goal.SimpleIDE.actions.RedoAction;
import nl.tudelft.goal.SimpleIDE.actions.ResetAction;
import nl.tudelft.goal.SimpleIDE.actions.RunOrPauseAction;
import nl.tudelft.goal.SimpleIDE.actions.SaveAllFileAction;
import nl.tudelft.goal.SimpleIDE.actions.SaveAsFileAction;
import nl.tudelft.goal.SimpleIDE.actions.SaveFileAction;
import nl.tudelft.goal.SimpleIDE.actions.StepAction;
import nl.tudelft.goal.SimpleIDE.actions.SwitchPanelAction;
import nl.tudelft.goal.SimpleIDE.actions.UndoAction;

/**
 * Defines the tool bar of the GOAL IDE. It introduces buttons for most of the
 * available user commands defined in the enum class UserCmd. Event handling is
 * taken care of by the class SimpleIDE.
 * 
 * See also:
 * http://java.sun.com/docs/books/tutorial/uiswing/components/toolbar.html.
 * 
 * @author KH
 * @author W.Pasman 23jun2011 now using actions.
 */
@SuppressWarnings("serial")
public class ToolBar extends JToolBar {

	/**
	 * Initializes the tool bar in the IDE. Sets layout and adds buttons to the
	 * tool bar.
	 * 
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public ToolBar() throws IllegalAccessException, InstantiationException {

		setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
		setName("GOAL IDE ToolBar");
		addButtons();
	}

	/**
	 * Add button for given action
	 * 
	 * @param action
	 *            is action class to be added.
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	private void addButton(Class<? extends GOALAction> action)
			throws IllegalAccessException, InstantiationException {
		JButton button = new JButton(ActionFactory.getAction(action));
		button.setHideActionText(true);
		add(button);
	}

	/**
	 * add the buttons to the toolbar.
	 * 
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	private void addButtons() throws IllegalAccessException,
			InstantiationException {
		// File buttons
		addButton(NewFileAction.class);
		addButton(OpenFileAction.class);
		addButton(SaveFileAction.class);
		addButton(SaveAsFileAction.class);
		addButton(SaveAllFileAction.class);
		addButton(EditAction.class);
		addSeparator();

		// Edit buttons
		addButton(UndoAction.class);
		addButton(RedoAction.class);
		addButton(CutAction.class);
		addButton(CopyAction.class);
		addButton(PasteAction.class);
		addSeparator();

		// Run buttons
		addButton(RunOrPauseAction.class);
		addButton(StepAction.class);
		addButton(BackstepAction.class);
		addButton(KillAction.class);
		addSeparator();

		// Debug button
		addButton(IntrospectorAction.class);

		addSeparator();

		// Agent reset button
		addButton(ResetAction.class);
		// alignment
		add(Box.createHorizontalGlue());
		addSeparator();

		// add switchpanel button directly, because we want to use the TEXT as
		// we have no icon there.
		add(ActionFactory.getAction(SwitchPanelAction.class));

	}

}
