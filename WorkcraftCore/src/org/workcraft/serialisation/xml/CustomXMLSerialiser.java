package org.workcraft.serialisation.xml;

import org.w3c.dom.Element;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.serialisation.ReferenceProducer;

public interface CustomXMLSerialiser extends XMLSerialiser {
    void serialise(Element element, Object object,
            ReferenceProducer internalReferences,
            ReferenceProducer externalReferences,
            NodeSerialiser nodeSerialiser) throws SerialisationException;
}
