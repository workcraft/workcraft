package org.workcraft.framework.serialisation.xml;

import org.w3c.dom.Element;
import org.workcraft.dom.Model;
import org.workcraft.dom.Node;
import org.workcraft.framework.exceptions.DeserialisationException;
import org.workcraft.framework.serialisation.ReferenceResolver;

public interface ModelXMLDeserialiser extends XMLDeserialiser {
	public Model deserialise (Element modelElement, Node hierarchyRoot, ReferenceResolver internalReferenceResolver,
			ReferenceResolver externalReferenceResolver) throws DeserialisationException;
}
