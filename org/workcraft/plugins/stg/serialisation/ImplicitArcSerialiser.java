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

package org.workcraft.plugins.stg.serialisation;

import org.w3c.dom.Element;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.plugins.stg.ImplicitPlaceArc;
import org.workcraft.serialisation.ReferenceProducer;
import org.workcraft.serialisation.xml.CustomXMLSerialiser;
import org.workcraft.serialisation.xml.NodeSerialiser;

public class ImplicitArcSerialiser implements CustomXMLSerialiser {
	@Override
	public String getClassName() {
		return ImplicitPlaceArc.class.getName();
	}

	@Override
	public void serialise(Element element, Object object,
			ReferenceProducer internalReferences,
			ReferenceProducer externalReferences, NodeSerialiser nodeSerialiser)
			throws SerialisationException {
		ImplicitPlaceArc arc = (ImplicitPlaceArc) object;

		element.setAttribute("first", internalReferences.getReference(arc.getFirst()));
		element.setAttribute("second", internalReferences.getReference(arc.getSecond()));

		element.setAttribute("refCon1", externalReferences.getReference(arc.getRefCon1()));
		element.setAttribute("refCon2", externalReferences.getReference(arc.getRefCon2()));
		element.setAttribute("refPlace", externalReferences.getReference(arc.getImplicitPlace()));
	}
}