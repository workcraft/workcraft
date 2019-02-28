package org.workcraft.plugins.stg.serialisation;

import org.w3c.dom.Element;
import org.workcraft.plugins.stg.VisualImplicitPlaceArc;
import org.workcraft.serialisation.ReferenceProducer;
import org.workcraft.serialisation.CustomXMLSerialiser;
import org.workcraft.serialisation.NodeSerialiser;

public class ImplicitPlaceArcSerialiser implements CustomXMLSerialiser<VisualImplicitPlaceArc> {

    @Override
    public String getClassName() {
        return VisualImplicitPlaceArc.class.getName();
    }

    @Override
    public void serialise(Element element, VisualImplicitPlaceArc object, ReferenceProducer internalReferences,
            ReferenceProducer externalReferences, NodeSerialiser nodeSerialiser) {

        element.setAttribute("first", internalReferences.getReference(object.getFirst()));
        element.setAttribute("second", internalReferences.getReference(object.getSecond()));

        element.setAttribute("refCon1", externalReferences.getReference(object.getRefCon1()));
        element.setAttribute("refCon2", externalReferences.getReference(object.getRefCon2()));
        element.setAttribute("refPlace", externalReferences.getReference(object.getImplicitPlace()));
    }

}