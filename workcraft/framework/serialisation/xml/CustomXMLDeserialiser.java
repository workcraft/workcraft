package org.workcraft.framework.serialisation.xml;

import org.w3c.dom.Element;
import org.workcraft.framework.exceptions.DeserialisationException;
import org.workcraft.framework.serialisation.ReferenceResolver;

public interface CustomXMLDeserialiser extends XMLDeserialiser {
	public Object initInstance (Element element) throws DeserialisationException;
	public void finaliseInstance (Element element, Object instance, ReferenceResolver referenceResolver) throws DeserialisationException;
}