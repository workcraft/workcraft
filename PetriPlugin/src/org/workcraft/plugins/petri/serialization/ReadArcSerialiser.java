package org.workcraft.plugins.petri.serialization;

import org.w3c.dom.Element;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.plugins.petri.VisualReadArc;
import org.workcraft.serialisation.ReferenceProducer;
import org.workcraft.serialisation.xml.CustomXMLSerialiser;
import org.workcraft.serialisation.xml.NodeSerialiser;

public class ReadArcSerialiser implements CustomXMLSerialiser {
    @Override
    public String getClassName() {
        return VisualReadArc.class.getName();
    }

    @Override
    public void serialise(Element element, Object object,
            ReferenceProducer internalReferences,
            ReferenceProducer externalReferences, NodeSerialiser nodeSerialiser)
            throws SerialisationException {
        VisualReadArc arc = (VisualReadArc) object;

        element.setAttribute("first", internalReferences.getReference(arc.getFirst()));
        element.setAttribute("second", internalReferences.getReference(arc.getSecond()));

        element.setAttribute("refCon1", externalReferences.getReference(arc.getMathConsumingArc()));
        element.setAttribute("refCon2", externalReferences.getReference(arc.getMathProducingArc()));
    }

}
