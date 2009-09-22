/*
*
* Copyright 2008,2009 Newcastle University
*
* This file is part of Workcraft.
*
* Workcraft is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* Workcraft is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with Workcraft.  If not, see <http://www.gnu.org/licenses/>.
*
*/

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

	public void setEnabled(boolean enabled) {
		if (this.enabled != enabled) {
			this.enabled = enabled;
			for (ScriptedActor actor : actors)
				actor.actionEnableStateChanged(enabled);
		}
	}
	public boolean isEnabled() {
		return enabled;
	}
}
