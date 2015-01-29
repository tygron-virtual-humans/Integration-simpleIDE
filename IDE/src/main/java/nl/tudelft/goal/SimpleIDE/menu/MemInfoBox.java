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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;

/**
 * Show current memory usage.
 *
 * @author W.Pasman 11feb09
 */
@SuppressWarnings("serial")
public class MemInfoBox extends JFrame {

	/**
	 *
	 */
	private static final long serialVersionUID = 2281513261935781259L;
	static final double MM = 1. / (1024. * 1024.); // CHECK using /MM gives
	// strange
	private final Component theParent;

	public MemInfoBox(Component parent) {

		// CHECK can we use a JOptionPane.dialog here? Could simplify the code.
		this.theParent = parent;
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setLocationRelativeTo(parent);
		setLayout(new BorderLayout());

		System.gc();

		String text = "<html><br>" + "Free memory: " //$NON-NLS-1$
				+ String.format("%1.2f", Runtime.getRuntime().freeMemory() * MM) //$NON-NLS-1$
				+ "  Mb<br><br>" + "JVM memory limit: " //$NON-NLS-1$ //$NON-NLS-2$
				+ String.format("%1.2f", Runtime.getRuntime().maxMemory() * MM) //$NON-NLS-1$
				+ " Mb<br><br>" + "</html>"; //$NON-NLS-1$ //$NON-NLS-2$
		add(new JLabel(text), BorderLayout.CENTER);

		// TODO: center close button, now has its own panel to avoid very
		// stretched button...
		JButton close = new JButton("Close"); //$NON-NLS-1$
		close.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				dispose();
			}
		});

		JButton details = new JButton("Details"); //$NON-NLS-1$
		details.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				showDetailsWindow();
			}
		});
		JPanel closepanel = new JPanel(new BorderLayout());
		closepanel.add(close, BorderLayout.EAST);
		closepanel.add(details, BorderLayout.WEST);
		add(closepanel, BorderLayout.SOUTH);
		pack();
		setVisible(true);
	}

	/**
	 * Shows a detail window with all the nasty memory details from
	 * dumpMemoryInfo
	 */
	private void showDetailsWindow() {
		JScrollPane infopane = new JScrollPane(new JTextArea(
				getMemoryInfoString(), 20, 40));
		JOptionPane.showMessageDialog(this.theParent, infopane);
	}

	/**
	 * TODO http://www.informit.com/guides/content.aspx?g=java&seqNum=249&rll=1
	 *
	 * @return all the memory details from the memory manager, as a big string
	 *         with newlines.
	 */
	public static String getMemoryInfoString() {
		String info = ""; //$NON-NLS-1$
		try {
			info += "\nDETAILED MEMORY INFO\n"; //$NON-NLS-1$
			// Read MemoryMXBean
			MemoryMXBean memorymbean = ManagementFactory.getMemoryMXBean();
			info += "Heap Memory Usage: " + "\n" //$NON-NLS-1$ //$NON-NLS-2$
					+ memorymbean.getHeapMemoryUsage();
			info += "Non-Heap Memory Usage: " //$NON-NLS-1$
					+ memorymbean.getNonHeapMemoryUsage() + "\n"; //$NON-NLS-1$

			// Read Garbage Collection information
			List<GarbageCollectorMXBean> gcmbeans = ManagementFactory
					.getGarbageCollectorMXBeans();
			for (GarbageCollectorMXBean gcmbean : gcmbeans) {
				info += "\nName: " + gcmbean.getName() + "\n"; //$NON-NLS-1$ //$NON-NLS-2$
				info += "Collection count: " + gcmbean.getCollectionCount() //$NON-NLS-1$
						+ "\n"; //$NON-NLS-1$
				info += "Collection time: " + gcmbean.getCollectionTime() //$NON-NLS-1$
						+ "\n"; //$NON-NLS-1$
				info += "Memory Pools: " + "\n"; //$NON-NLS-1$ //$NON-NLS-2$
				String[] memoryPoolNames = gcmbean.getMemoryPoolNames();
				for (String memoryPoolName : memoryPoolNames) {
					info += "\t" + memoryPoolName + "\n"; //$NON-NLS-1$ //$NON-NLS-2$
				}
			}

			// Read Memory Pool Information
			info += "\nMemory Pools Info\n"; //$NON-NLS-1$
			List<MemoryPoolMXBean> mempoolsmbeans = ManagementFactory
					.getMemoryPoolMXBeans();
			for (MemoryPoolMXBean mempoolmbean : mempoolsmbeans) {
				info += "\nName: " + mempoolmbean.getName() + "\n"; //$NON-NLS-1$ //$NON-NLS-2$
				info += "Usage: " + mempoolmbean.getUsage() + "\n"; //$NON-NLS-1$ //$NON-NLS-2$
				info += "Collection Usage: " //$NON-NLS-1$
						+ mempoolmbean.getCollectionUsage() + "\n"; //$NON-NLS-1$
				info += "Peak Usage: " + mempoolmbean.getPeakUsage() + "\n"; //$NON-NLS-1$ //$NON-NLS-2$
				info += "Type: " + mempoolmbean.getType() + "\n"; //$NON-NLS-1$ //$NON-NLS-2$
				info += "Memory Manager Names: \n"; //$NON-NLS-1$
				String[] memManagerNames = mempoolmbean.getMemoryManagerNames();
				for (String memManagerName : memManagerNames) {
					info += "\t" + memManagerName + "\n"; //$NON-NLS-1$ //$NON-NLS-2$
				}
				info += "\n"; //$NON-NLS-1$
			}
		} catch (Exception e) {
			new Warning(Resources.get(WarningStrings.FAILED_MEMINFO_CREATE), e);
		}
		return info;
	}
}
