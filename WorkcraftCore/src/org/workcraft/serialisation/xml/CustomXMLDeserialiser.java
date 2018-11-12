package org.workcraft.serialisation.xml;

import org.w3c.dom.Element;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.serialisation.ReferenceResolver;

public interface CustomXMLDeserialiser<T> extends XMLDeserialiser {

    T createInstance(Element element, ReferenceResolver externalReferenceResolver, Object... constructorParameters);

    void initInstance(Element element, T instance, ReferenceResolver externalReferenceResolver,
            NodeInitialiser nodeInitialiser) throws DeserialisationException;

    void finaliseInstance(Element element, T instance, ReferenceResolver internalReferenceResolver,
            ReferenceResolver externalReferenceResolver, NodeFinaliser nodeFinaliser) throws DeserialisationException;
}
