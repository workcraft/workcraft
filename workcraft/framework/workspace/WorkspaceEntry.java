package org.workcraft.framework.workspace;

import java.io.File;

import org.workcraft.dom.Model;

public class WorkspaceEntry {
	private File file = null;
	private Model model = null;
	private boolean unsaved = false;

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
		model.getMathModel().setTitle(title);
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

		if (model.getVisualModel() != null)
			res = res + " [V]";

		if (unsaved)
			res = "* " + res;

		return res;
	}

	public int getEntryID() {
		return entryID;
	}
}