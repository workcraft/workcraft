package org.workcraft.plugins.policy.serialisation;

import org.w3c.dom.Element;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.plugins.policy.Bundle;
import org.workcraft.plugins.policy.BundledTransition;
import org.workcraft.serialisation.ReferenceProducer;
import org.workcraft.serialisation.xml.CustomXMLSerialiser;
import org.workcraft.serialisation.xml.NodeSerialiser;

public class BundleSerialiser implements CustomXMLSerialiser {
    public void serialise(Element element, Object object,
            ReferenceProducer internalReferences,
            ReferenceProducer externalReferences,
            NodeSerialiser nodeSerialiser) throws SerialisationException {

        Bundle b = (Bundle) object;
        String s = "";
        for (BundledTransition t: b.getTransitions()) {
            if (s != "") {
                s += ", ";
            }
            s += internalReferences.getReference(t);
        }
        element.setAttribute("transitions", s);
    }

    public String getClassName() {
        return Bundle.class.getName();
    }
}
