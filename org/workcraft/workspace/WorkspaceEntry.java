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
	private Model model = null;
	private boolean unsaved = false;
	private boolean temporary = true;
	private int entryID;
	private Workspace workspace;

	public WorkspaceEntry(Workspace workspace) {
		entryID = workspace.getNextEntryID();
		this.workspace = workspace;
	}

	public File getFile() {
		return file;
	}

	public Model getModel() {
		return model;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public void setModel(Model model) {
		this.model = model;
	}

	public void renameModel(String title) {
		model.setTitle(title);
		workspace.fireEntryChanged(this);
	}

	public void setUnsaved(boolean unsaved) {
		this.unsaved = unsaved;
		workspace.fireEntryChanged(this);
	}

	public boolean isUnsaved() {
		return unsaved;
	}

	public boolean isWork() {
		return (model != null || file.getName().endsWith(".work"));
	}

	public String getTitle() {
		String res;
		if (isWork()) {
			res = model.getTitle();
			if (res.isEmpty())
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

		if (model != null)
			if (model instanceof VisualModel)
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