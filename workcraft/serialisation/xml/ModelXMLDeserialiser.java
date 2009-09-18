package org.workcraft.serialisation.xml;

import org.w3c.dom.Element;
import org.workcraft.dom.Model;
import org.workcraft.dom.Node;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.serialisation.ReferenceResolver;

public interface ModelXMLDeserialiser extends XMLDeserialiser {
	public Model deserialise (Element modelElement, Node hierarchyRoot, ReferenceResolver internalReferenceResolver,
			ReferenceResolver externalReferenceResolver) throws DeserialisationException;
}
