package org.workcraft.plugins.policy.serialisation;

import org.w3c.dom.Element;
import org.workcraft.plugins.policy.Locality;
import org.workcraft.plugins.policy.VisualLocality;
import org.workcraft.serialisation.ReferenceProducer;
import org.workcraft.serialisation.CustomXMLSerialiser;
import org.workcraft.serialisation.NodeSerialiser;

public class VisualLocalitySerialiser implements CustomXMLSerialiser<VisualLocality> {

    @Override
    public String getClassName() {
        return VisualLocality.class.getName();
    }

    @Override
    public void serialise(Element element, VisualLocality object, ReferenceProducer internalReferences,
            ReferenceProducer externalReferences, NodeSerialiser nodeSerialiser) {

        Locality locality = object.getLocality();
        String ref = externalReferences.getReference(locality);
        element.setAttribute("ref", ref);
    }

}
