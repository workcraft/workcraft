package org.workcraft.framework;
import java.io.File;
import java.io.FileFilter;

import org.workcraft.dom.Model;
import org.workcraft.framework.plugins.Plugin;

public interface Exporter extends Plugin, FileFilter {
	public boolean accept (File pathname);
	public void exportToFile (Model model, File name);
}