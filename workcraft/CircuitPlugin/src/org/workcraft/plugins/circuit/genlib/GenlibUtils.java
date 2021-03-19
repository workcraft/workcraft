package org.workcraft.plugins.circuit.genlib;

import org.workcraft.exceptions.ArgumentException;
import org.workcraft.formula.BooleanFormula;
import org.workcraft.formula.BooleanVariable;
import org.workcraft.formula.FormulaUtils;
import org.workcraft.formula.bdd.BddManager;
import org.workcraft.formula.jj.BooleanFormulaParser;
import org.workcraft.formula.jj.ParseException;
import org.workcraft.plugins.builtin.settings.DebugCommonSettings;
import org.workcraft.plugins.circuit.*;
import org.workcraft.plugins.circuit.Contact.IOType;
import org.workcraft.plugins.circuit.utils.CircuitUtils;
import org.workcraft.plugins.circuit.utils.ExpressionUtils;
import org.workcraft.types.Pair;
import org.workcraft.utils.LogUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GenlibUtils {

    public static FunctionComponent instantiateGate(Gate gate, String instanceName, Circuit circuit) {
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
        if (DebugCommonSettings.getVerboseImport()) {
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

    public static void instantiateGate(Gate gate, VisualCircuit circuit, VisualFunctionComponent component) {
        component.getReferencedComponent().setModule(gate.name);
        VisualFunctionContact contact = component.getGateOutput();
        if (contact == null) {
            contact = component.createContact(IOType.OUTPUT);
        }
        circuit.setMathName(contact, gate.function.name);
        String setFunction = getSetFunction(gate);
        String resetFunction = getResetFunction(gate);
        try {
            BooleanFormula setFormula = CircuitUtils.parsePinFuncton(circuit, component, setFunction);
            contact.setSetFunction(setFormula);
            BooleanFormula resetFormula = CircuitUtils.parsePinFuncton(circuit, component, resetFunction);
            contact.setResetFunction(resetFormula);
        } catch (org.workcraft.formula.jj.ParseException e) {
            throw new RuntimeException(e);
        }
        contact.setDefaultDirection();
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

    public static Pair<Gate, Map<BooleanVariable, String>> findMapping(BooleanFormula formula, Library library) {
        if (library != null) {
            for (String gateName : library.getNames()) {
                Gate gate = library.get(gateName);
                Map<BooleanVariable, String> mapping = findMapping(formula, gate);
                if (mapping != null) {
                    return Pair.of(gate, mapping);
                }
            }
        }
        return null;
    }

    private static Map<BooleanVariable, String> findMapping(BooleanFormula formula, Gate gate) {
        if (!gate.isSequential()) {
            try {
                BooleanFormula gateFormula = BooleanFormulaParser.parse(gate.function.formula);
                Map<BooleanVariable, BooleanVariable> mapping = findMapping(formula, gateFormula);
                if (mapping != null) {
                    return mapping.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getLabel()));
                }
            } catch (ParseException e) {
            }
        }
        return null;
    }

    private static Map<BooleanVariable, BooleanVariable> findMapping(BooleanFormula firstFormula, BooleanFormula secondFormula) {
        List<BooleanVariable> firstVars = FormulaUtils.extractOrderedVariables(firstFormula);
        List<BooleanVariable> secondVars = FormulaUtils.extractOrderedVariables(secondFormula);
        if (firstVars.size() == secondVars.size()) {
            BddManager bdd = new BddManager();
            for (List<BooleanVariable> vars : generatePermutations(firstVars)) {
                BooleanFormula mappedFormula = FormulaUtils.replace(firstFormula, vars, secondVars);
                if (bdd.equal(mappedFormula, secondFormula)) {
                    Map<BooleanVariable, BooleanVariable> result = new HashMap<>();
                    for (int i = 0; i < vars.size(); i++) {
                        result.put(vars.get(i), secondVars.get(i));
                    }
                    return result;
                }
            }
        }
        return null;
    }

    private static <T> List<List<T>> generatePermutations(List<T> list) {
        List<List<T>> result = new ArrayList<>();
        if (list.isEmpty()) {
            result.add(new ArrayList<>());
        } else {
            T firstElement = list.remove(0);
            List<List<T>> permutations = generatePermutations(list);
            for (List<T> permutation : permutations) {
                for (int index = 0; index <= permutation.size(); index++) {
                    List<T> tmp = new ArrayList<>(permutation);
                    tmp.add(index, firstElement);
                    result.add(tmp);
                }
            }
        }
        return result;
    }

    public static int getPinCount(Gate gate) {
        if (gate != null) {
            try {
                BooleanFormula formula = BooleanFormulaParser.parse(gate.function.formula);
                return FormulaUtils.extractOrderedVariables(formula).size() + (gate.isSequential() ? 0 : 1);
            } catch (ParseException e) {
            }
        }
        return 0;
    }

    public static Pair<Integer, Integer> getPinRange(Library library) {
        int min = 0;
        int max = 0;
        if (library != null) {
            boolean first = true;
            for (String gateName : library.getNames()) {
                Gate gate = library.get(gateName);
                int pinCount = GenlibUtils.getPinCount(gate);
                if (first || (pinCount < min)) {
                    min = pinCount;
                }
                if (first || (pinCount > max)) {
                    max = pinCount;
                }
                first = false;
            }
        }
        return Pair.of(min, max);
    }

}