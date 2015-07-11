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
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.interop.Importer;
import org.workcraft.plugins.circuit.Circuit;
import org.workcraft.plugins.circuit.CircuitModelDescriptor;
import org.workcraft.plugins.circuit.CircuitUtils;
import org.workcraft.plugins.circuit.Contact.IOType;
import org.workcraft.plugins.circuit.FunctionComponent;
import org.workcraft.plugins.circuit.FunctionContact;
import org.workcraft.plugins.circuit.javacc.GenlibParser;
import org.workcraft.plugins.circuit.javacc.GenlibParser.Gate;
import org.workcraft.plugins.circuit.javacc.ParseException;
import org.workcraft.plugins.cpog.optimisation.BooleanFormula;
import org.workcraft.workspace.ModelEntry;

public class GenlibImporter implements Importer {

	static private final String TERM_DELIMITER = Pattern.quote("+");
	static private final String FACTOR_DELIMITER = Pattern.quote("*");

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
		GenlibParser parser = new GenlibParser(in);
		try {
			List<GenlibParser.Gate> gates = parser.parseGenlib();
			for (final GenlibParser.Gate gate: gates) {
				System.out.println(gate.name + " " + gate.function.name + "=" + gate.function.expression);
				instantiateGate(gate, circuit);
			}
		} catch (ParseException e) {
			throw new DeserialisationException(e);
		}
		return circuit;
	}

	private void instantiateGate(GenlibParser.Gate gate, final Circuit circuit) {
		final FunctionComponent component = new FunctionComponent();
		component.setModule(gate.name);
		circuit.add(component);
		FunctionContact contact = new FunctionContact();
		contact.setIOType(IOType.OUTPUT);
		component.add(contact);
		circuit.setName(contact, gate.function.name);
		String setFunction = getSetFunction(gate);
		String resetFunction = getResetFunction(gate);
		System.out.println("  set: " + setFunction);
		System.out.println("  reset: " + resetFunction);
		try {
			BooleanFormula setFormula = CircuitUtils.parseContactFuncton(circuit, component, setFunction);
			contact.setSetFunction(setFormula);
			BooleanFormula resetFormula = CircuitUtils.parseContactFuncton(circuit, component, resetFunction);
			contact.setResetFunction(resetFormula);
		} catch (org.workcraft.plugins.cpog.optimisation.javacc.ParseException e) {
			throw new RuntimeException(e);
		}
	}

	private String getSetFunction(Gate gate) {
		String result = null;
		if ( !gate.isSequential() ) {
			result = gate.function.expression;
		} else {
			for (String term: getTerms(gate.function.expression)) {
				if (!term.contains(gate.seq)) {
					if (result == null) {
						result = "";
					} else {
						result += "+";
					}
					result += term;
				}
			}
		}
		return result;
	}

	private String getResetFunction(Gate gate) {
		String result = null;
		if (gate.isSequential()) {
			for (String term: getTerms(gate.function.expression)) {
				if (term.contains(gate.seq)) {
					if (result == null) {
						result = "";
					} else {
						result += "+";
					}
					String clearedTerm = term.replaceAll(gate.seq + FACTOR_DELIMITER, "").replaceAll(FACTOR_DELIMITER + gate.seq, "").replaceAll(gate.seq, "");
					String invertedTerm = "!(" + clearedTerm + ")";
					result += invertedTerm;
				}
			}
		}
		return result;
	}

	private List<String> getTerms(String expression) {
		List<String> result = new LinkedList<>();
		int b = 0;
		String term = "";
		for (int i = 0; i < expression.length(); i++){
			char c = expression.charAt(i);
			if ((c == '+') && (b == 0)) {
				result.add(term);
				term = "";
			} else {
				term += c;
				if (c == '(') b++;
				if (c == ')') b--;
			}
		}
		if (b == 0) {
			result.add(term);
		}
		return result;
	}

}
