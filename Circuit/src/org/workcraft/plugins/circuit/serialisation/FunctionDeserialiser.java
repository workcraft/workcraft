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

package org.workcraft.plugins.circuit.serialisation;

import org.w3c.dom.Element;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.plugins.circuit.Function;
import org.workcraft.plugins.cpog.serialisation.BooleanFunctionDeserialiser;
import org.workcraft.serialisation.ReferenceResolver;
import org.workcraft.serialisation.xml.CustomXMLDeserialiser;
import org.workcraft.serialisation.xml.NodeFinaliser;
import org.workcraft.serialisation.xml.NodeInitialiser;

public class FunctionDeserialiser implements CustomXMLDeserialiser
{
	@Override
	public String getClassName()
	{
		return Function.class.getName();
	}

	@Override
	public Object createInstance(Element element, ReferenceResolver externalReferenceResolver,
			Object... constructorParameters)
	{
		return new Function();
	}

	@Override
	public void finaliseInstance(Element element, Object instance,
			ReferenceResolver internalReferenceResolver,
			ReferenceResolver externalReferenceResolver,
			NodeFinaliser nodeFinaliser) throws DeserialisationException {
		Function function = ((Function)instance);
		function.setSetFunction(
				BooleanFunctionDeserialiser.readFormulaFromAttribute(element, internalReferenceResolver, FunctionSerialiser.SET_FUNCTION_ATTRIBUTE_NAME));
		function.setResetFunction(
				BooleanFunctionDeserialiser.readFormulaFromAttribute(element, internalReferenceResolver, FunctionSerialiser.RESET_FUNCTION_ATTRIBUTE_NAME));
	}

	@Override
	public void initInstance(Element element, Object instance,
			ReferenceResolver externalReferenceResolver,
			NodeInitialiser nodeInitialiser) throws DeserialisationException {
	}
}