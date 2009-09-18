package org.workcraft.serialisation;
import java.io.OutputStream;

import org.workcraft.Plugin;
import org.workcraft.dom.Model;
import org.workcraft.exceptions.SerialisationException;

public interface ModelSerialiser extends Plugin, SerialFormat {
	public String getDescription();
	public String getExtension();
	public boolean isApplicableTo (Model model);
	public ReferenceProducer export (Model model, OutputStream out, ReferenceProducer externalReferences) throws SerialisationException;
}