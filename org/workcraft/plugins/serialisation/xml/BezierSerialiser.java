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

import org.w3c.dom.Element;
import org.workcraft.dom.visual.connections.Bezier;
import org.workcraft.dom.visual.connections.BezierControlPoint;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.serialisation.ReferenceProducer;
import org.workcraft.serialisation.xml.CustomXMLSerialiser;
import org.workcraft.serialisation.xml.NodeSerialiser;
import org.workcraft.util.XmlUtil;

public class BezierSerialiser implements CustomXMLSerialiser {

	@Override
	public void serialise(Element element, Object object,
			ReferenceProducer internalReferences,
			ReferenceProducer externalReferences, NodeSerialiser nodeSerialiser)
			throws SerialisationException {
		Bezier b = (Bezier)object;
		BezierControlPoint[] cp = b.getControlPoints();

		Element cp1e = XmlUtil.createChildElement("cp1", element);
		Element cp2e = XmlUtil.createChildElement("cp2", element);

		nodeSerialiser.serialise(cp1e, cp[0]);
		nodeSerialiser.serialise(cp2e, cp[1]);
	}

	@Override
	public String getClassName() {
		return Bezier.class.getName();
	}
}
