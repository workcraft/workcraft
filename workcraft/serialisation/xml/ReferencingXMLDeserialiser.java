package org.workcraft.serialisation.xml;

import org.w3c.dom.Element;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.serialisation.ReferenceResolver;

public interface ReferencingXMLDeserialiser extends XMLDeserialiser {
	public void deserialise(Element element, Object instance,
			ReferenceResolver internalReferenceResolver,
			ReferenceResolver externalReferenceResolver
			)
			throws DeserialisationException;
}