package org.workcraft.plugins.serialisation.xml;

import org.w3c.dom.Element;
import org.workcraft.dom.visual.connections.Bezier;
import org.workcraft.dom.visual.connections.BezierControlPoint;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.serialisation.ReferenceResolver;
import org.workcraft.serialisation.xml.CustomXMLDeserialiser;
import org.workcraft.serialisation.xml.NodeFinaliser;
import org.workcraft.serialisation.xml.NodeInitialiser;
import org.workcraft.util.XmlUtils;

public class BezierDeserialiser implements CustomXMLDeserialiser {
    @Override
    public String getClassName() {
        return Bezier.class.getName();
    }

    @Override
    public void finaliseInstance(Element element, Object instance,
            ReferenceResolver internalReferenceResolver,
            ReferenceResolver externalReferenceResolver,
            NodeFinaliser nodeFinaliser) throws DeserialisationException {
        Bezier bezier = (Bezier) instance;
        for (BezierControlPoint cp : bezier.getBezierControlPoints()) {
            nodeFinaliser.finaliseInstance(cp);
        }
        bezier.finaliseControlPoints();
    }

    @Override
    public Object createInstance(Element element,
            ReferenceResolver externalReferenceResolver,
            Object... constructorParameters) {
        return new Bezier((VisualConnection) constructorParameters[0]);
    }

    @Override
    public void initInstance(Element element, Object instance,
            ReferenceResolver externalReferenceResolver,
            NodeInitialiser nodeInitialiser) throws DeserialisationException {

        Element cp1e = XmlUtils.getChildElement("cp1", element);
        Element cp2e = XmlUtils.getChildElement("cp2", element);

        BezierControlPoint cp1 = (BezierControlPoint) nodeInitialiser.initInstance(cp1e);
        BezierControlPoint cp2 = (BezierControlPoint) nodeInitialiser.initInstance(cp2e);

        ((Bezier) instance).initControlPoints(cp1, cp2);
    }
}
