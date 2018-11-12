package org.workcraft.plugins.serialisation.xml;

import org.w3c.dom.Element;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.serialisation.xml.BasicXMLSerialiser;

import java.awt.geom.AffineTransform;

public class AffineTransformSerialiser implements BasicXMLSerialiser<AffineTransform> {

    @Override
    public String getClassName() {
        return AffineTransform.class.getName();
    }

    @Override
    public void serialise(Element element, AffineTransform object) throws SerialisationException {
        double[] matrix = new double[6];
        object.getMatrix(matrix);

        element.setAttribute("matrix", String.format("%s %s %s %s %s %s",
                DoubleSerialiser.doubleToString(matrix[0]),
                DoubleSerialiser.doubleToString(matrix[1]),
                DoubleSerialiser.doubleToString(matrix[2]),
                DoubleSerialiser.doubleToString(matrix[3]),
                DoubleSerialiser.doubleToString(matrix[4]),
                DoubleSerialiser.doubleToString(matrix[5]))
        );
    }

}
