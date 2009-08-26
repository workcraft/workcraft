package org.workcraft.framework.serialisation.xml;

import org.w3c.dom.Element;
import org.workcraft.framework.exceptions.DeserialisationException;
import org.workcraft.framework.serialisation.ReferenceResolver;

public interface ReferencingXMLDeserialiser extends XMLDeserialiser {
	public void deserialise (Element element, Object instance, ReferenceResolver referenceResolver) throws DeserialisationException;
}