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
import goal.tools.errorhandling.exceptions.GOALUserError;

import java.awt.event.ActionEvent;

import nl.tudelft.goal.SimpleIDE.EditManager;
import nl.tudelft.goal.SimpleIDE.IDEMainPanel;
import nl.tudelft.goal.SimpleIDE.IDENode;
import nl.tudelft.goal.SimpleIDE.IconFactory;

/**
 * Ask current editor to cut current selection.
 *
 * @author W.Pasman 20jun2011
 */
public class CutAction extends GOALAction {
	/**
	 *
	 */
	private static final long serialVersionUID = 6265639091725707522L;

	public CutAction() {
		setIcon(IconFactory.CUT_TEXT.getIcon());
		setShortcut('X');
		setDescription("Move selected text to clipboard");
	}

	@Override
	/**
	 * {@inheritDoc}
	 */
	public void stateChangeEvent() {
		boolean isUserEditing = true;
		try {
			developmentEnvironment.getMainPanel().getCurrentPanel();
		} catch (GOALUserError e) {
			isUserEditing = false;
		}
		setActionEnabled(this.currentState.getViewMode() == IDEMainPanel.EDIT_VIEW
				&& isUserEditing);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void execute(IDENode selectedNode, ActionEvent ae)
			throws GOALException {
		EditManager.getInstance().cut();
	}
}
