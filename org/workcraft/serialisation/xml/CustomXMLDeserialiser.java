package org.workcraft.serialisation.xml;

import org.w3c.dom.Element;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.serialisation.ReferenceResolver;

public interface CustomXMLDeserialiser extends XMLDeserialiser {
	public Object initInstance (Element element, ReferenceResolver externalReferenceResolver) throws DeserialisationException;
	public void finaliseInstance(Element element, Object instance,
			ReferenceResolver internalReferenceResolver,
			ReferenceResolver externalReferenceResolver)
			throws DeserialisationException;
}