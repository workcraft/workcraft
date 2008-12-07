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
		return this.file;
	}

	public Model getModel() {
		return this.model;
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
		return (this.model != null || this.file.getName().endsWith(".work"));
	}

	@Override
	public String toString() {
		String res = "";

		if (isWork()) {
			res = this.model.getTitle();
			if (res.isEmpty())
				if (this.file != null)
					res = this.file.getName();
				else
					res = "unnamed";
			if (this.model.getVisualModel() != null)
				res = res + " [V]";
		} else
			res = this.file.getName();

		if (this.unsaved || this.file == null)
			res = "* " + res;

		return res;
	}
}
