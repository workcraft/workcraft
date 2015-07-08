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

package org.workcraft.plugins.circuit.interop;

import java.io.File;
import java.io.InputStream;
import java.util.List;

import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.exceptions.FormatException;
import org.workcraft.interop.Importer;
import org.workcraft.plugins.circuit.Circuit;
import org.workcraft.plugins.circuit.CircuitModelDescriptor;
import org.workcraft.plugins.circuit.Contact.IOType;
import org.workcraft.plugins.circuit.FunctionComponent;
import org.workcraft.plugins.circuit.FunctionContact;
import org.workcraft.plugins.circuit.javacc.GenlibParser;
import org.workcraft.plugins.circuit.javacc.ParseException;
import org.workcraft.plugins.cpog.optimisation.BooleanFormula;
import org.workcraft.plugins.cpog.optimisation.javacc.BooleanParser;
import org.workcraft.util.Func;
import org.workcraft.workspace.ModelEntry;

public class GenlibImporter implements Importer {

	@Override
	public boolean accept(File file) {
		return file.getName().endsWith(".lib");
	}

	@Override
	public String getDescription() {
		return "Genlib (.lib)";
	}

	@Override
	public ModelEntry importFrom(InputStream in) throws DeserialisationException {
		return new ModelEntry(new CircuitModelDescriptor(), importGenlib(in));
	}

	public Circuit importGenlib(InputStream in) throws DeserialisationException {
		final Circuit circuit = new Circuit();
		try {
			GenlibParser parser = new GenlibParser(in);
			List<GenlibParser.Gate> gates = parser.parseGenlib();
			for (final GenlibParser.Gate gate: gates) {
				System.out.println(gate.name + " " + gate.function.name + "=" + gate.function.expression);

				final FunctionComponent component = new FunctionComponent();
				component.setModule(gate.name);
				circuit.add(component);
				FunctionContact contact = new FunctionContact();
				contact.setIOType(IOType.OUTPUT);
				component.add(contact);
				circuit.setName(contact, gate.function.name);

					try {
						contact.setSetFunction(BooleanParser.parse(gate.function.expression,
								new Func<String, BooleanFormula>() {
									@Override
									public BooleanFormula eval(String name) {
										FunctionContact input = (FunctionContact)circuit.getNodeByReference(component, name);
										if (input == null) {
											input = new FunctionContact();
											input.setIOType(IOType.INPUT);
											component.add(input);
											circuit.setName(input, name);
										}
										return input;
									}
								}));
					} catch (org.workcraft.plugins.cpog.optimisation.javacc.ParseException e) {
					}
			}
		} catch (FormatException e) {
			throw new DeserialisationException(e);
		} catch (ParseException e) {
			throw new DeserialisationException(e);
		}
		return circuit;
	}

}
