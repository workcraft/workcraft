package org.workcraft.framework;
import java.io.IOException;
import java.nio.channels.WritableByteChannel;

import org.workcraft.dom.Model;
import org.workcraft.framework.exceptions.ExportException;
import org.workcraft.framework.exceptions.ModelValidationException;
import org.workcraft.framework.plugins.Plugin;

public interface Exporter extends Plugin {
	public String getDescription();
	public String getExtenstion();
	public boolean isApplicableTo (Model model);
	public void export (Model model, WritableByteChannel out) throws IOException, ModelValidationException, ExportException;
}