package org.workcraft.serialisation;

class XMLSerialiserState {

    public final ReferenceProducer externalReferences;
    public final ReferenceProducer internalReferences;

    XMLSerialiserState(ReferenceProducer internalReferences, ReferenceProducer externalReferences) {
        this.externalReferences = externalReferences;
        this.internalReferences = internalReferences;
    }

}
