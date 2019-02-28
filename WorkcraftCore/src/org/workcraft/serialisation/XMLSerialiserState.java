package org.workcraft.serialisation;

class XMLSerialiserState {
    ReferenceProducer externalReferences = null;
    ReferenceProducer internalReferences = null;

    XMLSerialiserState(ReferenceProducer internalReferences, ReferenceProducer externalReferences) {
        this.externalReferences = externalReferences;
        this.internalReferences = internalReferences;
    }
}
