package org.workcraft.framework.serialisation;
import java.io.IOException;
import java.io.OutputStream;

import org.workcraft.dom.Model;
import org.workcraft.framework.exceptions.SerialisationException;
import org.workcraft.framework.exceptions.ModelValidationException;
import org.workcraft.framework.plugins.Plugin;

public interface Exporter extends Plugin {
	public String getDescription();
	public String getExtenstion();
	public boolean isApplicableTo (Model model);
	public void export (Model model, OutputStream out) throws IOException, ModelValidationException, SerialisationException;
}