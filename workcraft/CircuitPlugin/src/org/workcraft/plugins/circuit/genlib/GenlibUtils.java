package org.workcraft.plugins.circuit.genlib;

import org.workcraft.exceptions.ArgumentException;
import org.workcraft.formula.BooleanFormula;
import org.workcraft.formula.BooleanVariable;
import org.workcraft.formula.FormulaUtils;
import org.workcraft.formula.Not;
import org.workcraft.formula.bdd.BddManager;
import org.workcraft.formula.jj.BooleanFormulaParser;
import org.workcraft.formula.jj.ParseException;
import org.workcraft.formula.visitors.StringGenerator;
import org.workcraft.plugins.builtin.settings.DebugCommonSettings;
import org.workcraft.plugins.circuit.*;
import org.workcraft.plugins.circuit.Contact.IOType;
import org.workcraft.plugins.circuit.utils.CircuitUtils;
import org.workcraft.plugins.circuit.utils.ExpressionUtils;
import org.workcraft.types.Pair;
import org.workcraft.types.Triple;
import org.workcraft.utils.ListUtils;
import org.workcraft.utils.LogUtils;

import java.util.*;
import java.util.stream.Collectors;

public class GenlibUtils {

    private static final String RIGHT_ARROW_SYMBOL = Character.toString((char) 0x2192);

    public static FunctionComponent instantiateGate(Gate gate, String instanceName, Circuit circuit) {
        final FunctionComponent component = new FunctionComponent();
        component.setModule(gate.name);
        circuit.add(component);
        if (instanceName != null) {
            try {
                circuit.setName(component, instanceName);
            } catch (ArgumentException e) {
                String componentName = circuit.getComponentReference(component);
                LogUtils.logWarning("Cannot set name '" + instanceName + "' for component '" + componentName + "'");
            }
        }

        FunctionContact contact = new FunctionContact(IOType.OUTPUT);
        component.add(contact);
        circuit.setName(contact, gate.function.name);
        String setFunction = getSetFunction(gate);
        String resetFunction = getResetFunction(gate);
        if (DebugCommonSettings.getVerboseImport()) {
            LogUtils.logInfo("Instantiating gate " + gate.name + ' ' + gate.function.name + '=' + gate.function.formula);
            LogUtils.logInfo("  Set function: " + setFunction);
            LogUtils.logInfo("  Reset function: " + resetFunction);
        }
        try {
            BooleanFormula setFormula = CircuitUtils.parsePinFunction(circuit, component, setFunction);
            contact.setSetFunctionQuiet(setFormula);
            BooleanFormula resetFormula = CircuitUtils.parsePinFunction(circuit, component, resetFunction);
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
            BooleanFormula setFormula = CircuitUtils.parsePinFunction(circuit, component, setFunction);
            contact.setSetFunction(setFormula);
            BooleanFormula resetFormula = CircuitUtils.parsePinFunction(circuit, component, resetFunction);
            contact.setResetFunction(resetFormula);
            contact.setInitToOne(CircuitUtils.cannotFall(contact.getReferencedComponent()));
        } catch (org.workcraft.formula.jj.ParseException e) {
            throw new RuntimeException(e);
        }
        contact.setDefaultDirection();
    }

    private static String getSetFunction(Gate gate) {
        return !gate.isSequential() ? gate.function.formula
                : ExpressionUtils.extractSetFunction(gate.function.formula, gate.seq);
    }

    private static String getResetFunction(Gate gate) {
        return !gate.isSequential() ? null
                : ExpressionUtils.extractResetFunction(gate.function.formula, gate.seq);
    }

    public static Pair<Gate, Map<BooleanVariable, String>> findMapping(BooleanFormula formula, Library library) {
        if (library == null) {
            return null;
        }
        for (Gate gate : library.getGatesOrderedBySize()) {
            if (gate.isSequential() || gate.isPrimitive()) continue;
            try {
                BooleanFormula gateFormula = BooleanFormulaParser.parse(gate.function.formula);

                Map<BooleanVariable, String> variableMapping
                        = getVariableMappingIfEquivalentOrNull(formula, gateFormula);

                if (variableMapping != null) {
                    return Pair.of(gate, variableMapping);
                }
            } catch (ParseException ignored) {
            }
        }
        return null;
    }

