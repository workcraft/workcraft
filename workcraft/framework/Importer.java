package org.workcraft.framework;
import java.io.File;

import org.workcraft.dom.Model;
import org.workcraft.framework.plugins.Plugin;

public interface Importer extends Plugin {
	public boolean accept (File file);
	public String getDescription();
	public Model importFromFile (File file);
}