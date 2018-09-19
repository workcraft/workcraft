package org.workcraft.plugins.circuit.genlib;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.workcraft.exceptions.ArgumentException;
import org.workcraft.formula.BooleanFormula;
import org.workcraft.plugins.circuit.Circuit;
import org.workcraft.plugins.circuit.utils.CircuitUtils;
import org.workcraft.plugins.circuit.Contact.IOType;
import org.workcraft.plugins.circuit.FunctionComponent;
import org.workcraft.plugins.circuit.FunctionContact;
import org.workcraft.plugins.circuit.expression.ExpressionUtils;
import org.workcraft.plugins.circuit.jj.genlib.GenlibParser;
import org.workcraft.plugins.shared.CommonDebugSettings;
import org.workcraft.util.FileUtils;
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
                LogUtils.logWarning("Cannot set name '" + instanceName + "' for component '" + circuit.getName(component) + "'.");
            }
        }

        FunctionContact contact = new FunctionContact(IOType.OUTPUT);
        component.add(contact);
        circuit.setName(contact, gate.function.name);
        String setFunction = getSetFunction(gate);
        String resetFunction = getResetFunction(gate);
        if (CommonDebugSettings.getVerboseImport()) {
            LogUtils.logInfo("Instantiating gate " + gate.name + " " + gate.function.name + "=" + gate.function.formula);
            LogUtils.logInfo("  Set function: " + setFunction);
            LogUtils.logInfo("  Reset function: " + resetFunction);
        }
        try {
            BooleanFormula setFormula = CircuitUtils.parsePinFuncton(circuit, component, setFunction);
            contact.setSetFunctionQuiet(setFormula);
            BooleanFormula resetFormula = CircuitUtils.parsePinFuncton(circuit, component, resetFunction);
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

    public static Library readLibrary(String fileName) {
        Library library = new Library();
        if ((fileName == null) || fileName.isEmpty()) {
            LogUtils.logWarning("Gate library file is not specified.");
        } else {
            File file = new File(fileName);
            if (FileUtils.checkAvailability(file, "Gate library access error", false)) {
                try {
                    InputStream genlibInputStream = new FileInputStream(fileName);
                    GenlibParser genlibParser = new GenlibParser(genlibInputStream);
                    if (CommonDebugSettings.getParserTracing()) {
                        genlibParser.enable_tracing();
                    } else {
                        genlibParser.disable_tracing();
                    }
                    library = genlibParser.parseGenlib();
                    LogUtils.logInfo("Mapping the imported Verilog into the gate library '" + fileName + "'.");
                } catch (FileNotFoundException e) {
                } catch (org.workcraft.plugins.circuit.jj.genlib.ParseException e) {
                    LogUtils.logWarning("Could not parse the gate library '" + fileName + "'.");
                }
            }
        }
        return library;
    }

}