    public static Map<BooleanVariable, String> getVariableMappingIfEquivalentOrNull(
            BooleanFormula formula, BooleanFormula candidateFormula) {

        List<BooleanVariable> vars = FormulaUtils.extractOrderedVariables(formula);
        List<BooleanVariable> candidateVars = FormulaUtils.extractOrderedVariables(candidateFormula);
        if (vars.size() == candidateVars.size()) {
            BddManager bdd = new BddManager();
            for (List<BooleanVariable> permutatedVars : ListUtils.permutate(vars)) {
                BooleanFormula mappedFormula = FormulaUtils.replace(formula, permutatedVars, candidateVars);
                if (bdd.isEquivalent(mappedFormula, candidateFormula)) {
                    Map<BooleanVariable, String> result = new HashMap<>();
                    for (int i = 0; i < permutatedVars.size(); i++) {
                        result.put(permutatedVars.get(i), candidateVars.get(i).getLabel());
                    }
                    return result;
                }
            }
        }
        return null;
    }

    public static Triple<Gate, Map<BooleanVariable, String>, Set<String>> findExtendedMapping(
            BooleanFormula formula, Library library, boolean allowOutputInversion, boolean allowInputInversion) {

        if (library == null) {
            return null;
        }
        // First, try direct implementation
        Pair<Gate, Map<BooleanVariable, String>> mapping = findMapping(formula, library);
        if (mapping != null) {
            return Triple.of(mapping.getFirst(), mapping.getSecond(), Set.of());
        }
        // Then try inverted gates
        if (allowOutputInversion) {
            Pair<Gate, Map<BooleanVariable, String>> invMapping = findMapping(new Not(formula), library);
            if (invMapping != null) {
                Gate gate = invMapping.getFirst();
                return Triple.of(gate, invMapping.getSecond(), Set.of(gate.function.name));
            }
        }
        // Then try direct implementation with input bubbles
        if (allowInputInversion) {
            Triple<Gate, Map<BooleanVariable, String>, Set<String>> bubbleMapping
                    = findMappingWithInputInversions(formula, library);

            if (bubbleMapping != null) {
                return bubbleMapping;
            }
        }
        // Finally try inverted gates with input bubbles
        if (allowOutputInversion && allowInputInversion) {
            Triple<Gate, Map<BooleanVariable, String>, Set<String>> invBubbleMapping
                    = findMappingWithInputInversions(new Not(formula), library);

            if (invBubbleMapping != null) {
                Gate gate = invBubbleMapping.getFirst();
                Map<BooleanVariable, String> varAssignments = invBubbleMapping.getSecond();
                Set<String> invertedPins = invBubbleMapping.getThird();
                invertedPins.add(gate.function.name);
                return Triple.of(gate, varAssignments, invertedPins);
            }
        }
        return null;
    }

