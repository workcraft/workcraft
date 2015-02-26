package org.workcraft.plugins.circuit;

import java.io.File;

import org.workcraft.dom.math.MathNode;
import org.workcraft.observation.PropertyChangedEvent;

public class Environment extends MathNode {
	private File file;
	private File base;

	public Environment() {

	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
		sendNotification(new PropertyChangedEvent(this, "file"));
	}

	public File getBase() {
		return base;
	}

	public void setBase(File base) {
		this.base = base;
		sendNotification(new PropertyChangedEvent(this, "base"));
	}

}
