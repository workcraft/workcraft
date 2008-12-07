package org.workcraft.framework;

public class HistoryEvent extends Event {
	private String undoScript;
	private String redoScript;
	private String eventDescription;

	public HistoryEvent (String undoScript, String redoScript, String eventDescription, Object sender) {
		super(sender);
		this.undoScript = undoScript;
		this.redoScript = redoScript;
		this.eventDescription = eventDescription;
	}

	public String getEventDescription() {
		return this.eventDescription;
	}

	public String getRedoScript() {
		return this.redoScript;
	}

	public String getUndoScript() {
		return this.undoScript;
	}

}
