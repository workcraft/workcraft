package org.workcraft.exceptions;

@SuppressWarnings("serial")
public class DeserialisationException extends Exception {

	public DeserialisationException() {
	}

	public DeserialisationException(String arg0) {
		super(arg0);
	}

	public DeserialisationException(Throwable arg0) {
		super(arg0);
	}

	public DeserialisationException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

}
