package org.workcraft.serialisation.xml;

import org.w3c.dom.Element;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.serialisation.ReferenceResolver;

public interface CustomXMLDeserialiser extends XMLDeserialiser {
    Object createInstance(Element element,
            ReferenceResolver externalReferenceResolver,
            Object... constructorParameters);

    void initInstance(Element element, Object instance,
            ReferenceResolver externalReferenceResolver,
            NodeInitialiser nodeInitialiser)
            throws DeserialisationException;

    void finaliseInstance(Element element, Object instance,
            ReferenceResolver internalReferenceResolver,
            ReferenceResolver externalReferenceResolver,
            NodeFinaliser nodeFinaliser)
            throws DeserialisationException;
}
