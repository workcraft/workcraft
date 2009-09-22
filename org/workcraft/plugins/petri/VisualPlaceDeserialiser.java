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

package org.workcraft.plugins.petri;

import org.w3c.dom.Element;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.serialisation.ReferenceResolver;
import org.workcraft.serialisation.xml.CustomXMLDeserialiser;

public class VisualPlaceDeserialiser implements CustomXMLDeserialiser {

	public void finaliseInstance(Element element, Object instance,
			ReferenceResolver internalReferenceResolver,
			ReferenceResolver externalReferenceResolver)
			throws DeserialisationException {

	}

	public Object initInstance(Element element,
			ReferenceResolver externalReferenceResolver)
			throws DeserialisationException {
		return new VisualPlace ( ((Place) externalReferenceResolver.getObject(element.getAttribute("ref"))) );
	}


	public String getClassName() {
		return VisualPlace.class.getName();
	}

}
