package org.workcraft.framework.serialisation;
import java.io.OutputStream;

import org.workcraft.dom.Model;
import org.workcraft.framework.exceptions.SerialisationException;
import org.workcraft.framework.plugins.Plugin;

public interface ModelSerialiser extends Plugin, SerialFormat {
	public String getDescription();
	public String getExtension();
	public boolean isApplicableTo (Model model);
	public ReferenceProducer export (Model model, OutputStream out, ReferenceProducer externalReferences) throws SerialisationException;
}