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

import goal.tools.errorhandling.Resources;
import goal.tools.errorhandling.Warning;
import goal.tools.errorhandling.WarningStrings;
import goal.tools.errorhandling.exceptions.GOALBug;
import goal.tools.errorhandling.exceptions.GOALCommandCancelledException;
import goal.tools.errorhandling.exceptions.GOALException;
import goal.tools.errorhandling.exceptions.GOALIncompleteGUIUsageException;
import goal.tools.errorhandling.exceptions.GOALUserError;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import nl.tudelft.goal.SimpleIDE.ActionFactory;
import nl.tudelft.goal.SimpleIDE.IDENode;
import nl.tudelft.goal.SimpleIDE.IDEState;
import nl.tudelft.goal.SimpleIDE.IDEfunctionality;

/**
 * General GOAL action. Intended for use inside our package only. The function
 * {@link #setIDEfunctionality(IDEfunctionality)} should be called once to
 * initialize this class for use. <br>
 *
 *
 * @author W.Pasman 15jun2011
 */
public abstract class GOALAction extends AbstractAction {

	/** current IDEState. */
	protected IDEState currentState = null;

	/**
	 * the IDEFunctionality is set once and shared for all. The IDE should be
	 * used only to execute actions. For handling state changes the IDEState
	 * should contain the required info.
	 *
	 */
	protected static IDEfunctionality developmentEnvironment = null;

	private static String action = "Action"; //$NON-NLS-1$

	/**
	 * The default name given to an action is the name of the java class,
	 * without the string "Action" if the java class ends on that. You can use
	 * CamelCase, each upper case character will be pre-fixed with a space for
	 * the text in the menu.
	 */
	public GOALAction() {
		checkInit();
		String defaultName = ""; //$NON-NLS-1$
		for (char c : this.getClass().getSimpleName().toCharArray()) {
			if (c >= 'A' && c <= 'Z' && !defaultName.isEmpty()) {
				defaultName += " "; //$NON-NLS-1$
			}
			defaultName += c;
		}
		if (defaultName.endsWith(action)) {
			defaultName = defaultName.substring(0,
					defaultName.lastIndexOf(action));
		}
		putValue(NAME, defaultName);

	}

	/**
	 * default, enforce to use the equals() code
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Two GOAL Actions are the same if they are the same class. So two
	 * UndoActions are always the same. You should use the ActionFactory to
	 * ensure that currentState is consitent always.
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		return (getClass() == obj.getClass());
	}

	/**
	 * Check that GOALAction has been initialized.
	 */
	private void checkInit() {
		if (developmentEnvironment == null) {
			throw new GOALBug(
					"GOALAction class has not been initialized (setIDEfunctionality was not called)"); //$NON-NLS-1$
		}
	}

	/**
	 * Call this once before using this class
	 *
	 * @param f
	 */
	public static void setIDEfunctionality(IDEfunctionality f) {
		if (developmentEnvironment != null) {
			throw new GOALBug("IDEfunctionality cannot be set more than once"); //$NON-NLS-1$
		}
		developmentEnvironment = f;
	}

	/**
	 * This should be called whenever the IDE changes its state. This state is
	 * saved and then {@link #stateChangeEvent()} is called.
	 *
	 * @param newstate
	 *            is the new IDE state.
	 */
	public final void ideStateChangeEvent(IDEState newState) {
		this.currentState = newState;
		stateChangeEvent();
	}

	/**
	 * This function is called when the IDE state changes. Actions can override
	 * this to handle IDE state changes properly.
	 */
	public abstract void stateChangeEvent();

	@Override
	/**
	 * This function is called when the action is activated.
	 * We reroute it to {@link #execute} after a few checks.
	 * Do not overwrite
	 */
	public final void actionPerformed(ActionEvent event) {
		if (this.currentState == null) {
			throw new IllegalStateException(
					"action state has not been set up properly"); //$NON-NLS-1$
		}
		if (developmentEnvironment == null) {
			throw new IllegalStateException("IDE has not been set up properly"); //$NON-NLS-1$
		}

		try {
			executeAll(event);
		} catch (GOALCommandCancelledException e) {
			new Warning(Resources.get(WarningStrings.CANCELLED_ACTION), e);
		} catch (GOALIncompleteGUIUsageException e) {
			new Warning(Resources.get(WarningStrings.FAILED_REQUEST), e);
			// CHECK maybe show a popup at this point?
		} catch (GOALBug e) {
			new Warning(Resources.get(WarningStrings.HIT_GOAL_BUG), e);
		} catch (GOALUserError e) {
			// Report what the user did that we could not handle. FIXME don't
			// duplicate messages, does not help the user.
			new Warning(e.getMessage(), e.getCause());
		} catch (GOALException e) { // catch remaining exceptions. Just hope
			// it's not too bad.
			new Warning(Resources.get(WarningStrings.GOAL_EXCEPTION), e);
		} catch (RuntimeException e) {
			// emergency catch, we should avoid these.
			new Warning(String.format(
					Resources.get(WarningStrings.RUNTIME_EXCEPTION),
					this.toString()), e);
		}
		ActionFactory.broadcastStateChange(this.currentState);
	}

	/**
	 * Execute this action for all currently selected nodes. It is the default
	 * behaviour: iterate over the selected nodes and call execute for each
	 * selected node. You can override this if you want a different behaviour
	 * based on the total selection.
	 * <p>
	 * By default all Exceptions are passed straight through, which means that
	 * any exception will cancel the execute for the remaining nodes.
	 *
	 *
	 * @param event
	 *            is the UI event that triggered this action
	 * @throws GOALCommandCancelledException
	 * @throws GOALIncompleteGUIUsageException
	 * @throws GOALException
	 */
	protected void executeAll(ActionEvent event)
			throws GOALCommandCancelledException,
			GOALIncompleteGUIUsageException, GOALException {
		for (IDENode selectedNode : this.currentState.getSelectedNodes()) {
			execute(selectedNode, event);
		}
	}

