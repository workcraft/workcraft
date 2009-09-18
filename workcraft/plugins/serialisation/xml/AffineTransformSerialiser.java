package org.workcraft.plugins.serialisation.xml;

import java.awt.geom.AffineTransform;

import org.w3c.dom.Element;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.serialisation.xml.BasicXMLSerialiser;

public class AffineTransformSerialiser implements BasicXMLSerialiser {
	public String getClassName() {
		return AffineTransform.class.getName();
	}

	public void serialise(Element element, Object object)
			throws SerialisationException {
		AffineTransform t = (AffineTransform) object;

		double[] matrix = new double[6];
		t.getMatrix(matrix);

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