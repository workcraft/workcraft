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
import org.workcraft.dom.math.MathNode;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.plugins.son.connections.SONConnection;
import org.workcraft.serialisation.ReferenceResolver;
import org.workcraft.serialisation.xml.CustomXMLDeserialiser;
import org.workcraft.serialisation.xml.NodeFinaliser;
import org.workcraft.serialisation.xml.NodeInitialiser;

public class SONConnectionDeserialiser implements CustomXMLDeserialiser {
	public String getClassName() {
		return SONConnection.class.getName();
	}

	@Override
	public void finaliseInstance(Element element, Object instance,
			ReferenceResolver internalReferenceResolver,
			ReferenceResolver externalReferenceResolver,
			NodeFinaliser nodeFinaliser) throws DeserialisationException {

		SONConnection con = (SONConnection)instance;
		MathNode first = (MathNode)internalReferenceResolver.getObject(element.getAttribute("first"));
		MathNode second = (MathNode)internalReferenceResolver.getObject(element.getAttribute("second"));
		String type = (String)internalReferenceResolver.getObject(element.getAttribute("contype"));

		con.setDependencies(first, second, type);
		con.setType(type);
	}

	@Override
	public Object createInstance(Element element,
			ReferenceResolver externalReferenceResolver,
			Object... constructorParameters) {
		return new SONConnection();
	}

	@Override
	public void initInstance(Element element, Object instance,
			ReferenceResolver externalReferenceResolver,
			NodeInitialiser nodeInitialiser) throws DeserialisationException {
	}
}