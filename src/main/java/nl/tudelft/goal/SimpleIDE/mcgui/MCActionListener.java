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

import goal.preferences.PMPreferences;
import goal.tools.errorhandling.exceptions.GOALCommandCancelledException;
import goal.tools.errorhandling.exceptions.GOALUserError;
import goal.tools.mc.core.ActionListener;
import goal.tools.mc.core.Controller;
import goal.tools.mc.core.Directives;
import goal.util.Extension;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import nl.tudelft.goal.SimpleIDE.SimpleIDE;

public class MCActionListener implements ActionListener,
		java.awt.event.ActionListener {

	private final MCPanel prnt;
	private Thread mcthread;

	public MCActionListener(MCPanel parent) {
		super();
		this.prnt = parent;
	}

	@Override
	public void actionPerformed(ActionEvent event) {

		/* Cancel -- Terminates application */
		if (event.getActionCommand().equals("cancel")) {
			prnt.dispose();
			return;
		}

		/* Add agent -- Adds an agent to the list of agents */
		if (event.getActionCommand().equals("addAgent")) {
			File file = null;
			try {
				file = SimpleIDE.askFile(prnt, true, "Add Agent",
						JFileChooser.FILES_ONLY, Extension.GOAL.getExtension(),
						"", PMPreferences.getAgentBrowsePath(), true);
			} catch (GOALCommandCancelledException e) {
				file = null; // FIXME hack
			} catch (GOALUserError e) {
				file = null; // FIXME hack
			}
			if (file != null) {
				String path = file.getAbsolutePath();
				PMPreferences.setAgentBrowsePath(path);
				String name = path
						.substring(path.lastIndexOf(File.separator) + 1);
				DefaultListModel defaultListModel = new DefaultListModel();
				for (int i = 0; i < prnt.agentList.getModel().getSize(); i++) {
					defaultListModel.add(i, prnt.agentList.getModel()
							.getElementAt(i));
				}
				int index = defaultListModel.size();
				defaultListModel.add(index, new AgentListEntry(path, name));
				prnt.agentList.setModel(defaultListModel);
				prnt.agentList.setSelectedIndex(index);
				return;
			}
		}

		/* Start -- Commence verification */
		if (event.getActionCommand().equals("start")) {

			/* Get agent */
			AgentListEntry entry = (AgentListEntry) prnt.agentList
					.getSelectedValue();
			if (entry == null) {
				showError("Please select an agent from the list above, or "
						+ "add one to the list by clicking the \"Add Agent\" "
						+ "button.");
				return;
			}
			String pString = entry.path;

			/* Get property */
			String fString = prnt.propertyTextField.getText();
			if (fString == null || fString.equals("")) {
				showError("Could not parse the provided property. Please "
						+ "rephrase.");
				return;
			}

			/* Set directives */
			MCDirTableModel model = (MCDirTableModel) prnt.dirsTable.getModel();
			Directives.PROP_ON_THE_FLY = model.getValueOf("PROP_ON_THE_FLY");
			Directives.PROG_ON_THE_FLY = model.getValueOf("PROG_ON_THE_FLY");
			Directives.PRINT_TREE = model.getValueOf("PRINT_TREE");
			Directives.EXPLICIT_GC = model.getValueOf("EXPLICIT_GC");
			Directives.SLICING = model.getValueOf("SLICING");
			Directives.POR = model.getValueOf("POR");

			try {

				/* Initalize controller */
				Controller cont = new Controller(fString, pString);

				/* Pre-checks */
				List<String> messages = cont.preCheck();
				boolean proceed = true;
				if (!messages.isEmpty()) {
					proceed = showWarnings(messages);
				}

				/* Commence model checking if all pre-checks succeed */
				if (proceed) {
					prnt.startButton.setText("Stop");
					prnt.startButton.setActionCommand("stop");
					cont.setListener(this);
					// mcthread = new Thread(cont);
					// mcthread.start();
					cont.run();// #2374. Lock the GUI till done.
					cont.dispose();
				}
				return;

			} catch (Exception e) {
				e.printStackTrace();
				showError(e.getMessage());
				return;
			}
		}

		/* Start -- Terminate verification */
		if (event.getActionCommand().equals("stop")) {
			if (mcthread != null) {
				mcthread.interrupt();
				mcthread = null;
			}
			prnt.startButton.setText("Start");
			prnt.startButton.setActionCommand("start");
		}
	}

	private void showError(String message) {

		try {

			/* Create text area */
			JTextArea text = new JTextArea();
			text.setLineWrap(true);
			text.setWrapStyleWord(true);
			text.setEditable(false);
			text.setText(message);

			/* Create scroll pane to put text area in */
			JScrollPane scroll = new JScrollPane(text);
			scroll.setAutoscrolls(true);
			scroll.setPreferredSize(new Dimension(200, 150));
			scroll.setSize(new Dimension(200, 150));

			/* Show message */
			JOptionPane.showMessageDialog(prnt, scroll, "Error",
					JOptionPane.ERROR_MESSAGE);
		}

		catch (Exception e) {
			e.printStackTrace();
		}

	}

	private boolean showWarnings(List<String> messages) {

		try {

			/* Create text area */
			JTextArea text = new JTextArea();
			text.setLineWrap(true);
			text.setWrapStyleWord(true);
			text.setEditable(false);

			/* Create scroll pane to put text area in */
			JScrollPane scroll = new JScrollPane(text);
			scroll.setAutoscrolls(true);
			scroll.setPreferredSize(new Dimension(200, 150));
			scroll.setSize(new Dimension(200, 150));

			/* Options */
			Object[] options = { "Abort", "Proceed" };

			/* Display messages */
			int i = 1;
			for (String message : messages) {
				text.setText(message);
				int option = JOptionPane.showOptionDialog(prnt, scroll,
						"Warning [" + i + "/" + messages.size() + "]",
						JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE,
						null, options, options[0]);
				i++;
				if (option == 0) {
					return false;
				}
			}
			return true;
		}

		catch (Exception e) {
			e.printStackTrace();
			return false;
		}

	}
}