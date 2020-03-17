package org.workcraft.plugins.builtin.serialisation.xml;

import org.w3c.dom.Element;
import org.workcraft.dom.visual.connections.Bezier;
import org.workcraft.dom.visual.connections.BezierControlPoint;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.serialisation.ReferenceProducer;
import org.workcraft.serialisation.CustomXMLSerialiser;
import org.workcraft.serialisation.NodeSerialiser;
import org.workcraft.utils.XmlUtils;

public class BezierSerialiser implements CustomXMLSerialiser<Bezier> {

    @Override
    public String getClassName() {
        return Bezier.class.getName();
    }

    @Override
    public void serialise(Element element, Bezier object, ReferenceProducer internalReferences,
            ReferenceProducer externalReferences, NodeSerialiser nodeSerialiser) throws SerialisationException {

        BezierControlPoint[] cp = object.getBezierControlPoints();

        Element cp1e = XmlUtils.createChildElement("cp1", element);
        Element cp2e = XmlUtils.createChildElement("cp2", element);

        nodeSerialiser.serialise(cp1e, cp[0]);
        nodeSerialiser.serialise(cp2e, cp[1]);
    }

}
