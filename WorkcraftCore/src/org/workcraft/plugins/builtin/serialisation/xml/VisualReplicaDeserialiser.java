package org.workcraft.plugins.builtin.serialisation.xml;

import org.w3c.dom.Element;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualReplica;
import org.workcraft.serialisation.ReferenceResolver;
import org.workcraft.serialisation.CustomXMLDeserialiser;
import org.workcraft.serialisation.NodeFinaliser;
import org.workcraft.serialisation.NodeInitialiser;

public class VisualReplicaDeserialiser implements CustomXMLDeserialiser<VisualReplica> {

    @Override
    public String getClassName() {
        return VisualReplica.class.getName();
    }

    @Override
    public void finaliseInstance(Element element, VisualReplica instance, ReferenceResolver internalReferenceResolver,
            ReferenceResolver externalReferenceResolver, NodeFinaliser nodeFinaliser) {

        instance.setMaster((VisualComponent) internalReferenceResolver.getObject(element.getAttribute("master")));
    }

    @Override
    public VisualReplica createInstance(Element element, ReferenceResolver externalReferenceResolver,
            Object... constructorParameters) {

        return new VisualReplica();
    }

    @Override
    public void initInstance(Element element, VisualReplica instance, ReferenceResolver externalReferenceResolver,
            NodeInitialiser nodeInitialiser) {

    }

}
