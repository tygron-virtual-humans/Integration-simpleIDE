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

package nl.tudelft.goal.SimpleIDE.mcgui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import nl.tudelft.goal.SimpleIDE.IDEfunctionality;

@SuppressWarnings("serial")
public class MCFrame extends JFrame implements ActionListener {

	public static void main(String[] args) {
		@SuppressWarnings("unused")
		MCFrame mcframe = new MCFrame(null);
	}

	public MCFrame(IDEfunctionality IDE) {
		super("GOAL Model Checker");
		setResizable(false);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setLocationRelativeTo(IDE.getMainPanel());
		setLayout(new BorderLayout());
		add(new MCPanel(IDE, this));
		pack();
		setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		dispose();
	}
}
