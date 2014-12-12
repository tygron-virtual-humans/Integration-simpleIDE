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

	// Debugging.
	// private JCheckBox autoSwitchTab; // automatically switch to debug tab of
	// // launched
	private JCheckBox openDebugTraceTabAtAgentLaunch = new JCheckBox(
			"Open debug trace tab when agent is launched");
	private JCheckBox prefixAgentNamesWithMASName = new JCheckBox(
			"Prefix agent names with MAS name");

	// Middleware section.
	private JCheckBox alwaysMiddlewareLocal = new JCheckBox(
			"Always run the middleware locally");

	// High Performance section.
	private JCheckBox sleepRepeatingAgent = new JCheckBox(
			"Sleep agents when they do no actions");

	private JCheckBox removeKilledAgent = new JCheckBox(
			"Remove agents when they are killed");

	private JSpinner threadPoolSize = new JSpinner();

	// KR section.
	private JComboBox defaultkrlanguagebox;

	// Environment section.
	private JCheckBox printEntities = new JCheckBox(
			"print message when new entity appears");

	private JCheckBox agentCopyEnvRunState = new JCheckBox(
			"new agents copy the environment run state (or run if no environment available)");

	// Learning section.
	private JCheckBox enablelearn = new JCheckBox("enable learning");
	private JTextField agentsbrowsedir = new JTextField("/");
	private JLabel agentsBrowseDirExplanation = new JLabel(
			"Use this learned-behaviour file:");
	private JButton agentBrowseButton = new JButton("Browse...");

	/**
	 * Creates the Platform and Runtime tab in the GOAL Preferences panel.
	 */
	public RuntimePrefPanel() {

		// Auto-switch to Debug Tracer
		// autoSwitchTab = new JCheckBox("Automatically switch to Debug Tracer "
		// + "tab in feedback area when agent is launched.");

		// Panel to select default KR language.
		Vector<String> langs = new Vector<String>(
				goal.core.kr.KRFactory.getLanguages());
		defaultkrlanguagebox = new JComboBox(langs);

		JPanel languageselpanel = new JPanel(new BorderLayout());
		languageselpanel.add(
				PreferencesPanel.getBoldFontJLabel("Default KR language: "),
				BorderLayout.WEST);
		languageselpanel.add(defaultkrlanguagebox, BorderLayout.CENTER);

		// Layout items related to learning section of panel.
		final RuntimePrefPanel thispanel = this;

		agentBrowseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				try {
					File newFile = SimpleIDE.askFile(thispanel, true,
							"Select learned behaviour file",
							JFileChooser.FILES_ONLY, ".lrn", null,
							CorePreferences.getLearnFile(), true);
					if (newFile != null) {
						agentsbrowsedir.setText(newFile.getAbsolutePath());
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
		agentbrowsepanel.add(agentsbrowsedir, BorderLayout.CENTER);
		agentbrowsepanel.add(agentBrowseButton, BorderLayout.EAST);

		// Initialize settings of check boxes etc.
		initSettings();

		// Define layout.
		setLayout(new GridLayout(0, 1));

		// Debugging section.
		add(PreferencesPanel.getBoldFontJLabel("Debugging"));
		// add(autoSwitchTab);
		// autoSwitchTab.addChangeListener(this);
		add(openDebugTraceTabAtAgentLaunch);
		openDebugTraceTabAtAgentLaunch.addChangeListener(this);
		add(prefixAgentNamesWithMASName);
		prefixAgentNamesWithMASName.addChangeListener(this);

		// KR language section.
		add(new JPopupMenu.Separator());
		add(languageselpanel);

		// Performance section.
		add(new JPopupMenu.Separator());
		add(PreferencesPanel.getBoldFontJLabel("High Performance"));
		add(sleepRepeatingAgent);
		add(removeKilledAgent);
		JPanel threadpoolpanel = new JPanel(new BorderLayout());
		threadpoolpanel.add(new JLabel("Agent thread pool size"),BorderLayout.CENTER);
		threadpoolpanel.add(threadPoolSize, BorderLayout.WEST);
		add(threadpoolpanel);

		// Environment section.
		add(new JPopupMenu.Separator());
		add(PreferencesPanel.getBoldFontJLabel("Environment"));
		add(printEntities);
		add(agentCopyEnvRunState);

		// Middleware section.
		add(new JPopupMenu.Separator());
		add(PreferencesPanel.getBoldFontJLabel("Hosting"));
		add(alwaysMiddlewareLocal);

		// Learning section.
		add(new JPopupMenu.Separator());
		add(PreferencesPanel.getBoldFontJLabel("Learning"));
		add(enablelearn);
		add(agentsBrowseDirExplanation);
		add(agentbrowsepanel);

		enablelearn.addActionListener(this);
		defaultkrlanguagebox.addActionListener(this);
		sleepRepeatingAgent.addActionListener(this);
		alwaysMiddlewareLocal.addActionListener(this);
		removeKilledAgent.addActionListener(this);
		printEntities.addActionListener(this);
		agentCopyEnvRunState.addActionListener(this);
		threadPoolSize.addChangeListener(this);
	}

	/**
	 * Copies settings from preferences to the check boxes
	 */
	private void initSettings() {
		// autoSwitchTab.setSelected( IDEPreferences.getAutoSwitchDebugTab() );
		openDebugTraceTabAtAgentLaunch.setSelected(IDEPreferences
				.getOpenDebugTraceTabAtAgentLaunch());
		alwaysMiddlewareLocal.setSelected(PMPreferences
				.getAlwaysMiddlewareLocal());
		defaultkrlanguagebox.setSelectedItem(PMPreferences
				.getDefaultKRLanguage());
		sleepRepeatingAgent.setSelected(PMPreferences.getSleepRepeatingAgent());
		removeKilledAgent.setSelected(PMPreferences.getRemoveKilledAgent());
		printEntities.setSelected(EnvironmentPreferences.getPrintEntities());
		agentCopyEnvRunState.setSelected(PMPreferences
				.getAgentCopyEnvRunState());
		enablelearn.setSelected(CorePreferences.isLearning());
		agentsbrowsedir.setText(CorePreferences.getLearnFile());
		prefixAgentNamesWithMASName.setSelected(PMPreferences
				.getUseMASNameAsAgentPrefix());
		threadPoolSize.setModel(new SpinnerNumberModel(PMPreferences.getThreadPoolSize(),0,99,1));
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		PMPreferences.setAlwaysMiddlewareLocal(alwaysMiddlewareLocal
				.isSelected());
		PMPreferences.setDefaultKRLanguage((String) defaultkrlanguagebox
				.getSelectedItem());
		PMPreferences.setSleepRepeatingAgent(sleepRepeatingAgent.isSelected());
		PMPreferences.setRemoveKilledAgent(removeKilledAgent.isSelected());
		EnvironmentPreferences.setPrintEntities(printEntities.isSelected());
		PMPreferences
				.setAgentCopyEnvRunState(agentCopyEnvRunState.isSelected());
		PMPreferences.setThreadPoolSize((Integer)threadPoolSize.getValue());
		CorePreferences.setLearning(enablelearn.isSelected());
		CorePreferences.setLearnFile(agentsbrowsedir.getText());
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		// IDEPreferences.setAutoSwitchDebugTab( autoSwitchTab.isSelected() );
		IDEPreferences
				.setOpenDebugTraceTabAtAgentLaunch(openDebugTraceTabAtAgentLaunch
						.isSelected());
		PMPreferences.setPrefixAgentNameWithMASName(prefixAgentNamesWithMASName
				.isSelected());
	}
}