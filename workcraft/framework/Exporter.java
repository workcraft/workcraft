package org.workcraft.framework;
import java.io.File;
import java.io.IOException;

import org.workcraft.dom.Model;
import org.workcraft.framework.exceptions.ExportException;
import org.workcraft.framework.exceptions.ModelValidationException;
import org.workcraft.framework.plugins.Plugin;

public interface Exporter extends Plugin {
	public String getDescription();
	public String getExtenstion();
	public boolean isApplicableTo (Model model);
	public void exportToFile (Model model, File name) throws IOException, ModelValidationException, ExportException;
}