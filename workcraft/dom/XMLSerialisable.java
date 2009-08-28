package org.workcraft.dom;

import org.w3c.dom.Element;
import org.workcraft.framework.exceptions.DeserialisationException;
import org.workcraft.framework.serialisation.ReferenceProducer;
import org.workcraft.framework.serialisation.ReferenceResolver;

public interface XMLSerialisable {
	public void serialise(Element element, ReferenceProducer referenceResolver);
	public void deserialise(Element element, ReferenceResolver referenceResolver) throws DeserialisationException;
}