package org.workcraft.serialisation;

import org.w3c.dom.Element;
import org.workcraft.exceptions.DeserialisationException;

public interface CustomXMLDeserialiser<T> extends XMLDeserialiser {

    T createInstance(Element element, ReferenceResolver externalReferenceResolver, Object... constructorParameters);

    void initInstance(Element element, T instance, ReferenceResolver externalReferenceResolver,
            NodeInitialiser nodeInitialiser) throws DeserialisationException;

    void finaliseInstance(Element element, T instance, ReferenceResolver internalReferenceResolver,
            ReferenceResolver externalReferenceResolver, NodeFinaliser nodeFinaliser) throws DeserialisationException;
}
