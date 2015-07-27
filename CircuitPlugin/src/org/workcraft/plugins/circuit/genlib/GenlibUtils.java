package org.workcraft.plugins.circuit.genlib;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import org.workcraft.exceptions.ArgumentException;
import org.workcraft.plugins.circuit.Circuit;
import org.workcraft.plugins.circuit.CircuitUtils;
import org.workcraft.plugins.circuit.Contact.IOType;
import org.workcraft.plugins.circuit.FunctionComponent;
import org.workcraft.plugins.circuit.FunctionContact;
import org.workcraft.plugins.cpog.optimisation.BooleanFormula;
import org.workcraft.plugins.shared.CommonDebugSettings;

public class GenlibUtils {
	static private final String TERM_DELIMITER = Pattern.quote("+");
	static private final String FACTOR_DELIMITER = Pattern.quote("*");

	public static FunctionComponent instantiateGate(final Gate gate, final String instanceName, final Circuit circuit) {
		final FunctionComponent component = new FunctionComponent();
		component.setModule(gate.name);
		circuit.add(component);
		if (instanceName != null) {
			try {
				circuit.setName(component, instanceName);
			} catch (ArgumentException e) {
				System.out.println("Warning: cannot set name '" + instanceName +"' for component '" + circuit.getName(component) + "'.");
			}
		}

		FunctionContact contact = new FunctionContact(IOType.OUTPUT);
		component.add(contact);
		circuit.setName(contact, gate.function.name);
		String setFunction = getSetFunction(gate);
		String resetFunction = getResetFunction(gate);
		if (CommonDebugSettings.getVerboseImport()) {
			System.out.println("Info: instantiating gate " + gate.name + " " + gate.function.name + "=" + gate.function.expression);
			System.out.println("  Set function: " + setFunction);
			System.out.println("  Reset function: " + resetFunction);
		}
		try {
			BooleanFormula setFormula = CircuitUtils.parseContactFuncton(circuit, component, setFunction);
			contact.setSetFunction(setFormula);
			BooleanFormula resetFormula = CircuitUtils.parseContactFuncton(circuit, component, resetFunction);
			contact.setResetFunction(resetFormula);
		} catch (org.workcraft.plugins.cpog.optimisation.javacc.ParseException e) {
			throw new RuntimeException(e);
		}
		return component;
	}

	private static String getSetFunction(Gate gate) {
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

	private static String getResetFunction(Gate gate) {
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
					result += clearedTerm;
				}
			}
		}
		if (result != null) {
			List<String> terms = getTerms(result);
			if (terms.size() != 1) {
				result = "!(" + result + ")";
			} else {
				String singleTerm = terms.iterator().next();
				if (singleTerm.startsWith("!")) {
					result = singleTerm.substring(1, singleTerm.length());
				} else if (singleTerm.endsWith("'")) {
					result = singleTerm.substring(0, singleTerm.length() - 1);
				} else if (singleTerm.startsWith("(")) {
					result = "!" + singleTerm;
				} else {
					result = "!(" + singleTerm + ")";
				}
			}
		}
		return result;
	}

	private static List<String> getTerms(String expression) {
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
