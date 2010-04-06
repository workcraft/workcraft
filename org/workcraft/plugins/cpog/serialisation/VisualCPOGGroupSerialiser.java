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

package org.workcraft.plugins.cpog.serialisation;

import java.util.Map;

import org.w3c.dom.Element;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.plugins.cpog.Encoding;
import org.workcraft.plugins.cpog.Variable;
import org.workcraft.plugins.cpog.VariableState;
import org.workcraft.plugins.cpog.VisualCPOGGroup;
import org.workcraft.serialisation.ReferenceProducer;
import org.workcraft.serialisation.xml.CustomXMLSerialiser;
import org.workcraft.serialisation.xml.NodeSerialiser;

public class VisualCPOGGroupSerialiser implements CustomXMLSerialiser
{
	@Override
	public String getClassName()
	{
		return VisualCPOGGroup.class.getName();
	}

	@Override
	public void serialise(Element element, Object object, ReferenceProducer internalReferences,
			ReferenceProducer externalReferences, NodeSerialiser nodeSerialiser) throws SerialisationException
	{
		Encoding encoding = ((VisualCPOGGroup) object).getEncoding();

		Map<Variable, VariableState> states = encoding.getStates();

		for(Variable var : states.keySet())
		{
			VariableState state = states.get(var);

			Element subelement = element.getOwnerDocument().createElement("encoding");
			subelement.setAttribute("variable", internalReferences.getReference(var));
			subelement.setAttribute("state", internalReferences.getReference(state));
		}
	}
}