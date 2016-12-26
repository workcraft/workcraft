package org.workcraft.plugins.stg.serialisation;

import org.w3c.dom.Element;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.plugins.stg.VisualImplicitPlaceArc;
import org.workcraft.serialisation.ReferenceProducer;
import org.workcraft.serialisation.xml.CustomXMLSerialiser;
import org.workcraft.serialisation.xml.NodeSerialiser;

public class ImplicitPlaceArcSerialiser implements CustomXMLSerialiser {
    @Override
    public String getClassName() {
        return VisualImplicitPlaceArc.class.getName();
    }

    @Override
    public void serialise(Element element, Object object,
            ReferenceProducer internalReferences,
            ReferenceProducer externalReferences, NodeSerialiser nodeSerialiser)
            throws SerialisationException {
        VisualImplicitPlaceArc arc = (VisualImplicitPlaceArc) object;

        element.setAttribute("first", internalReferences.getReference(arc.getFirst()));
        element.setAttribute("second", internalReferences.getReference(arc.getSecond()));

        element.setAttribute("refCon1", externalReferences.getReference(arc.getRefCon1()));
        element.setAttribute("refCon2", externalReferences.getReference(arc.getRefCon2()));
        element.setAttribute("refPlace", externalReferences.getReference(arc.getImplicitPlace()));
    }
}