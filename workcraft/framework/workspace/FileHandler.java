package org.workcraft.framework.workspace;

import java.io.File;

import org.workcraft.framework.plugins.Plugin;

public interface FileHandler extends Plugin {
	public boolean accept (File f);
	public void execute (File f);
}