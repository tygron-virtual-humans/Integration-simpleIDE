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

import goal.tools.mc.property.ltl.Formula;

import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.Font;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.tree.TreeNode;

import nl.tudelft.goal.SimpleIDE.IDENode;
import nl.tudelft.goal.SimpleIDE.IDEfunctionality;
import nl.tudelft.goal.SimpleIDE.NodeType;
import nl.tudelft.goal.SimpleIDE.files.FileNode;

@SuppressWarnings("serial")
public class MCPanel extends JPanel {

	MCActionListener mcal;
	JLabel agentLabel, propertyLabel, parsedPropertyLabel;
	JButton cancelButton, addAgentButton, startButton;
	JScrollPane agentScrollPanel;
	JList agentList;
	JTextField propertyTextField;
	JTable dirsTable;

	private final IDEfunctionality IDE;
	private final MCFrame prnt;

	public MCPanel(IDEfunctionality IDE, MCFrame parent) {
		super();
		this.prnt = parent;
		this.IDE = IDE;
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		mcal = new MCActionListener(this);

		/* Agent label */
		agentLabel = new JLabel("Agent:");
		agentLabel.setAlignmentX(LEFT_ALIGNMENT);
		add(agentLabel);

		/* Agent scroll pane and list */
		List<Object> agentNames = IDE == null ? new ArrayList<Object>()
				: getAgentsFromIDE();
		agentList = new JList(agentNames.toArray());
		agentList.setAlignmentX(LEFT_ALIGNMENT);
		agentList.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		agentScrollPanel = new JScrollPane(agentList);
		agentScrollPanel.setAlignmentX(LEFT_ALIGNMENT);
		agentScrollPanel.setPreferredSize(new Dimension(300, 80));
		agentScrollPanel
				.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		add(agentScrollPanel);

		/* Property label */
		propertyLabel = new JLabel("Property:");
		propertyLabel.setAlignmentX(LEFT_ALIGNMENT);
		add(propertyLabel);

		/* Property text field */
		propertyTextField = new JTextField("");
		propertyTextField.setAlignmentX(LEFT_ALIGNMENT);
		DocumentListener documentListener = new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent e) {
				parse();
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				parse();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				parse();
			}

			private void parse() {
				String s = propertyTextField.getText();
				Formula property = null;
				try {
					property = Formula.parse(s);
				} catch (Exception e) {
					parsedPropertyLabel.setText("<html>" + e + "</html>");
				}
				if (property != null) {
					parsedPropertyLabel.setText(""
							+ "<html>Parsed property:<br>"
							+ property.toString() + "</html>");
				}
			}
		};
		propertyTextField.getDocument().addDocumentListener(documentListener);
		add(propertyTextField);

		/* Parsed property label */
		parsedPropertyLabel = new JLabel("<html>Parsed property:"
				+ "<br>No valid parse</html>");
		parsedPropertyLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 10));
		add(parsedPropertyLabel);

		/* Directives label */
		JLabel directivesLabel = new JLabel("Directives:");
		add(directivesLabel);

		/* Directives table */
		dirsTable = new JTable(new MCDirTableModel());
		dirsTable.setAlignmentX(LEFT_ALIGNMENT);
		dirsTable.setCellSelectionEnabled(false);
		dirsTable.getColumnModel().getColumn(0).setPreferredWidth(290);
		dirsTable.getColumnModel().getColumn(1).setPreferredWidth(10);
		int height = dirsTable.getRowCount() * dirsTable.getRowHeight();
		dirsTable.setPreferredSize(new Dimension(300, height));
		add(dirsTable);
		add(new JComponent() {
		});

		/* Control panel */
		JPanel controlPanel = new JPanel();
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(mcal);
		cancelButton.setActionCommand("cancel");
		addAgentButton = new JButton("Add Agent");
		addAgentButton.addActionListener(mcal);
		addAgentButton.setActionCommand("addAgent");
		startButton = new JButton("Start");
		startButton.addActionListener(mcal);
		startButton.setActionCommand("start");
		controlPanel.setAlignmentX(LEFT_ALIGNMENT);
		controlPanel.add(cancelButton);
		controlPanel.add(addAgentButton);
		controlPanel.add(startButton);
		add(controlPanel);
	}

	public void dispose() {
		prnt.dispose();
	}

	//
	// Private methods
	//

	private List<Object> getAgentsFromIDE() {

		try {
			ArrayList<Object> agentNames = new ArrayList<Object>();
			String path;
			String name;
			IDENode selectedNode = IDE.getSelectedNodes().get(0); // FIXME hack
			switch (selectedNode.getType()) {
			case MODFILE:
			case PLFILE:
				while (selectedNode.getType() != NodeType.GOALFILE) {
					selectedNode = (IDENode) selectedNode.getParent();
				}
				// deliberate fall-through
			case GOALFILE:
				path = ((FileNode) selectedNode).getFilename();
				name = new File(path).getName();
				agentNames.add(new AgentListEntry(path, name));
				break;
			case MASFILE:
				FileNode masNode = (FileNode) selectedNode;
				for (int i = 0; i < masNode.getChildCount(); i++) {
					TreeNode treeNode = masNode.getChildAt(i);
					if (treeNode instanceof FileNode) {
						FileNode fileNode = (FileNode) treeNode;
						if (fileNode.getType() == NodeType.GOALFILE) {
							path = fileNode.getFilename();
							name = path.substring(path
									.lastIndexOf(File.separator) + 1);
							agentNames.add(new AgentListEntry(path, name));
						}
					}
				}
				break;
			case ROOT:
				break;
			}
			return agentNames;
		}

		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}

final class AgentListEntry {
	final String path;
	final String name;

	public AgentListEntry(String path, String name) {
		this.path = path;
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}
}
