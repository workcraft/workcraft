package org.workcraft.framework.serialisation.xml;

import org.w3c.dom.Element;
import org.workcraft.framework.exceptions.SerialisationException;
import org.workcraft.framework.serialisation.ExternalReferenceResolver;

public interface ReferencingXMLSerialiser extends XMLSerialiser {
	public void serialise(Element element, Object object, ExternalReferenceResolver incomingReferences) throws SerialisationException;
}
