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

package nl.tudelft.goal.SimpleIDE.prefgui;

import goal.preferences.CorePreferences;
import goal.preferences.EnvironmentPreferences;
import goal.preferences.PMPreferences;
import goal.tools.errorhandling.exceptions.GOALCommandCancelledException;
import goal.tools.errorhandling.exceptions.GOALUserError;
import goalhub.krTools.KRFactory;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import nl.tudelft.goal.SimpleIDE.SimpleIDE;
import nl.tudelft.goal.SimpleIDE.preferences.IDEPreferences;

/**
 * The panel is a GUI for the user to edit IDE preferences. We set the
 * preferences through {@link goal.preferences.PMPreferences}.
 *
 * @author W.Pasman 24mar09
 * @modified V.Koeman 12jun13 refactoring preferences: management separated from
 *           display through new class
 * @modified K.Hindriks Layout and naming.
 */
@SuppressWarnings("serial")
public class RuntimePrefPanel extends JPanel implements ActionListener,
ChangeListener {

	/**
	 *
	 */
	private static final long serialVersionUID = 9138030922232947696L;
	// Debugging.
	// private JCheckBox autoSwitchTab; // automatically switch to debug tab of
	// // launched
	private final JCheckBox openDebugTraceTabAtAgentLaunch = new JCheckBox(
			"Open debug trace tab when agent is launched");
	private final JCheckBox prefixAgentNamesWithMASName = new JCheckBox(
			"Prefix agent names with MAS name");

	// Middleware section.
	private final JCheckBox alwaysMiddlewareLocal = new JCheckBox(
			"Always run the middleware locally");

	// High Performance section.
	private final JCheckBox sleepRepeatingAgent = new JCheckBox(
			"Sleep agents when they do no actions");

	private final JCheckBox removeKilledAgent = new JCheckBox(
			"Remove agents when they are killed");

	private final JSpinner threadPoolSize = new JSpinner();

	// KR section.
	private final JComboBox defaultkrlanguagebox;

	// Environment section.
	private final JCheckBox printEntities = new JCheckBox(
			"print message when new entity appears");

	private final JCheckBox agentCopyEnvRunState = new JCheckBox(
			"new agents copy the environment run state (or run if no environment available)");

	// Learning section.
	private final JCheckBox enablelearn = new JCheckBox("enable learning");
	private final JTextField agentsbrowsedir = new JTextField("/");
	private final JLabel agentsBrowseDirExplanation = new JLabel(
			"Use this learned-behaviour file:");
	private final JButton agentBrowseButton = new JButton("Browse...");

	/**
	 * Creates the Platform and Runtime tab in the GOAL Preferences panel.
	 */
	public RuntimePrefPanel() {

		// Auto-switch to Debug Tracer
		// autoSwitchTab = new JCheckBox("Automatically switch to Debug Tracer "
		// + "tab in feedback area when agent is launched.");

		// Panel to select default KR language.
		Vector<String> langs = new Vector<String>(
				KRFactory.getSupportedInterfaces());
		this.defaultkrlanguagebox = new JComboBox(langs);

		JPanel languageselpanel = new JPanel(new BorderLayout());
		languageselpanel.add(
				PreferencesPanel.getBoldFontJLabel("Default KR language: "),
				BorderLayout.WEST);
		languageselpanel.add(this.defaultkrlanguagebox, BorderLayout.CENTER);

		// Layout items related to learning section of panel.
		final RuntimePrefPanel thispanel = this;

		this.agentBrowseButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				try {
					File newFile = SimpleIDE.askFile(thispanel, true,
							"Select learned behaviour file",
							JFileChooser.FILES_ONLY, ".lrn", null,
							CorePreferences.getLearnFile(), true);
					if (newFile != null) {
						RuntimePrefPanel.this.agentsbrowsedir.setText(newFile
								.getAbsolutePath());
						thispanel.actionPerformed(null);
					}
				} catch (GOALCommandCancelledException e) {
					// user cancelled the action. ignore.
				} catch (GOALUserError e) {
					// not much else we can do here. We can not use GOAL
					// exceptions
					System.out.println("browse failed:" + e);
				}
			}
		});

		// panel fragment for agent browsepath
		JPanel agentbrowsepanel = new JPanel(new BorderLayout());
		agentbrowsepanel.add(this.agentsbrowsedir, BorderLayout.CENTER);
		agentbrowsepanel.add(this.agentBrowseButton, BorderLayout.EAST);

		// Initialize settings of check boxes etc.
		initSettings();

		// Define layout.
		setLayout(new GridLayout(0, 1));

		// Debugging section.
		add(PreferencesPanel.getBoldFontJLabel("Debugging"));
		// add(autoSwitchTab);
		// autoSwitchTab.addChangeListener(this);
		add(this.openDebugTraceTabAtAgentLaunch);
		this.openDebugTraceTabAtAgentLaunch.addChangeListener(this);
		add(this.prefixAgentNamesWithMASName);
		this.prefixAgentNamesWithMASName.addChangeListener(this);

		// KR language section.
		add(new JPopupMenu.Separator());
		add(languageselpanel);

		// Performance section.
		add(new JPopupMenu.Separator());
		add(PreferencesPanel.getBoldFontJLabel("High Performance"));
		add(this.sleepRepeatingAgent);
		add(this.removeKilledAgent);
		JPanel threadpoolpanel = new JPanel(new BorderLayout());
		threadpoolpanel.add(new JLabel("Agent thread pool size"),
				BorderLayout.CENTER);
		threadpoolpanel.add(this.threadPoolSize, BorderLayout.WEST);
		add(threadpoolpanel);

		// Environment section.
		add(new JPopupMenu.Separator());
		add(PreferencesPanel.getBoldFontJLabel("Environment"));
		add(this.printEntities);
		add(this.agentCopyEnvRunState);

		// Middleware section.
		add(new JPopupMenu.Separator());
		add(PreferencesPanel.getBoldFontJLabel("Hosting"));
		add(this.alwaysMiddlewareLocal);

		// Learning section.
		add(new JPopupMenu.Separator());
		add(PreferencesPanel.getBoldFontJLabel("Learning"));
		add(this.enablelearn);
		add(this.agentsBrowseDirExplanation);
		add(agentbrowsepanel);

		this.enablelearn.addActionListener(this);
		this.defaultkrlanguagebox.addActionListener(this);
		this.sleepRepeatingAgent.addActionListener(this);
		this.alwaysMiddlewareLocal.addActionListener(this);
		this.removeKilledAgent.addActionListener(this);
		this.printEntities.addActionListener(this);
		this.agentCopyEnvRunState.addActionListener(this);
		this.threadPoolSize.addChangeListener(this);
	}

	/**
	 * Copies settings from preferences to the check boxes
	 */
	private void initSettings() {
		// autoSwitchTab.setSelected( IDEPreferences.getAutoSwitchDebugTab() );
		this.openDebugTraceTabAtAgentLaunch.setSelected(IDEPreferences
				.getOpenDebugTraceTabAtAgentLaunch());
		this.alwaysMiddlewareLocal.setSelected(PMPreferences
				.getAlwaysMiddlewareLocal());
		this.defaultkrlanguagebox.setSelectedItem(PMPreferences
				.getDefaultKRInterface());
		this.sleepRepeatingAgent.setSelected(PMPreferences
				.getSleepRepeatingAgent());
		this.removeKilledAgent
				.setSelected(PMPreferences.getRemoveKilledAgent());
		this.printEntities.setSelected(EnvironmentPreferences
				.getPrintEntities());
		this.agentCopyEnvRunState.setSelected(PMPreferences
				.getAgentCopyEnvRunState());
		this.enablelearn.setSelected(CorePreferences.isLearning());
		this.agentsbrowsedir.setText(CorePreferences.getLearnFile());
		this.prefixAgentNamesWithMASName.setSelected(PMPreferences
				.getUseMASNameAsAgentPrefix());
		this.threadPoolSize.setModel(new SpinnerNumberModel(PMPreferences
				.getThreadPoolSize(), 0, 99, 1));
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		PMPreferences.setAlwaysMiddlewareLocal(this.alwaysMiddlewareLocal
				.isSelected());
		PMPreferences.setDefaultKRInterface((String) this.defaultkrlanguagebox
				.getSelectedItem());
		PMPreferences.setSleepRepeatingAgent(this.sleepRepeatingAgent
				.isSelected());
		PMPreferences.setRemoveKilledAgent(this.removeKilledAgent.isSelected());
		EnvironmentPreferences
				.setPrintEntities(this.printEntities.isSelected());
		PMPreferences.setAgentCopyEnvRunState(this.agentCopyEnvRunState
				.isSelected());
		PMPreferences.setThreadPoolSize((Integer) this.threadPoolSize
				.getValue());
		CorePreferences.setLearning(this.enablelearn.isSelected());
		CorePreferences.setLearnFile(this.agentsbrowsedir.getText());
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		// IDEPreferences.setAutoSwitchDebugTab( autoSwitchTab.isSelected() );
		IDEPreferences
		.setOpenDebugTraceTabAtAgentLaunch(this.openDebugTraceTabAtAgentLaunch
				.isSelected());
		PMPreferences
				.setPrefixAgentNameWithMASName(this.prefixAgentNamesWithMASName
						.isSelected());
	}
}