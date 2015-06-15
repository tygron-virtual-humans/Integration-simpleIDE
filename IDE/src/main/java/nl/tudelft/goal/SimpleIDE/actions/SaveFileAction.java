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
import goal.tools.errorhandling.exceptions.GOALUserError;
import goal.util.Extension;
import goalhub.krTools.KRFactory;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import krTools.KRInterface;
import krTools.errors.exceptions.KRInitFailedException;
import krTools.errors.exceptions.ParserException;
import languageTools.exceptions.relationParser.InvalidEmotionConfigFile;
import nl.tudelft.goal.SimpleIDE.EditManager;
import nl.tudelft.goal.SimpleIDE.IDEMainPanel;
import nl.tudelft.goal.SimpleIDE.IDENode;
import nl.tudelft.goal.SimpleIDE.IconFactory;

/**
 * Save file currently being edited in the editor panel.
 *
 * @author W.Pasman 20jun2011
 */
public class SaveFileAction extends GOALAction {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Create 'Save File' action.
	 */
	public SaveFileAction() {
		setIcon(IconFactory.SAVE_TEXT.getIcon());
		setShortcut('S');
		setDescription("Save to file");
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
	 * DOC
	 *
	 * @throws GOALUserError
	 * @throws GOALParseException
	 */
	@Override
	protected void execute(IDENode selectedNode, ActionEvent e)
			throws GOALUserError {
		File theFile = EditManager.getInstance().save();
		Extension ext = Extension.getFileExtension(theFile);
		if (ext != null) {
			switch (ext) {
			case MAS:
				try {
					PlatformManager.getCurrent().parseMASFile(theFile);
				} catch (ParserException | FileNotFoundException | InvalidEmotionConfigFile e1) {
					throw new GOALUserError("Can't parse MAS file " + theFile,
							e1);
				} finally {
					developmentEnvironment.getMainPanel().getFilePanel()
							.refreshMASFile(theFile);
				}
				break;
			case GOAL:
				try {
					KRInterface language;
					try {
						language = KRFactory.getDefaultInterface();
					} catch (KRInitFailedException e1) {
						throw new GOALUserError("Can't parse GOAL file "
								+ theFile, e1);
					}
					// FIXME how can we already know the language at this
					// point?? We did not yet parse the goal file!
					// KRlanguage language = PlatformManager
					// .getGOALProgam(theFile).getKRLanguage();
					try {
						PlatformManager.getCurrent().parseGOALFile(theFile,
								language);
					} catch (ParserException e1) {
						throw new GOALUserError("Can't parse GOAL file "
								+ theFile, e1);
					}
				} finally {
					developmentEnvironment.getMainPanel().getFilePanel()
							.refreshGOALFile(theFile);
				}
				break;
			case MODULES:
				developmentEnvironment.getMainPanel().getFilePanel()
						.refreshMod2gFile(theFile);
				break;
			case EMOTION:
				try {
					try {
						PlatformManager.getCurrent().parseEmotionConfig(
								theFile.getCanonicalFile());
					} catch (ParserException | IOException e1) {
						throw new GOALUserError("Can't parse GOAL file "
								+ theFile, e1);
					}
				} finally {
					developmentEnvironment.getMainPanel().getFilePanel()
							.refreshEmo2gFile(theFile);
				}
				break;
			case PROLOG:
				developmentEnvironment.getMainPanel().getFilePanel()
						.refreshPrologFile(theFile);
				break;
			default:
				break;
			}
		}
	}

}
