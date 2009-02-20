package org.workcraft.plugins.interop;

import java.io.File;

import org.workcraft.dom.Model;
import org.workcraft.framework.Importer;

public class DotGImporter implements Importer {
	public boolean accept(File file) {
		if (file.isDirectory())
			return true;
		if (file.getName().endsWith(".g"))
			return true;
		return false;
	}

	public String getDescription() {
        return ".g files (Petrify, PUNF)";
    }

	public Model importFromFile(File file) {
		return null;
	}
}
