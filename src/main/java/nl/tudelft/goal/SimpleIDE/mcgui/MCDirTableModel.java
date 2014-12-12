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

import goal.tools.mc.core.Directives;

import javax.swing.table.AbstractTableModel;

@SuppressWarnings("serial")
public class MCDirTableModel extends AbstractTableModel {

	private final String[] directives = { "PROP_ON_THE_FLY", "PROG_ON_THE_FLY",
			"PRINT_TREE",
			// "SHORTEST",
			"EXPLICIT_GC", "SLICING", "POR" };
	private final String[] columnNames = { "Directive", "Value" };
	private final Object[][] tableContent = new Object[directives.length][2];
	public final String[] longValues = new String[2];

	public MCDirTableModel() {
		longValues[0] = new String(columnNames[0]);
		longValues[1] = new String(columnNames[1]);
		for (int i = 0; i < directives.length; i++) {
			tableContent[i][0] = directives[i];
			tableContent[i][1] = Directives.get(directives[i]);
		}
	}

	@Override
	public int getColumnCount() {
		return columnNames.length;
	}

	@Override
	public int getRowCount() {
		return tableContent.length;
	}

	@Override
	public String getColumnName(int col) {
		return columnNames[col];
	}

	public boolean getValueOf(String s) {
		for (int i = 0; i < directives.length; i++) {
			if (s.equals(directives[i])) {
				return (Boolean) tableContent[i][1];
			}
		}
		return false;
	}

	@Override
	public Object getValueAt(int row, int col) {
		return tableContent[row][col];
	}

	@Override
	public Class<?> getColumnClass(int c) {
		return getValueAt(0, c).getClass();
	}

	@Override
	public boolean isCellEditable(int row, int col) {
		return col > 0;
	}

	@Override
	public void setValueAt(Object value, int row, int col) {
		tableContent[row][col] = value;
		fireTableCellUpdated(row, col);
	}
}
