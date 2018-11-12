package org.workcraft.plugins.serialisation.xml;

import org.w3c.dom.Element;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.serialisation.ReferenceProducer;
import org.workcraft.serialisation.xml.CustomXMLSerialiser;
import org.workcraft.serialisation.xml.NodeSerialiser;
import org.workcraft.util.XmlUtils;

public class VisualConnectionSerialiser implements CustomXMLSerialiser<VisualConnection> {

    @Override
    public String getClassName() {
        return VisualConnection.class.getName();
    }

    @Override
    public void serialise(Element element, VisualConnection object, ReferenceProducer internalReferences,
            ReferenceProducer externalReferences, NodeSerialiser nodeSerialiser) throws SerialisationException {

        element.setAttribute("first", internalReferences.getReference(object.getFirst()));
        element.setAttribute("second", internalReferences.getReference(object.getSecond()));
        element.setAttribute("ref", externalReferences.getReference(object.getReferencedConnection()));

        Element graphicElement = XmlUtils.createChildElement("graphic", element);

        nodeSerialiser.serialise(graphicElement, object.getGraphic());
    }

}
