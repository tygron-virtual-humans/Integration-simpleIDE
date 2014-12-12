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

import goal.tools.errorhandling.exceptions.GOALActionFailedException;
import goal.tools.errorhandling.exceptions.GOALCommandCancelledException;
import goal.tools.errorhandling.exceptions.GOALException;
import goal.tools.errorhandling.exceptions.GOALIncompleteGUIUsageException;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import nl.tudelft.goal.SimpleIDE.IDENode;

/**
 * Open user docu in web browser
 * 
 * @author W.Pasman 3feb2014
 * 
 */
@SuppressWarnings("serial")
public class DocumentationAction extends GOALAction {

	private static String doc = "http://ii.tudelft.nl/trac/goal/wiki//WikiStart#Documentation";

	public DocumentationAction() {
		setDescription("open documentation in web browser");
	}

	@Override
	public void stateChangeEvent() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void execute(IDENode node, ActionEvent arg)
			throws GOALCommandCancelledException,
			GOALIncompleteGUIUsageException, GOALException {
		URI uri;
		try {
			uri = new URI(doc);

			Desktop desktop = Desktop.isDesktopSupported() ? Desktop
					.getDesktop() : null;
			if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
				desktop.browse(uri);
			}
		} catch (URISyntaxException e) {
			throw new GOALActionFailedException("can't open web page:", e);
		} catch (IOException e) {
			throw new GOALActionFailedException("can't open web page:", e);

		}
	}
}
