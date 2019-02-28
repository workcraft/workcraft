package org.workcraft.plugins.policy.serialisation;

import org.w3c.dom.Element;
import org.workcraft.plugins.policy.Bundle;
import org.workcraft.plugins.policy.BundledTransition;
import org.workcraft.serialisation.ReferenceProducer;
import org.workcraft.serialisation.CustomXMLSerialiser;
import org.workcraft.serialisation.NodeSerialiser;

public class BundleSerialiser implements CustomXMLSerialiser<Bundle> {

    @Override
    public String getClassName() {
        return Bundle.class.getName();
    }

    @Override
    public void serialise(Element element, Bundle object, ReferenceProducer internalReferences,
            ReferenceProducer externalReferences, NodeSerialiser nodeSerialiser) {

        String s = "";
        for (BundledTransition t: object.getTransitions()) {
            if (s != "") {
                s += ", ";
            }
            s += internalReferences.getReference(t);
        }
        element.setAttribute("transitions", s);
    }

}
