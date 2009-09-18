package org.workcraft.interop;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.workcraft.Plugin;
import org.workcraft.dom.Model;
import org.workcraft.exceptions.DeserialisationException;

public interface Importer extends Plugin {
	public boolean accept (File file);
	public String getDescription();
	public Model importFrom (InputStream in) throws DeserialisationException, IOException;
}