	/**
	 * public version of execute. do not overwrite. It calls the private execute
	 * and then forces broadcastStateChange after call.
	 *
	 * @throws GOALException
	 * @throws GOALIncompleteGUIUsageException
	 * @throws GOALCommandCancelledException
	 */
	public final void Execute(IDENode node, ActionEvent arg)
			throws GOALCommandCancelledException,
			GOALIncompleteGUIUsageException, GOALException {
		execute(node, arg);
		// we know / assume that the state always is current so we don't have to
		// ask IDE for new state.
		ActionFactory.broadcastStateChange(this.currentState);
	}

	/**
	 * This function is called when the user triggers the action, eg by pressing
	 * a button or selecting a menu. Override this with the implementation of
	 * the action executer. It has been checked that
	 * {@link #developmentEnvironment} and {@link #currentState} have been set
	 * properly when this is called. <br>
	 * If multiple nodes were selected by user, the execute call is done
	 * multiple times, one node at a time.
	 *
	 * @param node
	 *            is the node that needs treatment with this action.
	 */
	protected abstract void execute(IDENode node, ActionEvent arg)
			throws GOALCommandCancelledException,
			GOALIncompleteGUIUsageException, GOALException;

	/**
	 * sets the icon for this action. Normal use is to override the constructor
	 * and call this to set the action's icon.
	 *
	 * @param icon
	 *            the icon to be used.
	 */

	public void setIcon(ImageIcon icon) {
		putValue(SMALL_ICON, icon);

	}

	/**
	 * Get the current icon of this action
	 *
	 * @return ImageIcon of the current icon
	 */
	public ImageIcon getIcon() {
		return (ImageIcon) getValue(SMALL_ICON);
	}

	/**
	 * Associates pressing keyboard character in combination with Apply key or
	 * CTLR (Linux, Windows) with the {@link GOALAction}.
	 *
	 * @param keyboardshortcut
	 *            Keyboard character.
	 */
	public void setShortcut(char keyboardshortcut) {
		setShortcut(keyboardshortcut, 0);
	}

	/**
	 * sets the keyboard shortcut for this action, aka "accelerator". Note that
	 * shortcuts work through the MENU bar, so only actions that have been added
	 * to the menu can be accelerated this way. <br>
	 * This function adds the common 'mask' to the shortcut, depending on the
	 * OS. On OSX, this means the META key is to be pressed along with the
	 * shortcut (aka 'Apple' key), while on Linux and Windows the ctrl key is to
	 * be pressed along with the shortcut. <br>
	 * You can also specify additional masks for the shortcut. The shift key is
	 * the most common additional mask.
	 *
	 * @param keyboardshortcut
	 *            is the key(s) to press to trigger the action. Should be upper
	 *            case char, eg 'S'
	 * @param mask
	 *            is the modifier key, see {@link InputEvent#SHIFT_DOWN_MASK} eg
	 *            when you want the shift key to be depressed along with the
	 *            keyboardshortcut, you use {@link InputEvent#SHIFT_DOWN_MASK}
	 *
	 */
	public void setShortcut(char keyboardshortcut, int mask) {
		// Set mask for keystroke
		int fullmask = 0;

		// On OSX we need apple-X for the menu shortcuts, on other platforms
		// ctrl-X.
		if (System.getProperty("os.name").toLowerCase().indexOf("mac os x") > -1) { //$NON-NLS-1$ //$NON-NLS-2$
			fullmask = InputEvent.META_MASK | mask;
		} else {
			fullmask = InputEvent.CTRL_MASK | mask;
		}

		mask = mask | InputEvent.META_MASK;

		putValue(ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(keyboardshortcut, fullmask));
	}

	/**
	 * set the name of the action. This name is used for the menu text. If not
	 * used, the default name (see {@link #GOALAction()} ) is used. If possible,
	 * try to use the default name to keep code and menu items match as close as
	 * possible.
	 *
	 * @param name
	 *            is the name of the action
	 */
	public void setName(String name) {
		putValue(NAME, name);
	}

	/**
	 * Set a longer description for the action. Used as tool tip text.
	 *
	 * @param descr
	 */
	public void setDescription(String descr) {
		putValue(SHORT_DESCRIPTION, descr);
	}

	public String getDescription() {
		return (String) getValue(SHORT_DESCRIPTION);
	}

	/**
	 * Our local, threadsafe version of the enabled flag. We copy this to the
	 * real enabled using invokelater, if you use the setActionEnabled call. DO
	 * NOT USE setEnabled directly!
	 */
	private boolean isEnabled = false;

	/**
	 * The swing enabled bit will switch only after SWING has had CPU time. with
	 * isActionEnabled you can check the planned state as has been set with the
	 * last call to setActionEnabled
	 *
	 * @return
	 */
	public boolean isActionEnabled() {
		return this.isEnabled;
	}

	/**
	 * Direct calling setEnabled from a non-AWT thread is not OK, see #1859. To
	 * fix this we provide a thread-safe setActionEnabled. WARNING: do not try
	 * to call setEnabled directly. Unfortunately we can't override it and make
	 * it private. We can't even override because the implementation of
	 * setActionEnabled would be a problem (how to call the super class
	 * setEnabled from the Swing thread if we override it?)
	 *
	 * @param isEnabled
	 *            new value for enablednesss
	 */
	public void setActionEnabled(final boolean newEnabled) {
		final AbstractAction action = this;
		this.isEnabled = newEnabled;

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				action.setEnabled(GOALAction.this.isEnabled);
			}
		});
	}

}
