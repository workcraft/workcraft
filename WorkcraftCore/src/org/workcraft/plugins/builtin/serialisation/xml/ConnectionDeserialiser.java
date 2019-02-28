package org.workcraft.plugins.builtin.serialisation.xml;

import org.w3c.dom.Element;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.math.MathNode;
import org.workcraft.serialisation.ReferenceResolver;
import org.workcraft.serialisation.CustomXMLDeserialiser;
import org.workcraft.serialisation.NodeFinaliser;
import org.workcraft.serialisation.NodeInitialiser;

public class ConnectionDeserialiser implements CustomXMLDeserialiser<MathConnection> {

    @Override
    public String getClassName() {
        return MathConnection.class.getName();
    }

    @Override
    public void finaliseInstance(Element element, MathConnection instance, ReferenceResolver internalReferenceResolver,
            ReferenceResolver externalReferenceResolver, NodeFinaliser nodeFinaliser) {

        String firstRef = element.getAttribute("first");
        MathNode firstNode = (MathNode) internalReferenceResolver.getObject(firstRef);

        String secondRef = element.getAttribute("second");
        MathNode secondNode = (MathNode) internalReferenceResolver.getObject(secondRef);

        instance.setDependencies(firstNode, secondNode);
    }

    @Override
    public MathConnection createInstance(Element element, ReferenceResolver externalReferenceResolver,
            Object... constructorParameters) {

        return new MathConnection();
    }

    @Override
    public void initInstance(Element element, MathConnection instance, ReferenceResolver externalReferenceResolver,
            NodeInitialiser nodeInitialiser) {
    }

}