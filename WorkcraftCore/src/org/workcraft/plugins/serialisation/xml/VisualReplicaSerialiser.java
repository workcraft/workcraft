package org.workcraft.plugins.serialisation.xml;

import org.w3c.dom.Element;
import org.workcraft.dom.visual.VisualReplica;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.serialisation.ReferenceProducer;
import org.workcraft.serialisation.xml.CustomXMLSerialiser;
import org.workcraft.serialisation.xml.NodeSerialiser;

public class VisualReplicaSerialiser implements CustomXMLSerialiser {

    @Override
    public void serialise(Element element, Object object,
            ReferenceProducer internalReferences,
            ReferenceProducer externalReferences,
            NodeSerialiser nodeSerialiser) throws SerialisationException {

        VisualReplica replica = (VisualReplica) object;
        String master = internalReferences.getReference(replica.getMaster());
        element.setAttribute("master", master);
    }

    @Override
    public String getClassName() {
        return VisualReplica.class.getName();
    }
}
