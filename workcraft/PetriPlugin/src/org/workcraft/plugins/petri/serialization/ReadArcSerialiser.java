package org.workcraft.plugins.petri.serialization;

import org.w3c.dom.Element;
import org.workcraft.plugins.petri.VisualReadArc;
import org.workcraft.serialisation.ReferenceProducer;
import org.workcraft.serialisation.CustomXMLSerialiser;
import org.workcraft.serialisation.NodeSerialiser;

public class ReadArcSerialiser implements CustomXMLSerialiser<VisualReadArc> {

    @Override
    public String getClassName() {
        return VisualReadArc.class.getName();
    }

    @Override
    public void serialise(Element element, VisualReadArc object, ReferenceProducer internalReferences,
            ReferenceProducer externalReferences, NodeSerialiser nodeSerialiser) {

        element.setAttribute("first", internalReferences.getReference(object.getFirst()));
        element.setAttribute("second", internalReferences.getReference(object.getSecond()));

        element.setAttribute("refCon1", externalReferences.getReference(object.getMathConsumingArc()));
        element.setAttribute("refCon2", externalReferences.getReference(object.getMathProducingArc()));
    }

}
