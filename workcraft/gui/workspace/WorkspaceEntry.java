package org.workcraft.gui.workspace;

import java.io.File;

public class WorkspaceEntry {
	private File file;

	public WorkspaceEntry (File file) {
		this.file = file;
	}

	public File file() {
		return file;
	}

	public String toString() {
		return file.getName();
	}
}
