package org.workcraft.serialisation;

import org.w3c.dom.Element;
import org.workcraft.exceptions.SerialisationException;

public interface CustomXMLSerialiser<T> extends XMLSerialiser {
    void serialise(Element element, T object, ReferenceProducer internalReferences,
            ReferenceProducer externalReferences, NodeSerialiser nodeSerialiser) throws SerialisationException;
}
