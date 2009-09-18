package org.workcraft.interop;
import java.io.IOException;
import java.io.OutputStream;

import org.workcraft.Plugin;
import org.workcraft.dom.Model;
import org.workcraft.exceptions.ModelValidationException;
import org.workcraft.exceptions.SerialisationException;

public interface Exporter extends Plugin {
	public String getDescription();
	public String getExtenstion();
	public boolean isApplicableTo (Model model);
	public void export (Model model, OutputStream out) throws IOException, ModelValidationException, SerialisationException;
}