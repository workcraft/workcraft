package org.workcraft.framework.workspace;

import java.io.File;

import org.workcraft.dom.Model;

public class WorkspaceEntry {
	protected File file;
	protected Model model;
	protected boolean unsaved;

	public WorkspaceEntry() {
		this.file = null;
		this.model = null;
		this.unsaved = false;
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

	public void setUnsaved(boolean unsaved) {
		this.unsaved = unsaved;
	}

	public boolean isUnsaved() {
		return this.unsaved;
	}

	public boolean isWork() {
		return (model != null || file.getName().endsWith(".work"));
	}

	public String toString() {
		String res = "";

		if (isWork()) {
			res = model.getTitle();
			if (res.isEmpty())
				if (file != null)
					res = file.getName();
				else
					res = "unnamed";
			if (model.getVisualModel() != null)
				res = res + " [V]";
		} else {
			res = file.getName();
		}

		if (unsaved || file == null)
			res = "* " + res;

		return res;
	}
}
