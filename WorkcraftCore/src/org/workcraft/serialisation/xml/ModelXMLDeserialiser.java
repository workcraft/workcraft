package org.workcraft.serialisation.xml;

import org.w3c.dom.Element;
import org.workcraft.dom.Model;
import org.workcraft.dom.Node;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.serialisation.ReferenceResolver;
import org.workcraft.serialisation.References;

public interface ModelXMLDeserialiser extends XMLDeserialiser {
    Model deserialise(
            Element modelElement,
            Model underlyingModel,
            Node hierarchyRoot,
            References internalReferences,
            ReferenceResolver externalReferenceResolver
            )
            throws DeserialisationException;
}
