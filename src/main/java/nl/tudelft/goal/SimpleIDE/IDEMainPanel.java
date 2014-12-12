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

package nl.tudelft.goal.SimpleIDE;

import goal.tools.errorhandling.exceptions.GOALCommandCancelledException;
import goal.tools.errorhandling.exceptions.GOALException;
import goal.tools.errorhandling.exceptions.GOALIncompleteGUIUsageException;
import goal.tools.errorhandling.exceptions.GOALUserError;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JSplitPane;

import nl.tudelft.goal.SimpleIDE.preferences.IDEPreferences;

/**
 * The main GUI of the IDE.
 * 
 */
@SuppressWarnings("serial")
public class IDEMainPanel extends JPanel {

	private JSplitPane pane, tophalf;

	private JPanel fileProcessPanel; // the top level panel, showing either edit
										// or debug
	private FilePanel filePanel;
	private ProcessPanel processPanel;

	private JPanel editDebugPanel;
	private DebugPanel debugPanel; // visible in run mode

	private FeedbackPanel feedbackPanel; // lower part of IDE that shows console
											// output and other tabs with
											// feedback

	private SimpleIDE theIDE;
	private static final String EDIT_VIEW_STR = "EDITVIEW";
	private static final String DEBUG_VIEW_STR = "DEBUGVIEW";
	public static final int EDIT_VIEW = 0;
	public static final int DEBUG_VIEW = 1;

	/*
	 * minimum size of the feedback area.
	 */
	private static final int MIN_WIDTH = 60;
	private static final int MIN_HEIGHT = 40;

	/**
	 * @param theIDE
	 *            is the SimpleIDE implementation which actually handles the
	 *            user's requests.
	 */
	public IDEMainPanel(SimpleIDE ide) throws InstantiationException,
			IllegalAccessException {
		CardLayout cl;

		theIDE = ide;

		setLayout(new BorderLayout());

		// add center pane
		fileProcessPanel = new JPanel(new CardLayout());
		filePanel = new FilePanel(this, theIDE);
		processPanel = new ProcessPanel(theIDE, theIDE);
		fileProcessPanel.add(filePanel, EDIT_VIEW_STR);
		fileProcessPanel.add(processPanel, DEBUG_VIEW_STR);
		cl = (CardLayout) fileProcessPanel.getLayout();
		cl.show(fileProcessPanel, EDIT_VIEW_STR);

		// add edit and debug panel
		editDebugPanel = new JPanel(new CardLayout());
		EditManager.init(theIDE);
		EditManager editPanel = EditManager.getInstance();
		debugPanel = new DebugPanel();
		editDebugPanel.add(editPanel, EDIT_VIEW_STR);
		editDebugPanel.add(debugPanel, DEBUG_VIEW_STR);
		cl = (CardLayout) editDebugPanel.getLayout();
		cl.show(editDebugPanel, EDIT_VIEW_STR);

		// add feedback panel
		tophalf = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, fileProcessPanel,
				editDebugPanel);
		tophalf.setOneTouchExpandable(true);
		tophalf.setContinuousLayout(true);
		tophalf.setMinimumSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
		feedbackPanel = new FeedbackPanel();

		pane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tophalf, feedbackPanel);
		pane.setDividerLocation(IDEPreferences.getMainAreaHeight());
		pane.setOneTouchExpandable(true);
		pane.setContinuousLayout(true);
		pane.setResizeWeight(1.0);
		add(pane, BorderLayout.CENTER);
	}

	/**
	 * @return the Process Panel
	 */
	public ProcessPanel getProcessPanel() {
		return processPanel;
	}

	/**
	 * @return the feedback panel.
	 */
	public FeedbackPanel getFeedbackPanel() {
		return feedbackPanel;
	}

	/**
	 * @return the debug panel.
	 */
	public DebugPanel getDebugPanel() {
		return debugPanel;
	}

	/**
	 * @return the file panel
	 */
	public FilePanel getFilePanel() {
		return filePanel;
	}

	/**
	 * Returns 0 or 1 to indicate whether edit or debug view is being displayed.
	 * 
	 * TODO: change to enum
	 * 
	 * @return 0 for edit view, 1 for debug view.
	 */
	public int getView() {
		if (EditManager.getInstance().isVisible()) {
			return EDIT_VIEW;
		}
		return DEBUG_VIEW;
	}

	/**
	 * Switches between edit and debug view.
	 */
	public void switchView() {
		CardLayout cl = (CardLayout) editDebugPanel.getLayout();
		cl.next(editDebugPanel);
		cl = (CardLayout) fileProcessPanel.getLayout();
		cl.next(fileProcessPanel);
		ActionFactory.broadcastStateChange(theIDE);
	}

	/**
	 * @return the selected file in the file panel, or process in the process
	 *         panel.
	 */
	public List<? extends IDENode> getSelectedNodes() {
		if (getView() == EDIT_VIEW) {
			return filePanel.getSelectedNodes();
		}
		// process view
		return processPanel.getSelectedNodes();
	}

	/**
	 * Closes tab in either edit or debug panel.
	 * 
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws GOALException
	 * @throws GOALIncompleteGUIUsageException
	 * @throws GOALCommandCancelledException
	 * 
	 * @throws Exception
	 *             DOC
	 */
	public void close() throws GOALCommandCancelledException,
			GOALIncompleteGUIUsageException, GOALException,
			IllegalAccessException, InstantiationException {
		int view = getView();
		switch (view) {
		case EDIT_VIEW:
			EditManager.getInstance().close();
		case DEBUG_VIEW:
			debugPanel.close();
		}
	}

	/**
	 * Closes all open panels and prepares for IDE shutdown
	 * 
	 * @throws GOALCommandCancelledException
	 *             if user cancelled the process halfway. The IDE should stay in
	 *             a working state if he cancells the closedown.
	 */
	public void closeAll() throws GOALException {
		// relative sizes don't work, because the window is not visible at the
		// moment we set the divider
		IDEPreferences.setConsoleAreaHeight(pane.getDividerLocation());
		EditManager.getInstance().closeAll();
		debugPanel.closeAll();
	}

	/**
	 * @return currently selected panel, otherwise throws GOALUserError
	 * 
	 *         TODO: editPanel and debugPanel have inconsistent differences;
	 *         should have same contract would be useful to have a simple
	 *         interface that is being returned here instead of Component.
	 */
	public Component getCurrentPanel() throws GOALUserError {
		switch (getView()) {
		case EDIT_VIEW:
			return (Component) EditManager.getInstance().getActiveEditor();
		case DEBUG_VIEW:
			Component panel = debugPanel.getSelectedComponent();
			if (panel == null) {
				throw new GOALUserError("Did not find panel");
			} else {
				return panel;
			}
		default:
			throw new GOALUserError("Did not find panel");
		}
	}

}
