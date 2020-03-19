package org.workcraft.plugins.graph.serialisation;

import org.w3c.dom.Element;
import org.workcraft.plugins.graph.Vertex;
import org.workcraft.serialisation.ReferenceProducer;
import org.workcraft.serialisation.CustomXMLSerialiser;
import org.workcraft.serialisation.NodeSerialiser;

public class VertexSerialiser implements CustomXMLSerialiser<Vertex> {

    @Override
    public String getClassName() {
        return Vertex.class.getName();
    }

    @Override
    public void serialise(Element element, Vertex object, ReferenceProducer internalReferences,
            ReferenceProducer externalReferences, NodeSerialiser nodeSerialiser) {

        element.setAttribute("symbol", internalReferences.getReference(object.getSymbol()));
    }

}