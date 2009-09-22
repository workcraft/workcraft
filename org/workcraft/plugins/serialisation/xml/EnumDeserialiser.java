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
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.serialisation.xml.BasicXMLDeserialiser;

public class EnumDeserialiser implements BasicXMLDeserialiser {
	@SuppressWarnings("unchecked")
	public Object deserialise(Element element) throws DeserialisationException {
		try {
			Class<? extends Enum> cls = Class.forName(element.getAttribute("enum-class")).asSubclass(Enum.class);
			return Enum.valueOf(cls, element.getAttribute("value"));
		} catch (ClassNotFoundException e) {
			throw new DeserialisationException(e);
		}
	}

	public String getClassName() {
		return Enum.class.getName();
	}
}
