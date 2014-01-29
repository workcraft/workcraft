package org.workcraft.plugins.petrify.tasks;

import java.io.File;

public class SynthesisResult {
	private File equationsFile;
	private File logFile;
	private String stdout;
	private String stderr;

	public SynthesisResult(File equationsFile, File logFile, String stdout, String stderr) {
		this.equationsFile = equationsFile;
		this.logFile = logFile;
		this.stdout = stdout;
		this.stderr = stderr;
	}

	public File getEquationFile() {
		return this.equationsFile;
	}

	public File getLogFile() {
		return this.logFile;
	}

	public String getStdout() {
		return this.stdout;
	}

	public String getStderr() {
		return this.stderr;
	}
}
