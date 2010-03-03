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

package org.workcraft.workspace;

import java.io.File;

import org.workcraft.dom.Model;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.gui.workspace.Path;

public class WorkspaceEntry
{
	private Object object = null;
	private boolean unsaved = true;
	private boolean unnamed = true;
	private boolean temporary = true;
	private Workspace workspace;

	public WorkspaceEntry(Workspace workspace) {
		this.workspace = workspace;
	}

	public Object getObject() {
		return object;
	}

	public void setObject(Object object) {
		this.object = object;
	}

	public void setUnsaved(boolean unsaved) {
		this.unsaved = unsaved;
		workspace.fireEntryChanged(this);
	}

	public boolean isUnsaved() {
		return unsaved;
	}

	public boolean isWork() {
		if (object != null)
			return (object instanceof Model);
		else
			return (getWorkspacePath().getNode().endsWith(".work"));
	}

	public String getTitle() {
		String res;
		String name = getWorkspacePath().getNode();
		if (isWork()) {
			int dot = name.lastIndexOf('.');
			if (dot == -1)
				res = name;
			else
				res = name.substring(0,dot);
		} else
			res = name;

		return res;
	}

	@Override
	public String toString() {
		String res = getTitle();

		if (object != null)
			if (object instanceof VisualModel)
				res = res + " [V]";

		if (unsaved)
			res = "* " + res;

		if (temporary)
			res = res + " (not in workspace)";

		return res;
	}

	public boolean isTemporary() {
		return temporary;
	}

	public void setTemporary(boolean temporary) {
		this.temporary = temporary;
	}

	public Path<String> getWorkspacePath() {
		return workspace.getPath(this);
	}

	public boolean isUnnamed() {
		return unnamed;
	}

	public File getFile() {
		return workspace.getFile(this);
	}

	public void setUnnamed(boolean unnamed) {
		this.unnamed = unnamed;
	}
}