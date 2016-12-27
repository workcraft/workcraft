package org.workcraft.plugins.graph.serialisation;

import org.w3c.dom.Element;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.plugins.graph.Symbol;
import org.workcraft.plugins.graph.Vertex;
import org.workcraft.serialisation.ReferenceResolver;
import org.workcraft.serialisation.xml.CustomXMLDeserialiser;
import org.workcraft.serialisation.xml.NodeFinaliser;
import org.workcraft.serialisation.xml.NodeInitialiser;

public class VertexDeserialiser implements CustomXMLDeserialiser {
    @Override
    public String getClassName() {
        return Vertex.class.getName();
    }

    @Override
    public void finaliseInstance(Element element, Object instance,
            ReferenceResolver internalReferenceResolver,
            ReferenceResolver externalReferenceResolver,
            NodeFinaliser nodeFinaliser) throws DeserialisationException {

        Symbol symbol = (Symbol) internalReferenceResolver.getObject(element.getAttribute("symbol"));

        Vertex vertex = (Vertex) instance;
        vertex.setSymbol(symbol);
    }

    @Override
    public Object createInstance(Element element,
            ReferenceResolver externalReferenceResolver,
            Object... constructorParameters) {
        return new Vertex();
    }

    @Override
    public void initInstance(Element element, Object instance,
            ReferenceResolver externalReferenceResolver,
            NodeInitialiser nodeInitialiser) throws DeserialisationException {

    }
}