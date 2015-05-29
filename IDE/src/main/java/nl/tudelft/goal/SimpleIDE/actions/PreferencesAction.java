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

import nl.tudelft.goal.SimpleIDE.EditManager;
import nl.tudelft.goal.SimpleIDE.IDENode;
import nl.tudelft.goal.SimpleIDE.TextEditorInterface;
import nl.tudelft.goal.SimpleIDE.prefgui.PreferencesPanel;

/**
 * Displays the {@link PreferencesPanel}.
 *
 * @author W.Pasman 20jun2011
 */
public class PreferencesAction extends GOALAction {
	/**
	 *
	 */
	private static final long serialVersionUID = 8161707443287918949L;

	@Override
	/**
	 * {@inheritDoc}
	 */
	public void stateChangeEvent() {
		setActionEnabled(true);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void execute(IDENode selectedNode, ActionEvent ae)
			throws GOALException {
		TextEditorInterface ed = null;
		if (EditManager.getInstance().hasActiveEditor()) {
			ed = EditManager.getInstance().getActiveEditor();
		}
		new PreferencesPanel(developmentEnvironment.getFrame(),
				ed != null ? ed.getEditorPreferencePanel() : null);
	}
}
