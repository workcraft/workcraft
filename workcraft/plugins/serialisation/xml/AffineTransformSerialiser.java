package org.workcraft.plugins.serialisation.xml;

import java.awt.geom.AffineTransform;

import org.w3c.dom.Element;
import org.workcraft.framework.exceptions.SerialisationException;
import org.workcraft.framework.serialisation.xml.BasicXMLSerialiser;

public class AffineTransformSerialiser implements BasicXMLSerialiser {
	public String getClassName() {
		return AffineTransform.class.getName();
	}

	public void serialise(Element element, Object object)
			throws SerialisationException {
		AffineTransform t = (AffineTransform) object;

		double[] matrix = new double[6];
		t.getMatrix(matrix);

		element.setAttribute("matrix", String.format("%f %f %f %f %f %f",
				matrix[0], matrix[1], matrix[2],
				matrix[3], matrix[4], matrix[5])
			);
	}
}