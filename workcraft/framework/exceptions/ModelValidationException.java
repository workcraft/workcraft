package org.workcraft.framework.exceptions;

import java.util.LinkedList;

public class ModelValidationException extends Exception {

	private static final long serialVersionUID = 1L;
	private LinkedList<String> errors = new LinkedList<String>();
	public ModelValidationException() {
		super();
	}

	public void addError(String message) {
		errors.add(message);
	}

	public LinkedList<String> getErrors() {
		return errors;
	}

	@Override
	public String getMessage() {
		String r = "Model contains following errors:\n";
		for (String e: errors)
			r += e+"\n";
		return r;
	}
}
