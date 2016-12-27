package org.workcraft.plugins.serialisation.xml;

import java.awt.geom.AffineTransform;

import org.w3c.dom.Element;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.serialisation.xml.BasicXMLDeserialiser;

public class AffineTransformDeserialiser implements BasicXMLDeserialiser {

    public String getClassName() {
        return AffineTransform.class.getName();
    }

    public Object deserialise(Element element) throws DeserialisationException {
        AffineTransform t = new AffineTransform();

        double[] matrix = new double[6];
        String[] values = element.getAttribute("matrix").split(" ");

        for (int i = 0; i < 6; i++) {
            matrix[i] = Double.parseDouble(values[i]);
        }

        t.setTransform(matrix[0], matrix[1], matrix[2], matrix[3], matrix[4], matrix[5]);
        return t;
    }
}
