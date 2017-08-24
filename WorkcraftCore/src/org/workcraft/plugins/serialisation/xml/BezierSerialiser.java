package org.workcraft.plugins.serialisation.xml;

import org.w3c.dom.Element;
import org.workcraft.dom.visual.connections.Bezier;
import org.workcraft.dom.visual.connections.BezierControlPoint;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.serialisation.ReferenceProducer;
import org.workcraft.serialisation.xml.CustomXMLSerialiser;
import org.workcraft.serialisation.xml.NodeSerialiser;
import org.workcraft.util.XmlUtils;

public class BezierSerialiser implements CustomXMLSerialiser {

    @Override
    public void serialise(Element element, Object object,
            ReferenceProducer internalReferences,
            ReferenceProducer externalReferences, NodeSerialiser nodeSerialiser)
            throws SerialisationException {
        Bezier b = (Bezier) object;
        BezierControlPoint[] cp = b.getBezierControlPoints();

        Element cp1e = XmlUtils.createChildElement("cp1", element);
        Element cp2e = XmlUtils.createChildElement("cp2", element);

        nodeSerialiser.serialise(cp1e, cp[0]);
        nodeSerialiser.serialise(cp2e, cp[1]);
    }

    @Override
    public String getClassName() {
        return Bezier.class.getName();
    }
}
