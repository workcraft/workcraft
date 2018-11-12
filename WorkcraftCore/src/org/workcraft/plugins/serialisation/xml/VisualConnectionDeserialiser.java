package org.workcraft.plugins.serialisation.xml;

import org.w3c.dom.Element;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.dom.visual.connections.ConnectionGraphic;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.serialisation.ReferenceResolver;
import org.workcraft.serialisation.xml.CustomXMLDeserialiser;
import org.workcraft.serialisation.xml.NodeFinaliser;
import org.workcraft.serialisation.xml.NodeInitialiser;
import org.workcraft.util.XmlUtils;

public class VisualConnectionDeserialiser implements CustomXMLDeserialiser<VisualConnection> {

    @Override
    public String getClassName() {
        return VisualConnection.class.getName();
    }

    @Override
    public void finaliseInstance(Element element, VisualConnection instance, ReferenceResolver internalReferenceResolver,
            ReferenceResolver externalReferenceResolver, NodeFinaliser nodeFinaliser) throws DeserialisationException {

        String graphicAttribute = XmlUtils.getChildElement("graphic", element).getAttribute("ref");

        instance.setVisualConnectionDependencies(
                (VisualNode) internalReferenceResolver.getObject(element.getAttribute("first")),
                (VisualNode) internalReferenceResolver.getObject(element.getAttribute("second")),
                (ConnectionGraphic) internalReferenceResolver.getObject(graphicAttribute),
                (MathConnection) externalReferenceResolver.getObject(element.getAttribute("ref"))
        );

        nodeFinaliser.finaliseInstance(instance.getGraphic());
    }

    @Override
    public VisualConnection createInstance(Element element, ReferenceResolver externalReferenceResolver,
            Object... constructorParameters) {

        return new VisualConnection();
    }

    @Override
    public void initInstance(Element element, VisualConnection instance, ReferenceResolver externalReferenceResolver,
            NodeInitialiser nodeInitialiser) throws DeserialisationException {

        nodeInitialiser.initInstance(XmlUtils.getChildElement("graphic", element), instance);
    }

}