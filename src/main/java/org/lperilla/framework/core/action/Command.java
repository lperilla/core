package org.lperilla.framework.core.action;

import java.awt.event.ActionEvent;
import java.util.Set;

import org.lperilla.framework.core.exceptions.IllegalUserActionException;

public interface Command {

	public void doAction(ActionEvent e) throws IllegalUserActionException;

	public Set<String> getActionNames();

}
