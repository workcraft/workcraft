package org.workcraft.framework.serialisation.xml;

import org.w3c.dom.Element;
import org.workcraft.framework.exceptions.ImportException;
import org.workcraft.framework.serialisation.ReferenceResolver;

public interface XMLDeserialiser {
	public String getClassName();
	public void deserialise (Element element, ReferenceResolver referenceResolver) throws ImportException;
}