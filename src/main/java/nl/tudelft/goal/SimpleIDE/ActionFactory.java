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

import java.util.LinkedHashMap;
import java.util.Map;

import nl.tudelft.goal.SimpleIDE.actions.GOALAction;

/**
 * This factory creates {@link GOALAction}s. It makes sure the initialization is
 * done properly and keeps track of existing actions, to (1) avoid making
 * duplicates which is just inefficient and (2) to be able to update the actions
 * when something changes in the IDE.
 * 
 * @author W.Pasman 21jun2011
 * 
 */
public class ActionFactory {
	/**
	 * The approach here does not entirely decouple the factory's objects from
	 * the actual class names, but instead expects the caller to be aware which
	 * exact class he needs. Here I am following "Thinking in Java" p343 (don't
	 * create interfaces unless it becomes clear that you really need them). The
	 * factory approach taken here also allows us to avoid the factory
	 * initialization problems (the chicken-and-egg problem, "Thinking in Java",
	 * p.582).<br>
	 * 
	 * The actionCache should maintain the ORDER of the inserted items.
	 */
	private static Map<Class<? extends GOALAction>, GOALAction> actionCache = new LinkedHashMap<Class<? extends GOALAction>, GOALAction>();

	private static ActionFactory theFactory = null;

	/**
	 * The initial state used for new actions.
	 */
	private static IDEState theInitialState = null;

	/**
	 * This factory is singleton, you can't instantiate it.
	 */
	protected ActionFactory(IDEfunctionality idef) {
		GOALAction.setIDEfunctionality(idef);
	}

	/**
	 * Get a factory. There is only 1 factory and the first call to getFactory
	 * determines the associated IDEfunctionality.
	 * 
	 * @param idef
	 *            is link to IDEFunctionality. Only used at the first call.
	 * @param initialState
	 *            is the initial state to be used for new actions.
	 * @return ActionFactory
	 */
	public static ActionFactory getFactory(IDEfunctionality idef,
			IDEState initialState) {
		if (theFactory == null) {
			theFactory = new ActionFactory(idef);
			theInitialState = initialState;
		}
		return theFactory;
	}

	/**
	 * get the action of the given class. Stores the action in the cache. The
	 * action is called with the initial state given
	 * 
	 * @param actionclass
	 *            the action class that this factory has to create a action
	 *            instantiation for.
	 * @return instantiation of given actionclass
	 * @throws InstantiationException
	 */
	public static GOALAction getAction(Class<? extends GOALAction> actionclass)
			throws IllegalAccessException, InstantiationException {
		GOALAction actionobject = actionCache.get(actionclass);
		if (actionobject == null) {
			actionobject = actionclass.newInstance();
			actionCache.put(actionclass, actionobject);
			actionobject.ideStateChangeEvent(theInitialState);
		}

		return actionobject;
	}

	/**
	 * Broadcast a state change to all actions.
	 * 
	 * @param newState
	 *            is the new state of the IDE.
	 */
	public static void broadcastStateChange(IDEState newState) {
		for (GOALAction action : actionCache.values()) {
			action.ideStateChangeEvent(newState);
		}
	}

}