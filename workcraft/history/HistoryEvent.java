package org.workcraft.history;

public class HistoryEvent {
	private String undoScript;
	private String redoScript;
	private String eventDescription;

	public HistoryEvent (String undoScript, String redoScript, String eventDescription, Object sender) {
		//super(sender);
		this.undoScript = undoScript;
		this.redoScript = redoScript;
		this.eventDescription = eventDescription;
	}

	public String getEventDescription() {
		return eventDescription;
	}

	public String getRedoScript() {
		return redoScript;
	}

	public String getUndoScript() {
		return undoScript;
	}

}
