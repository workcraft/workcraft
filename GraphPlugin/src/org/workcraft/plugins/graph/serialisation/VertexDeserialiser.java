package org.workcraft.plugins.graph.serialisation;

import org.w3c.dom.Element;
import org.workcraft.plugins.graph.Symbol;
import org.workcraft.plugins.graph.Vertex;
import org.workcraft.serialisation.ReferenceResolver;
import org.workcraft.serialisation.CustomXMLDeserialiser;
import org.workcraft.serialisation.NodeFinaliser;
import org.workcraft.serialisation.NodeInitialiser;

public class VertexDeserialiser implements CustomXMLDeserialiser<Vertex> {
    @Override
    public String getClassName() {
        return Vertex.class.getName();
    }

    @Override
    public void finaliseInstance(Element element, Vertex instance, ReferenceResolver internalReferenceResolver,
            ReferenceResolver externalReferenceResolver, NodeFinaliser nodeFinaliser) {

        Symbol symbol = (Symbol) internalReferenceResolver.getObject(element.getAttribute("symbol"));
        instance.setSymbol(symbol);
    }

    @Override
    public Vertex createInstance(Element element, ReferenceResolver externalReferenceResolver,
            Object... constructorParameters) {

        return new Vertex();
    }

    @Override
    public void initInstance(Element element, Vertex instance, ReferenceResolver externalReferenceResolver,
            NodeInitialiser nodeInitialiser) {

    }

}