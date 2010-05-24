package org.workcraft.plugins.petrify.tasks;

import java.io.File;

public class SynthesisResult {
	private File equationsFile;
	private File logFile;

	public SynthesisResult(File equationsFile, File logFile) {
		this.equationsFile = equationsFile;
		this.logFile = logFile;
	}

	public File getEquationFile() {
		return this.equationsFile;
	}

	public File getLogFile() {
		return this.logFile;
	}
}
