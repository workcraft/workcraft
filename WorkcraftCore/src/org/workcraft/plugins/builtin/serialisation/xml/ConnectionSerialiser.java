package org.workcraft.plugins.builtin.serialisation.xml;

import org.w3c.dom.Element;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.serialisation.ReferenceProducer;
import org.workcraft.serialisation.CustomXMLSerialiser;
import org.workcraft.serialisation.NodeSerialiser;

public class ConnectionSerialiser implements CustomXMLSerialiser<MathConnection> {

    @Override
    public String getClassName() {
        return MathConnection.class.getName();
    }

    @Override
    public void serialise(Element element, MathConnection object, ReferenceProducer internalReferences,
            ReferenceProducer incomingReferences, NodeSerialiser nodeSerialiser) throws SerialisationException {

        element.setAttribute("first", internalReferences.getReference(object.getFirst()));
        element.setAttribute("second", internalReferences.getReference(object.getSecond()));
    }

}
