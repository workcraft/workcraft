package org.workcraft.plugins.circuit;

import java.io.File;

import org.workcraft.dom.math.MathNode;

public class Environment extends MathNode {
	private File file;

	public Environment() {

	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}
}
