package org.lperilla.framework.core.action;

import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.SwingUtilities;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lperilla.framework.core.exceptions.IllegalUserActionException;
import org.lperilla.framework.core.reflect.ClassFinder;

public final class ActionRouter implements ActionListener {

	private static final Logger logger = LogManager.getLogger(ActionRouter.class);

	private static final Object LOCK = new Object();

	private static volatile ActionRouter instance;

	private Map<String, Set<Command>> commands;

	private final Map<String, HashSet<ActionListener>> preActionListeners = new HashMap<String, HashSet<ActionListener>>();

	private final Map<String, HashSet<ActionListener>> postActionListeners = new HashMap<String, HashSet<ActionListener>>();

	private ActionRouter() {
		this.setCommands(new HashMap<String, Set<Command>>());
	}

	public void actionPerformed(final ActionEvent e) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				performAction(e);
			}
		});
	}

	private void performAction(final ActionEvent e) {
		String actionCommand = e.getActionCommand();
		logger.debug("Ejecutando acción: " + actionCommand);
		try {
			for (Command command : getCommands().get(actionCommand)) {
				try {
					preActionPerformed(command.getClass(), e);
					command.doAction(e);
					postActionPerformed(command.getClass(), e);
				} catch (IllegalUserActionException err) {
					String msg = err.getMessage();
					if (msg == null) {
						msg = err.toString();
					}
					Throwable t = err.getCause();
					if (t != null) {
						String cause = t.getMessage();
						if (cause == null) {
							cause = t.toString();
						}
						msg = msg + "\n" + cause;
					}
				} catch (Exception err) {
					logger.error("Error processing " + command.toString(), err);
				}
			}
		} catch (NullPointerException er) {
			logger.error("performAction(" + actionCommand + ") " + e.toString() + " caused", er);
		}
	}

	/**
	 * To execute an action immediately in the current thread.
	 *
	 * @param e
	 *            the action to execute
	 */
	public void doActionNow(ActionEvent e) {
		performAction(e);
	}

	/**
	 * Get the set of {@link Command}s registered under the name
	 * <code>actionName</code>
	 * 
	 * @param actionName
	 *            The name the {@link Command}s were registered
	 * @return a set with all registered {@link Command}s for
	 *         <code>actionName</code>
	 */
	public Set<Command> getAction(String actionName) {
		Set<Command> set = new HashSet<Command>();
		for (Command command : getCommands().get(actionName)) {
			try {
				set.add(command);
			} catch (Exception err) {
				logger.error("Could not add Command ", err);
			}
		}
		return set;
	}

	/**
	 * Get the {@link Command} registered under the name <code>actionName</code>
	 * , that is of {@link Class} <code>actionClass</code>
	 * 
	 * @param actionName
	 *            The name the {@link Command}s were registered
	 * @param actionClass
	 *            The class the {@link Command}s should be equal to
	 * @return The registered {@link Command} for <code>actionName</code>, or
	 *         <code>null</code> if none could be found
	 */
	public Command getAction(String actionName, Class<?> actionClass) {
		for (Command com : getCommands().get(actionName)) {
			if (com.getClass().equals(actionClass)) {
				return com;
			}
		}
		return null;
	}

	/**
	 * Get the {@link Command} registered under the name <code>actionName</code>
	 * , which class names are equal to <code>className</code>
	 * 
	 * @param actionName
	 *            The name the {@link Command}s were registered
	 * @param className
	 *            The name of the class the {@link Command}s should be equal to
	 * @return The {@link Command} for <code>actionName</code> or
	 *         <code>null</code> if none could be found
	 */
	public Command getAction(String actionName, String className) {
		for (Command command : getCommands().get(actionName)) {
			if (command.getClass().getName().equals(className)) {
				return command;
			}
		}
		return null;
	}

	/**
	 * Allows an ActionListener to receive notification of a command being
	 * executed prior to the actual execution of the command.
	 *
	 * @param action
	 *            the Class of the command for which the listener will
	 *            notifications for. Class must extend
	 *            org.apache.jmeter.gui.action.Command.
	 * @param listener
	 *            the ActionListener to receive the notifications
	 */
	public void addPreActionListener(Class<?> action, ActionListener listener) {
		if (action != null) {
			HashSet<ActionListener> set = preActionListeners.get(action.getName());
			if (set == null) {
				set = new HashSet<ActionListener>();
			}
			set.add(listener);
			preActionListeners.put(action.getName(), set);
		}
	}

	/**
	 * Allows an ActionListener to be removed from receiving notifications of a
	 * command being executed prior to the actual execution of the command.
	 *
	 * @param action
	 *            the Class of the command for which the listener will
	 *            notifications for. Class must extend
	 *            org.apache.jmeter.gui.action.Command.
	 * @param listener
	 *            the ActionListener to receive the notifications
	 */
	public void removePreActionListener(Class<?> action, ActionListener listener) {
		if (action != null) {
			HashSet<ActionListener> set = preActionListeners.get(action.getName());
			if (set != null) {
				set.remove(listener);
				preActionListeners.put(action.getName(), set);
			}
		}
	}

	/**
	 * Allows an ActionListener to receive notification of a command being
	 * executed after the command has executed.
	 *
	 * @param action
	 *            the Class of the command for which the listener will
	 *            notifications for. Class must extend
	 *            org.apache.jmeter.gui.action.Command.
	 * @param listener
	 *            The {@link ActionListener} to be registered
	 */
	public void addPostActionListener(Class<?> action, ActionListener listener) {
		if (action != null) {
			HashSet<ActionListener> set = postActionListeners.get(action.getName());
			if (set == null) {
				set = new HashSet<ActionListener>();
			}
			set.add(listener);
			postActionListeners.put(action.getName(), set);
		}
	}

	/**
	 * Allows an ActionListener to be removed from receiving notifications of a
	 * command being executed after the command has executed.
	 *
	 * @param action
	 *            the Class of the command for which the listener will
	 *            notifications for. Class must extend
	 *            org.apache.jmeter.gui.action.Command.
	 * @param listener
	 *            The {@link ActionListener} that should be deregistered
	 */
	public void removePostActionListener(Class<?> action, ActionListener listener) {
		if (action != null) {
			HashSet<ActionListener> set = postActionListeners.get(action.getName());
			if (set != null) {
				set.remove(listener);
				postActionListeners.put(action.getName(), set);
			}
		}
	}

	protected void preActionPerformed(Class<? extends Command> action, ActionEvent e) {
		if (action != null) {
			Set<ActionListener> listenerSet = preActionListeners.get(action.getName());
			if (listenerSet != null && listenerSet.size() > 0) {
				ActionListener[] listeners = listenerSet.toArray(new ActionListener[listenerSet.size()]);
				for (ActionListener listener : listeners) {
					listener.actionPerformed(e);
				}
			}
		}
	}

	protected void postActionPerformed(Class<? extends Command> action, ActionEvent e) {
		if (action != null) {
			Set<ActionListener> listenerSet = postActionListeners.get(action.getName());
			if (listenerSet != null && listenerSet.size() > 0) {
				ActionListener[] listeners = listenerSet.toArray(new ActionListener[listenerSet.size()]);
				for (ActionListener listener : listeners) {
					listener.actionPerformed(e);
				}
			}
		}
	}

	private void populateCommandMap() {
		try {
			List<String> listClasses = ClassFinder.findClassesThatExtend();
			this.setCommands(new HashMap<String, Set<Command>>(listClasses.size()));
			if (listClasses.isEmpty()) {
				logger.warn("!!!!!Uh-oh, didn't find any action handlers!!!!!");
			} else {
				for (String strClassName : listClasses) {
					Class<?> commandClass = Class.forName(strClassName);
					Command command = (Command) commandClass.newInstance();
					for (String commandName : command.getActionNames()) {
						Set<Command> commandObjects = this.getCommands().get(commandName);
						if (commandObjects == null) {
							commandObjects = new HashSet<Command>();
							getCommands().put(commandName, commandObjects);
						}
						commandObjects.add(command);
					}
				}
			}
		} catch (HeadlessException e) {
			logger.error(e);
		} catch (Exception e) {
			logger.error("exception finding action handlers", e);
		}
	}

	/**
	 * Gets the Instance attribute of the ActionRouter class
	 *
	 * @return The Instance value
	 */
	public static ActionRouter getInstance() {
		if (instance == null) {
			synchronized (LOCK) {
				if (instance == null) {
					instance = new ActionRouter();
					instance.populateCommandMap();
				}
			}
		}
		return instance;
	}

	public Map<String, Set<Command>> getCommands() {
		return commands;
	}

	public void setCommands(Map<String, Set<Command>> commands) {
		this.commands = commands;
	}
}
