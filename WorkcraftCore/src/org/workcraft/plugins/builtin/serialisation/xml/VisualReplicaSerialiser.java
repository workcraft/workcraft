package org.workcraft.plugins.builtin.serialisation.xml;

import org.w3c.dom.Element;
import org.workcraft.dom.visual.VisualReplica;
import org.workcraft.serialisation.ReferenceProducer;
import org.workcraft.serialisation.CustomXMLSerialiser;
import org.workcraft.serialisation.NodeSerialiser;

public class VisualReplicaSerialiser implements CustomXMLSerialiser<VisualReplica> {

    @Override
    public String getClassName() {
        return VisualReplica.class.getName();
    }

    @Override
    public void serialise(Element element, VisualReplica object, ReferenceProducer internalReferences,
            ReferenceProducer externalReferences, NodeSerialiser nodeSerialiser) {

        String master = internalReferences.getReference(object.getMaster());
        element.setAttribute("master", master);
    }

}
