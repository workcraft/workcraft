package org.workcraft.plugins.circuit.genlib;

import org.workcraft.exceptions.ArgumentException;
import org.workcraft.formula.BooleanFormula;
import org.workcraft.plugins.circuit.Circuit;
import org.workcraft.plugins.circuit.CircuitUtils;
import org.workcraft.plugins.circuit.Contact.IOType;
import org.workcraft.plugins.circuit.FunctionComponent;
import org.workcraft.plugins.circuit.FunctionContact;
import org.workcraft.plugins.circuit.expression.ExpressionUtils;
import org.workcraft.plugins.shared.CommonDebugSettings;
import org.workcraft.util.LogUtils;

public class GenlibUtils {

    public static FunctionComponent instantiateGate(final Gate gate, final String instanceName, final Circuit circuit) {
        final FunctionComponent component = new FunctionComponent();
        component.setModule(gate.name);
        circuit.add(component);
        if (instanceName != null) {
            try {
                circuit.setName(component, instanceName);
            } catch (ArgumentException e) {
                LogUtils.logWarningLine("Cannot set name '" + instanceName + "' for component '" + circuit.getName(component) + "'.");
            }
        }

        FunctionContact contact = new FunctionContact(IOType.OUTPUT);
        component.add(contact);
        circuit.setName(contact, gate.function.name);
        String setFunction = getSetFunction(gate);
        String resetFunction = getResetFunction(gate);
        if (CommonDebugSettings.getVerboseImport()) {
            LogUtils.logInfoLine("Instantiating gate " + gate.name + " " + gate.function.name + "=" + gate.function.formula);
            LogUtils.logInfoLine("  Set function: " + setFunction);
            LogUtils.logInfoLine("  Reset function: " + resetFunction);
        }
        try {
            BooleanFormula setFormula = CircuitUtils.parseContactFuncton(circuit, component, setFunction);
            contact.setSetFunctionQuiet(setFormula);
            BooleanFormula resetFormula = CircuitUtils.parseContactFuncton(circuit, component, resetFunction);
            contact.setResetFunctionQuiet(resetFormula);
        } catch (org.workcraft.formula.jj.ParseException e) {
            throw new RuntimeException(e);
        }
        return component;
    }

    private static String getSetFunction(Gate gate) {
        String result = null;
        if (gate.isSequential()) {
            result = ExpressionUtils.extactSetExpression(gate.function.formula, gate.seq);
        } else {
            result = gate.function.formula;
        }
        return result;
    }

    private static String getResetFunction(Gate gate) {
        String result = null;
        if (gate.isSequential()) {
            result = ExpressionUtils.extactResetExpression(gate.function.formula, gate.seq);
        }
        return result;
    }

}
