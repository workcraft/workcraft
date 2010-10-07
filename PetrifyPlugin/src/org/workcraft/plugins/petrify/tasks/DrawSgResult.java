package org.workcraft.plugins.petrify.tasks;

import java.io.File;

public class DrawSgResult {
	private File psFile = null;
	private String errorMessages = null;

	public DrawSgResult(File psFile, String errorMessages) {
		this.psFile = psFile;
		this.errorMessages = errorMessages;
	}

	public File getPsFile() {
		return psFile;
	}

	public String getErrorMessages() {
		return errorMessages;
	}
}
