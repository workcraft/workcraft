package org.workcraft.plugins.builtin.serialisation.xml;

import org.w3c.dom.Element;
import org.workcraft.dom.visual.connections.Bezier;
import org.workcraft.dom.visual.connections.BezierControlPoint;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.serialisation.ReferenceResolver;
import org.workcraft.serialisation.CustomXMLDeserialiser;
import org.workcraft.serialisation.NodeFinaliser;
import org.workcraft.serialisation.NodeInitialiser;
import org.workcraft.utils.XmlUtils;

public class BezierDeserialiser implements CustomXMLDeserialiser<Bezier> {

    @Override
    public String getClassName() {
        return Bezier.class.getName();
    }

    @Override
    public void finaliseInstance(Element element, Bezier instance,
            ReferenceResolver internalReferenceResolver,
            ReferenceResolver externalReferenceResolver,
            NodeFinaliser nodeFinaliser) throws DeserialisationException {
        for (BezierControlPoint cp : instance.getBezierControlPoints()) {
            nodeFinaliser.finaliseInstance(cp);
        }
        instance.finaliseControlPoints();
    }

    @Override
    public Bezier createInstance(Element element, ReferenceResolver externalReferenceResolver,
            Object... constructorParameters) {

        return new Bezier((VisualConnection) constructorParameters[0]);
    }

    @Override
    public void initInstance(Element element, Bezier instance, ReferenceResolver externalReferenceResolver,
            NodeInitialiser nodeInitialiser) throws DeserialisationException {

        Element cp1e = XmlUtils.getChildElement("cp1", element);
        Element cp2e = XmlUtils.getChildElement("cp2", element);

        BezierControlPoint cp1 = (BezierControlPoint) nodeInitialiser.initInstance(cp1e);
        BezierControlPoint cp2 = (BezierControlPoint) nodeInitialiser.initInstance(cp2e);

        instance.initControlPoints(cp1, cp2);
    }

}
