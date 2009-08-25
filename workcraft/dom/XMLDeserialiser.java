package org.workcraft.dom;

import org.w3c.dom.Element;
import org.workcraft.framework.exceptions.ImportException;
import org.workcraft.framework.serialisation.ReferenceResolver;

public interface XMLDeserialiser {
	public void deserialise(Element element, ReferenceResolver refResolver) throws ImportException;
	public String getTagName();
}
