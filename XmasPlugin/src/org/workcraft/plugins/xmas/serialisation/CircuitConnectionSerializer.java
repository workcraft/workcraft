package org.workcraft.plugins.xmas.serialisation;

import org.w3c.dom.Element;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.plugins.xmas.components.VisualXmasConnection;
import org.workcraft.serialisation.ReferenceProducer;
import org.workcraft.serialisation.xml.CustomXMLSerialiser;
import org.workcraft.serialisation.xml.NodeSerialiser;

public class CircuitConnectionSerializer implements CustomXMLSerialiser{

    @Override
    public void serialise(Element element, Object object,
            ReferenceProducer internalReferences,
            ReferenceProducer externalReferences, NodeSerialiser nodeSerialiser)
            throws SerialisationException {

/*        VisualCircuitConnection vc = (VisualCircuitConnection) object;
        element.setAttribute("first", internalReferences.getReference(vc.getFirst()));
        element.setAttribute("second", internalReferences.getReference(vc.getSecond()));
        */
    }

    @Override
    public String getClassName() {
        return VisualXmasConnection.class.getName();
    }

}
