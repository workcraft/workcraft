package org.workcraft.gui.actions;

import java.util.LinkedList;

public abstract class ScriptedAction {
	private LinkedList<ScriptedActor> actors = new LinkedList<ScriptedActor>();
	private boolean enabled = true;

	public abstract String getText();
	public abstract String getScript();

	public String getUndoScript() {
		return null;
	}

	public String getRedoScript() {
		return null;
	}

	void addActor(ScriptedActor actor) {
		actors.add(actor);
	}

	void removeActor(ScriptedActor actor) {
		actors.remove(actor);
	}

	void setEnabled(boolean enabled) {
		if (this.enabled != enabled) {
			this.enabled = enabled;
			for (ScriptedActor actor : actors)
				actor.actionEnableStateChanged(enabled);
		}
	}
}
