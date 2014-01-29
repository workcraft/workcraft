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

package org.workcraft.plugins.son.serialisation;

import org.w3c.dom.Element;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.plugins.serialisation.xml.VisualConnectionSerialiser;
import org.workcraft.plugins.son.connections.VisualSONConnection;
import org.workcraft.serialisation.ReferenceProducer;
import org.workcraft.serialisation.xml.NodeSerialiser;
import org.workcraft.util.XmlUtil;

public class VisualSONConnectionSerialiser extends VisualConnectionSerialiser {
	@Override
	public void serialise(Element element, Object object,
			ReferenceProducer internalReferences,
			ReferenceProducer externalReferences,
			NodeSerialiser nodeSerialiser) throws SerialisationException {

		VisualSONConnection vcon = (VisualSONConnection) object;

		element.setAttribute("first", internalReferences.getReference(vcon.getFirst()));
		element.setAttribute("second", internalReferences.getReference(vcon.getSecond()));
		element.setAttribute("ref", externalReferences.getReference(vcon.getReferencedConnection()));

		Element graphicElement = XmlUtil.createChildElement("graphic", element);

		nodeSerialiser.serialise(graphicElement, vcon.getGraphic());
	}
	@Override
	public String getClassName() {
		return VisualSONConnection.class.getName();
	}
}
