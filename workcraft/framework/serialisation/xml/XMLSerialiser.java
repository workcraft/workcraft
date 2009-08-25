package org.workcraft.framework.serialisation.xml;

import org.w3c.dom.Element;
import org.workcraft.framework.exceptions.ExportException;
import org.workcraft.framework.plugins.Plugin;
import org.workcraft.framework.serialisation.ExternalReferenceResolver;

public interface XMLSerialiser extends Plugin {
	public String getClassName();
	public void serialise(Element element, Object object, ExternalReferenceResolver incomingReferences) throws ExportException;
}