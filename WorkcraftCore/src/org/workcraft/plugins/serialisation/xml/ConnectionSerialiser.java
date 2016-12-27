package org.workcraft.plugins.serialisation.xml;

import org.w3c.dom.Element;
import org.workcraft.dom.Connection;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.serialisation.ReferenceProducer;
import org.workcraft.serialisation.xml.CustomXMLSerialiser;
import org.workcraft.serialisation.xml.NodeSerialiser;

public class ConnectionSerialiser implements CustomXMLSerialiser {
    public String getClassName() {
        return MathConnection.class.getName();
    }

    public void serialise(Element element, Object object,
            ReferenceProducer internalReferences,
            ReferenceProducer incomingReferences,
            NodeSerialiser nodeSerialiser) throws SerialisationException {
        Connection con = (Connection) object;
        element.setAttribute("first", internalReferences.getReference(con.getFirst()));
        element.setAttribute("second", internalReferences.getReference(con.getSecond()));
    }
}
