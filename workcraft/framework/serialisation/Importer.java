package org.workcraft.framework.serialisation;
import java.io.File;
import java.io.IOException;
import java.nio.channels.ReadableByteChannel;

import org.workcraft.dom.Model;
import org.workcraft.framework.exceptions.ImportException;
import org.workcraft.framework.plugins.Plugin;

public interface Importer extends Plugin {
	public boolean accept (File file);
	public String getDescription();
	public Model importFrom (ReadableByteChannel in) throws ImportException, IOException;
}