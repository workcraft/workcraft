package org.workcraft.workspace;

import java.io.File;

import org.workcraft.Plugin;

public interface FileHandler extends Plugin {
	public boolean accept (File f);
	public void execute (File f);
}