package org.workcraft.serialisation.xml;

import org.workcraft.serialisation.ReferenceProducer;

class XMLSerialiserState {
    ReferenceProducer externalReferences = null;
    ReferenceProducer internalReferences = null;

    XMLSerialiserState(ReferenceProducer internalReferences, ReferenceProducer externalReferences) {
        this.externalReferences = externalReferences;
        this.internalReferences = internalReferences;
    }
}
