package org.workcraft.framework.serialisation;

import java.io.IOException;
import java.io.OutputStream;

import org.workcraft.dom.visual.VisualModel;
import org.workcraft.framework.exceptions.ExportException;
import org.workcraft.framework.exceptions.ModelValidationException;
import org.workcraft.framework.plugins.Plugin;

public interface VisualSerialiser extends Plugin, SerialFormat {
	public String getExtension();
	public String getDescription();
	public boolean isApplicableTo (VisualModel model);
	public ExportReferenceResolver export (VisualModel model, ExportReferenceResolver mathReferenceResolver, OutputStream out) throws IOException, ModelValidationException, ExportException;
}
