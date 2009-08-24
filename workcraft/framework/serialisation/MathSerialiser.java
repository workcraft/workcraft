package org.workcraft.framework.serialisation;
import java.io.IOException;
import java.io.OutputStream;

import org.workcraft.dom.MathModel;
import org.workcraft.framework.exceptions.ExportException;
import org.workcraft.framework.exceptions.ModelValidationException;
import org.workcraft.framework.plugins.Plugin;

public interface MathSerialiser extends Plugin, SerialFormat {
	public String getDescription();
	public String getExtension();
	public boolean isApplicableTo (MathModel model);
	public ExportReferenceResolver export (MathModel model, OutputStream out) throws IOException, ModelValidationException, ExportException;
}