    private static Triple<Gate, Map<BooleanVariable, String>, Set<String>> findMappingWithInputInversions(
            BooleanFormula formula, Library library) {

        if (library == null) {
            return null;
        }
        for (Gate gate : library.getGatesOrderedBySize()) {
            if (gate.isSequential() || gate.isPrimitive()) continue;
            try {
                BooleanFormula gateFormula = BooleanFormulaParser.parse(gate.function.formula);

                Map<BooleanVariable, Pair<String, Boolean>> extendedVariableMapping
                        = getVariableExtendedMappingIfEquivalentOrNull(formula, gateFormula);

                if (extendedVariableMapping != null) {
                    Map<BooleanVariable, String> variableMapping = extendedVariableMapping.entrySet().stream()
                            .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().getFirst()));

                    Set<String> invertedPins = extendedVariableMapping.values().stream()
                            .filter(Pair::getSecond)
                            .map(Pair::getFirst)
                            .collect(Collectors.toSet());

                    return Triple.of(gate, variableMapping, invertedPins);
                }
            } catch (ParseException ignored) {
            }
        }
        return null;
    }

    private static Map<BooleanVariable, Pair<String, Boolean>> getVariableExtendedMappingIfEquivalentOrNull(
            BooleanFormula formula, BooleanFormula candidateFormula) {

        List<BooleanVariable> vars = FormulaUtils.extractOrderedVariables(formula);
        List<BooleanVariable> candidateVars = FormulaUtils.extractOrderedVariables(candidateFormula);
        if (vars.size() != candidateVars.size()) {
            return null;
        }
        BddManager bdd = new BddManager();
        for (List<BooleanVariable> permutatedVars : ListUtils.permutate(vars)) {
            int varCount = permutatedVars.size();
            List<List<Boolean>> inversionCombinations = ListUtils.combine(List.of(false, true), varCount);
            for (List<Boolean> inversionCombination : inversionCombinations) {
                List<BooleanFormula> invCandidateVars = new ArrayList<>(varCount);
                for (int varIndex = 0; varIndex < varCount; varIndex++) {
                    BooleanVariable candidateVar = candidateVars.get(varIndex);
                    Boolean varInversion = inversionCombination.get(varIndex);
                    invCandidateVars.add(varIndex, varInversion ? new Not(candidateVar) : candidateVar);
                }
                BooleanFormula mappedFormula = FormulaUtils.replace(formula, permutatedVars, invCandidateVars);
                if (bdd.isEquivalent(mappedFormula, candidateFormula)) {
                    Map<BooleanVariable, Pair<String, Boolean>> result = new HashMap<>();
                    for (int varIndex = 0; varIndex < varCount; varIndex++) {
                        String varLabel = candidateVars.get(varIndex).getLabel();
                        Boolean varInversion = inversionCombination.get(varIndex);
                        result.put(permutatedVars.get(varIndex), Pair.of(varLabel, varInversion));
                    }
                    return result;
                }
            }
        }
        return null;
    }

    public static String gateToString(Gate gate) {
        String details = "";
        try {
            BooleanFormula formula = BooleanFormulaParser.parse(gate.function.formula);
            details =  " [" + gate.function.name + " = " + StringGenerator.toString(formula) + "]";
        } catch (ParseException ignored) {
        }
        return gate.name + details;
    }

    public static String getExtendedMappingInfo(Triple<Gate, Map<BooleanVariable, String>, Set<String>> extendedMapping,
            List<BooleanVariable> inputVars, BooleanVariable outputVar) {

        Gate gate = extendedMapping.getFirst();
        Map<BooleanVariable, String> assignments = extendedMapping.getSecond();
        Set<String> invertedPins = extendedMapping.getThird();
        StringBuilder s = new StringBuilder(gateToString(gate));
        boolean isFirstAssignment = true;
        if (outputVar != null) {
            s.append(" : ");
            s.append(getExtendedAssignmentInfo(outputVar, gate.function.name, invertedPins));
            isFirstAssignment = false;
        }
        for (BooleanVariable inputVar : inputVars) {
            s.append(isFirstAssignment ? " : " : ", ");
            s.append(getExtendedAssignmentInfo(inputVar, assignments.get(inputVar), invertedPins));
            isFirstAssignment = false;
        }
        return s.toString();
    }

    public static String getAssignmentInfo(BooleanVariable var, String pin) {
        return var.getLabel() + RIGHT_ARROW_SYMBOL + pin;
    }

    public static String getExtendedAssignmentInfo(BooleanVariable var, String pin, Set<String> invertedPins) {
        return var.getLabel() + RIGHT_ARROW_SYMBOL + pin + (invertedPins.contains(pin) ? "'" : "");
    }

    public static int getPinCount(Gate gate) {
        if (gate != null) {
            try {
                BooleanFormula formula = BooleanFormulaParser.parse(gate.function.formula);
                List<BooleanVariable> pins = FormulaUtils.extractOrderedVariables(formula);
                return pins.size() + (gate.isSequential() ? 0 : 1);
            } catch (ParseException ignored) {
            }
        }
        return 0;
    }

}