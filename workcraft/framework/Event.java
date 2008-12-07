package org.workcraft.framework;

public class Event {
	protected Object sender;

	public Event(Object sender) {
		this.sender = sender;
	}

	public Object getSender() {
		return this.sender;
	}
}
