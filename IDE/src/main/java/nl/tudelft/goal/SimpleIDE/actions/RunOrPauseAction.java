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

import goal.tools.errorhandling.exceptions.GOALException;

import java.awt.event.ActionEvent;

import nl.tudelft.goal.SimpleIDE.ActionFactory;
import nl.tudelft.goal.SimpleIDE.IDENode;

/**
 * This is a bit special action. It either RUNS or PAUSES a MAS. If the run
 * button is enabled, it takes the RUN shape. Else, if the pause button is
 * enabled, it takes the PAUSE shape. Else it takes the disabled shape. See
 * #1229
 *
 *
 *
 * @author W.Pasman 23jun2011
 */
public class RunOrPauseAction extends GOALAction {
	/**
	 *
	 */
	private static final long serialVersionUID = 1366288050070370281L;
	private final RunAction myRunAction;
	private final PauseAction myPauseAction;

	public RunOrPauseAction() throws IllegalAccessException,
	InstantiationException {
		/**
		 * This action must be updated with stateChangeEvent() AFTER the run and
		 * pause actions have been updated. This must be ensured by creating
		 * this object after the run and pause were created. Now theoretically
		 * the following code ensures this because this constructor will not
		 * return after we have the RunAction and StepAction in our hands, and
		 * once we have it in our hands it has been added by the ActionFactory.
		 * The ActionFactory will add the RunOrPauseAction only after WE return
		 * here.
		 */
		this.myRunAction = (RunAction) ActionFactory.getAction(RunAction.class);
		this.myPauseAction = (PauseAction) ActionFactory
				.getAction(PauseAction.class);

		// #1870, use dummy icon as long as we are not enabled.
		setIcon(this.myRunAction.getIcon());
	}

	@Override
	public void stateChangeEvent() {
		if (this.myRunAction.isActionEnabled()) {
			setActionEnabled(true);
			setIcon(this.myRunAction.getIcon());
			setDescription(this.myRunAction.getDescription());
		} else if (this.myPauseAction.isActionEnabled()) {
			setActionEnabled(true);
			setIcon(this.myPauseAction.getIcon());
			setDescription(this.myPauseAction.getDescription());
		} else {
			setActionEnabled(false);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void execute(IDENode selectedNode, ActionEvent ae)
			throws GOALException {
		if (this.myRunAction.isEnabled()) {
			this.myRunAction.execute(selectedNode, ae);
		} else {
			this.myPauseAction.execute(selectedNode, ae);
		}
	}
}
