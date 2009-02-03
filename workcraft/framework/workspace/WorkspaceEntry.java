package org.workcraft.framework.workspace;

import java.io.File;

import org.workcraft.dom.Model;

public class WorkspaceEntry {
	protected File file;
	protected Model model;
	protected boolean unsaved;
	protected Workspace workspace;

	public WorkspaceEntry(Workspace workspace) {
		file = null;
		model = null;
		unsaved = false;
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
		model.getMathModel().setTitle(title);
		workspace.fireEntryChanged(this);
	}

	public void setUnsaved(boolean unsaved) {
		this.unsaved = unsaved;
	}

	public boolean isUnsaved() {
		return unsaved;
	}

	public boolean isWork() {
		return (model != null || file.getName().endsWith(".work"));
	}

	@Override
	public String toString() {
		String res = "";

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
			if (model.getVisualModel() != null)
				res = res + " [V]";
		} else
			res = file.getName();

		if (unsaved || file == null)
			res = "* " + res;

		return res;
	}
}
