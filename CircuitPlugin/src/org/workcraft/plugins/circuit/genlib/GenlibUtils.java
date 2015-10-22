package org.workcraft.plugins.circuit.genlib;

import org.workcraft.exceptions.ArgumentException;
import org.workcraft.plugins.circuit.Circuit;
import org.workcraft.plugins.circuit.CircuitUtils;
import org.workcraft.plugins.circuit.Contact.IOType;
import org.workcraft.plugins.circuit.FunctionComponent;
import org.workcraft.plugins.circuit.FunctionContact;
import org.workcraft.plugins.circuit.expression.ExpressionUtils;
import org.workcraft.plugins.cpog.optimisation.BooleanFormula;
import org.workcraft.plugins.shared.CommonDebugSettings;

public class GenlibUtils {
	private static final char TERM_DELIMITER = '+';
	private static final char FACTOR_DELIMITER = '*';

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
			System.out.println("Info: Instantiating gate " + gate.name + " " + gate.function.name + "=" + gate.function.formula);
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
		if (gate.isSequential()) {
			result = ExpressionUtils.extactSetExpression(gate.function.formula, gate.seq, TERM_DELIMITER, FACTOR_DELIMITER);
		} else {
			result = gate.function.formula;
		}
		return result;
	}

	private static String getResetFunction(Gate gate) {
		String result = null;
		if (gate.isSequential()) {
			result = ExpressionUtils.extactResetExpression(gate.function.formula, gate.seq, TERM_DELIMITER, FACTOR_DELIMITER);
		}
		return result;
	}

}
