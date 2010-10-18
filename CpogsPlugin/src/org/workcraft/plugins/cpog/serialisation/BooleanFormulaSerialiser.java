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
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.plugins.cpog.optimisation.BooleanFormula;
import org.workcraft.plugins.cpog.optimisation.BooleanVariable;
import org.workcraft.plugins.cpog.optimisation.booleanvisitors.FormulaToString;
import org.workcraft.plugins.cpog.optimisation.booleanvisitors.FormulaToString.PrinterSuite;
import org.workcraft.plugins.cpog.optimisation.booleanvisitors.FormulaToString.Void;
import org.workcraft.serialisation.ReferenceProducer;
import org.workcraft.serialisation.xml.CustomXMLSerialiser;
import org.workcraft.serialisation.xml.NodeSerialiser;

public abstract class BooleanFormulaSerialiser implements CustomXMLSerialiser
{
	@Override
	public void serialise(Element element, Object object, final ReferenceProducer internalReferences,
			ReferenceProducer externalReferences, NodeSerialiser nodeSerialiser) throws SerialisationException
	{
		BooleanFormula formula = getFormula(object);
		String attributeName = "formula";

		writeFormulaAttribute(element, internalReferences, formula, attributeName);
	}

	public static void writeFormulaAttribute(Element element, final ReferenceProducer internalReferences, BooleanFormula formula, String attributeName) {
		PrinterSuite printers = new FormulaToString.PrinterSuite();
		printers.vars = new FormulaToString.VariablePrinter(){
			@Override
			public Void visit(BooleanVariable node) {
				append("var_"+internalReferences.getReference(node));
				return null;
			}
		};

		printers.init();

		formula.accept(printers.iff);
		String string = printers.builder.toString();

		element.setAttribute(attributeName, string);
	}

	protected abstract BooleanFormula getFormula(Object serialisee);
}