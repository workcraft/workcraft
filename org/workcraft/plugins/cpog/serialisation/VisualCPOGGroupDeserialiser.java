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

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.plugins.cpog.Encoding;
import org.workcraft.plugins.cpog.Variable;
import org.workcraft.plugins.cpog.VariableState;
import org.workcraft.plugins.cpog.VisualCPOGGroup;
import org.workcraft.serialisation.ReferenceResolver;
import org.workcraft.serialisation.xml.CustomXMLDeserialiser;
import org.workcraft.serialisation.xml.NodeFinaliser;
import org.workcraft.serialisation.xml.NodeInitialiser;

public class VisualCPOGGroupDeserialiser implements CustomXMLDeserialiser
{
	@Override
	public String getClassName()
	{
		return VisualCPOGGroup.class.getName();
	}

	@Override
	public void finaliseInstance(Element element, Object instance, ReferenceResolver internalReferenceResolver,
			ReferenceResolver externalReferenceResolver, NodeFinaliser nodeFinaliser) throws DeserialisationException
	{
		Encoding encoding = ((VisualCPOGGroup) instance).getEncoding();

		NodeList subelements = element.getElementsByTagName("encoding");

		for (int i = 0; i < subelements.getLength(); i++)
		{
			Element subelement = (Element) subelements.item(i);

			Variable var = (Variable) internalReferenceResolver.getObject(subelement.getAttribute("variable"));
			VariableState state = Enum.valueOf(VariableState.class, subelement.getAttribute("variable"));

			encoding.setState(var, state);
		}
	}

	@Override
	public Object createInstance(Element element, ReferenceResolver externalReferenceResolver,
			Object... constructorParameters)
	{
		return new Encoding();
	}

	@Override
	public void initInstance(Element element, Object instance, ReferenceResolver externalReferenceResolver,
			NodeInitialiser nodeInitialiser) throws DeserialisationException
	{

	}
}