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

import goal.tools.errorhandling.Resources;
import goal.tools.errorhandling.Warning;
import goal.tools.errorhandling.WarningStrings;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;

import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

/**
 * Displays 'About GOAL' panel. Shows version number and web link.
 *
 * CHECK why is this in the "menu" directory as this is not a menu? Maybe we can
 * use a JOptionPane.showMessageDialog here, that way we can avoid handling
 * close buttons and events saving a few lines of code.
 *
 * @author W.Pasman
 */
public class AboutBox extends JFrame {
	/**
	 *
	 */
	private static final long serialVersionUID = -904263836528337663L;

	/**
	 * Displays 'About GOAL' panel. Shows version number and web link.
	 *
	 * @param is
	 *            component, used to determine position of this about box.
	 */
	public AboutBox(Component parent) {

		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setLayout(new BorderLayout());

		Package p = getClass().getPackage();
		String text = "<html><br><center><b>GOAL</b> Agent Programming <br>"
				+ "Build Id: " //$NON-NLS-1$
				+ p.getImplementationVersion()
				+ "<br><br>" //$NON-NLS-1$
				+ "Visit " //$NON-NLS-1$
				+ "<a href=\"http://ii.tudelft.nl/trac/goal\">http://ii.tudelft.nl/trac/goal</a>" //$NON-NLS-1$
				+ "<br><br>" //$NON-NLS-1$
				+ "Contact us at: <a href = mailto:goal@ii.tudelft.nl>goal@ii.tudelft.nl</a>" //$NON-NLS-1$
				+ "<br><br> </center>" + "</html>"; //$NON-NLS-1$ //$NON-NLS-2$

		JEditorPane editorPane = new JEditorPane("text/html", text); //$NON-NLS-1$
		editorPane.setEditable(false);
		add(editorPane, BorderLayout.CENTER);
		editorPane.addHyperlinkListener(new myHyperlinkListener());
		// add(new JLabel(text), BorderLayout.CENTER);

		// TODO: center close button, now has its own panel to avoid very
		// stretched button...
		JButton close = new JButton("Close"); //$NON-NLS-1$
		close.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				dispose();
			}
		});
		JPanel closepanel = new JPanel(new BorderLayout());
		closepanel.add(close, BorderLayout.EAST);
		add(closepanel, BorderLayout.SOUTH);
		pack();
		setLocationRelativeTo(parent);
		setVisible(true);
	}
}

class myHyperlinkListener implements HyperlinkListener {
	@Override
	public void hyperlinkUpdate(HyperlinkEvent e) {
		if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
			try {
				Desktop.getDesktop().browse(new URI(e.getURL().toString()));
			} catch (Exception e1) {
				new Warning(Resources.get(WarningStrings.FAILED_BROWSER_OPEN),
						e1);
			}
		}

	}
}
