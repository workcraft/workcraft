package org.workcraft.framework;
import java.io.File;
import java.io.FileFilter;

import org.workcraft.dom.Model;
import org.workcraft.framework.plugins.Plugin;

public interface Importer extends Plugin, FileFilter {
	public boolean accept (File file);
	public Model importFromFile (File file);
}