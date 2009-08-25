package org.workcraft.framework.serialisation;
import java.io.OutputStream;

import org.workcraft.dom.Model;
import org.workcraft.framework.exceptions.ExportException;
import org.workcraft.framework.plugins.Plugin;

public interface ModelSerialiser extends Plugin, SerialFormat {
	public String getDescription();
	public String getExtension();
	public boolean isApplicableTo (Model model);
	public ExternalReferenceResolver export (Model model, OutputStream out, ExternalReferenceResolver incomingRefrences) throws ExportException;
}