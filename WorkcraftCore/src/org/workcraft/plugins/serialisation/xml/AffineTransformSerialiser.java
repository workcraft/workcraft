/*
*
* Copyright 2008,2009 Newcastle University
*
* This file is part of Workcraft.
*
* Workcraft is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* Workcraft is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with Workcraft.  If not, see <http://www.gnu.org/licenses/>.
*
*/

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
