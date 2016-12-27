package org.workcraft.plugins.graph.serialisation;

import org.w3c.dom.Element;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.plugins.graph.Vertex;
import org.workcraft.serialisation.ReferenceProducer;
import org.workcraft.serialisation.xml.CustomXMLSerialiser;
import org.workcraft.serialisation.xml.NodeSerialiser;

public class VertexSerialiser implements CustomXMLSerialiser {
    @Override
    public String getClassName() {
        return Vertex.class.getName();
    }

    @Override
    public void serialise(Element element, Object object,
            ReferenceProducer internalReferences,
            ReferenceProducer externalReferences, NodeSerialiser nodeSerialiser)
            throws SerialisationException {
        Vertex vertex = (Vertex) object;

        element.setAttribute("symbol", internalReferences.getReference(vertex.getSymbol()));
    }
}