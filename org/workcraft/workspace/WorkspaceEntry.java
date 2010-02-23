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

public class WorkspaceEntry {
	private File file = null;
	private Object object = null;
	private boolean unsaved = false;
	private boolean temporary = true;
	private int entryID;
	private Workspace workspace;

	public WorkspaceEntry(Workspace workspace) {
		entryID = workspace.getNextEntryID();
		this.workspace = workspace;
	}

	public WorkspaceEntry(Workspace workspace, File f) {
		this(workspace);

		file = f;
	}

	public File getFile() {
		return file;
	}

	public Object getObject() {
		return object;
	}

	public void setFile(File file) {
		this.file = file;
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
		else if (file != null)
			return (file.getName().endsWith(".work"));
		else
			throw new RuntimeException ("WorkspaceEntry has null in both object and file fields, this should not happen.");
	}

	public String getTitle() {
		String res;
		if (isWork()) {
				if (file != null) {
					String fileName = file.getName();
					int dot = fileName.lastIndexOf('.');
					if (dot == -1)
						res = fileName;
					else
						res = fileName.substring(0,dot);
				}
				else
					res = "Untitled";
		} else
			res = file.getName();

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

	public int getEntryID() {
		return entryID;
	}

	public boolean isTemporary() {
		return temporary;
	}

	public void setTemporary(boolean temporary) {
		this.temporary = temporary;
	}
}