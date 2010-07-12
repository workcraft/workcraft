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
import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.connections.ConnectionGraphic;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.serialisation.ReferenceResolver;
import org.workcraft.serialisation.xml.CustomXMLDeserialiser;
import org.workcraft.serialisation.xml.NodeFinaliser;
import org.workcraft.serialisation.xml.NodeInitialiser;
import org.workcraft.util.XmlUtil;

public class VisualConnectionDeserialiser implements CustomXMLDeserialiser {
	@Override
	public String getClassName() {
		return VisualConnection.class.getName();
	}

	@Override
	public void finaliseInstance(Element element, Object instance,
			ReferenceResolver internalReferenceResolver,
			ReferenceResolver externalReferenceResolver,
			NodeFinaliser nodeFinaliser) throws DeserialisationException {

		VisualConnection vcon = (VisualConnection)instance;

		vcon.setVisualConnectionDependencies(
				(VisualComponent)internalReferenceResolver.getObject(element.getAttribute("first")),
				(VisualComponent)internalReferenceResolver.getObject(element.getAttribute("second")),
				(ConnectionGraphic)internalReferenceResolver.getObject(XmlUtil.getChildElement("graphic", element).getAttribute("ref")),
				(MathConnection)externalReferenceResolver.getObject(element.getAttribute("ref"))
		);

		nodeFinaliser.finaliseInstance(vcon.getGraphic());
	}

	@Override
	public Object createInstance(Element element,
			ReferenceResolver externalReferenceResolver,
			Object... constructorParameters) {
		return new VisualConnection();
	}

	@Override
	public void initInstance(Element element, Object instance,
			ReferenceResolver externalReferenceResolver,
			NodeInitialiser nodeInitialiser) throws DeserialisationException {
		nodeInitialiser.initInstance(XmlUtil.getChildElement("graphic", element), (VisualConnection)instance);
	}
}