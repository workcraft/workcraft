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

        for (int i=0; i<6; i++)
            matrix[i] = Double.parseDouble(values[i]);

        t.setTransform(matrix[0], matrix[1], matrix[2], matrix[3], matrix[4], matrix[5]);
        return t;
    }
}
