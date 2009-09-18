package org.workcraft.serialisation;
import java.io.File;
import java.io.IOException;
import java.nio.channels.ReadableByteChannel;

import org.workcraft.Plugin;
import org.workcraft.dom.Model;
import org.workcraft.exceptions.DeserialisationException;

public interface Importer extends Plugin {
	public boolean accept (File file);
	public String getDescription();
	public Model importFrom (ReadableByteChannel in) throws DeserialisationException, IOException;
}