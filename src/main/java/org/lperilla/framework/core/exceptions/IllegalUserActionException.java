
package org.lperilla.framework.core.exceptions;

public class IllegalUserActionException extends Exception {

	private static final long serialVersionUID = 240L;

	public IllegalUserActionException(String name) {
		super(name);
	}

	public IllegalUserActionException(String name, Throwable t) {
		super(name, t);
	}

}
