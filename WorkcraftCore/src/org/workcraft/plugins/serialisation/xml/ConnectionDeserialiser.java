package org.workcraft.plugins.serialisation.xml;

import org.w3c.dom.Element;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.math.MathNode;
import org.workcraft.serialisation.ReferenceResolver;
import org.workcraft.serialisation.xml.CustomXMLDeserialiser;
import org.workcraft.serialisation.xml.NodeFinaliser;
import org.workcraft.serialisation.xml.NodeInitialiser;

public class ConnectionDeserialiser implements CustomXMLDeserialiser {

    public String getClassName() {
        return MathConnection.class.getName();
    }

    @Override
    public void finaliseInstance(Element element, Object instance,
            ReferenceResolver internalReferenceResolver,
            ReferenceResolver externalReferenceResolver,
            NodeFinaliser nodeFinaliser) {

        String firstRef = element.getAttribute("first");
        MathNode firstNode = (MathNode) internalReferenceResolver.getObject(firstRef);

        String secondRef = element.getAttribute("second");
        MathNode secondNode = (MathNode) internalReferenceResolver.getObject(secondRef);

        MathConnection connection = (MathConnection) instance;
        connection.setDependencies(firstNode, secondNode);
    }

    @Override
    public Object createInstance(Element element,
            ReferenceResolver externalReferenceResolver,
            Object... constructorParameters) {
        return new MathConnection();
    }

    @Override
    public void initInstance(Element element, Object instance,
            ReferenceResolver externalReferenceResolver,
            NodeInitialiser nodeInitialiser) {
    }

}