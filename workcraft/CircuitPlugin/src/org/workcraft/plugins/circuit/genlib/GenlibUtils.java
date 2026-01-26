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
import org.workcraft.utils.ListUtils;
import org.workcraft.utils.LogUtils;

import java.util.*;

public final class GenlibUtils {

    private static final String RIGHT_ARROW_SYMBOL = Character.toString((char) 0x2192);

    private record PinRenamingAndInversion(Gate.PinRenamining pinRenamining, Set<String> invertedPinNames) {
    }

    private GenlibUtils() {
    }

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
            contact.setSetFunctionQuiet(CircuitUtils.parsePinFunction(circuit, component, setFunction));
            contact.setResetFunctionQuiet(CircuitUtils.parsePinFunction(circuit, component, resetFunction));
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
            contact.setInitToOne(CircuitUtils.cannotFall(contact.getReferencedComponent()));
            contact.setBothFunctions(
                    CircuitUtils.parsePinFunction(circuit, component, setFunction),
                    CircuitUtils.parsePinFunction(circuit, component, resetFunction));
        } catch (org.workcraft.formula.jj.ParseException e) {
            throw new RuntimeException(e);
        }
        contact.setDefaultDirection();
    }

    public static String getSetFunction(Gate gate) {
        return !gate.isSequential() ? gate.function.formula
                : ExpressionUtils.extractSetFunction(gate.function.formula, gate.seq);
    }

    public static String getResetFunction(Gate gate) {
        return !gate.isSequential() ? null
                : ExpressionUtils.extractResetFunction(gate.function.formula, gate.seq);
    }

    public static Gate.Mapping findMapping(BooleanFormula formula, Library library) {
        return GenlibUtils.findMapping(formula, null, library);
    }

    public static Gate.Mapping findMapping(BooleanFormula setFormula,
            BooleanFormula resetFormula, Library library) {

        if (library == null) {
            return null;
        }
        // Ignore resetFormula if it is complementary to setFormula
        if ((setFormula != null) && (resetFormula != null)) {
            BddManager bdd = new BddManager();
            if (bdd.isEquivalent(setFormula, new Not(resetFormula))) {
                resetFormula = null;
            }
        }
        for (Gate gate : library.getGatesOrderedBySize()) {
            if (gate.isPrimitive() && ((resetFormula == null) == gate.isSequential())) {
                continue;
            }
            try {
                Gate.PinRenamining pinRenamining = getVariableMappingIfEquivalentOrNull(setFormula,
                        BooleanFormulaParser.parse(GenlibUtils.getSetFunction(gate)));

                if (resetFormula != null) {
                    BooleanFormula gateResetFormula = BooleanFormulaParser.parse(GenlibUtils.getResetFunction(gate));
                    Gate.PinRenamining resetPinRenamining
                            = getVariableMappingIfEquivalentOrNull(resetFormula, gateResetFormula);

                    pinRenamining = mergeVariableMappingsIfCompatibleOrNull(pinRenamining, resetPinRenamining);
                }
                if (pinRenamining != null) {
                    return new Gate.Mapping(gate, pinRenamining);
                }
            } catch (ParseException ignored) {
            }
        }
        return null;
    }

    public static Gate.PinRenamining getVariableMappingIfEquivalentOrNull(
            BooleanFormula formula, BooleanFormula candidateFormula) {

        List<BooleanVariable> vars = FormulaUtils.extractOrderedVariables(formula);
        List<BooleanVariable> candidateVars = FormulaUtils.extractOrderedVariables(candidateFormula);
        if (vars.size() == candidateVars.size()) {
            BddManager bdd = new BddManager();
            for (List<BooleanVariable> permutatedVars : ListUtils.permutate(vars)) {
                BooleanFormula mappedFormula = FormulaUtils.replace(formula, permutatedVars, candidateVars);
                if (bdd.isEquivalent(mappedFormula, candidateFormula)) {
                    Gate.PinRenamining result = new Gate.PinRenamining();
                    for (int i = 0; i < permutatedVars.size(); i++) {
                        result.put(permutatedVars.get(i), candidateVars.get(i).getLabel());
                    }
                    return result;
                }
            }
        }
        return null;
    }

    private static Gate.PinRenamining mergeVariableMappingsIfCompatibleOrNull(
            Gate.PinRenamining setPinRenamining, Gate.PinRenamining resetPinRenamining) {

        if ((setPinRenamining == null) || (resetPinRenamining == null)) {
            return null;
        }
        Gate.PinRenamining result = new Gate.PinRenamining();
        result.putAll(setPinRenamining);
        for (BooleanVariable variable : resetPinRenamining.keySet()) {
            String variableName = setPinRenamining.get(variable);
            String resetVariableName = resetPinRenamining.get(variable);
            if (variableName == null) {
                result.put(variable, resetVariableName);
            } else if (!variableName.equals(resetVariableName)) {
                return null;
            }
        }
        return result;
    }

    public static Gate.ExtendedMapping findExtendedMapping(
            BooleanFormula formula, Library library, boolean allowOutputInversion, boolean allowInputInversion) {

        return findExtendedMapping(formula, null, library, allowOutputInversion, allowInputInversion);
    }

    public static Gate.ExtendedMapping findExtendedMapping(
            BooleanFormula setFormula, BooleanFormula resetFormula, Library library,
            boolean allowOutputInversion, boolean allowInputInversion) {

        if (library == null) {
            return null;
        }
        // Ignore resetFormula if it is complementary to setFormula
        if ((setFormula != null) && (resetFormula != null)) {
            BddManager bdd = new BddManager();
            if (bdd.isEquivalent(setFormula, new Not(resetFormula))) {
                resetFormula = null;
            }
        }

        // First, try direct implementation
        Gate.Mapping mapping = findMapping(setFormula, resetFormula, library);
        if (mapping != null) {
            return new Gate.ExtendedMapping(mapping.gate(), mapping.pinRenamining(), Set.of());
        }
        // Then try inverted gates
        BooleanFormula notSetFormula = (setFormula == null) ? null : new Not(setFormula);
        BooleanFormula notResetFormula = (resetFormula == null) ? null : new Not(resetFormula);
        if (allowOutputInversion) {
            Gate.Mapping invMapping = findMapping(notSetFormula, notResetFormula, library);
            if (invMapping != null) {
                Gate gate = invMapping.gate();
                return new Gate.ExtendedMapping(gate, invMapping.pinRenamining(), Set.of(gate.function.name));
            }
        }
        // Then try direct implementation with input bubbles (only for combinational gates, i.e. without reset function)
        if (allowInputInversion) {
            Gate.ExtendedMapping bubbleMapping
                    = findMappingWithInputInversions(setFormula, resetFormula, library);

            if (bubbleMapping != null) {
                return bubbleMapping;
            }
        }
        // Finally try inverted gates with input bubbles (only for combinational gates, i.e. without reset function)
        if (allowOutputInversion && allowInputInversion) {
            Gate.ExtendedMapping invBubbleMapping
                    = findMappingWithInputInversions(notSetFormula, notResetFormula, library);

            if (invBubbleMapping != null) {
                Gate gate = invBubbleMapping.gate();
                Set<String> invertedPins = invBubbleMapping.invertedPinNames();
                invertedPins.add(gate.function.name);
                return new Gate.ExtendedMapping(gate, invBubbleMapping.pinRenamining(), invertedPins);
            }
        }
        return null;
    }

    private static Gate.ExtendedMapping findMappingWithInputInversions(
            BooleanFormula setFormula, BooleanFormula resetFormula, Library library) {

        if (library == null) {
            return null;
        }
        for (Gate gate : library.getGatesOrderedBySize()) {
            if (gate.isPrimitive() && ((resetFormula == null) == gate.isSequential())) {
                continue;
            }
            try {
                BooleanFormula gateSetFormula = BooleanFormulaParser.parse(GenlibUtils.getSetFunction(gate));
                PinRenamingAndInversion pinRenamingAndInversion
                        = getExtendedVariableMappingIfEquivalentOrNull(setFormula, gateSetFormula);

                if (resetFormula != null) {
                    BooleanFormula gateResetFormula = BooleanFormulaParser.parse(GenlibUtils.getResetFunction(gate));
                    PinRenamingAndInversion resetPinRenamingAndInversion
                            = getExtendedVariableMappingIfEquivalentOrNull(resetFormula, gateResetFormula);

                    pinRenamingAndInversion = mergeExtendedVariableMappingsIfCompatibleOrNull(
                            pinRenamingAndInversion, resetPinRenamingAndInversion);
                }

                if (pinRenamingAndInversion != null) {
                    return new Gate.ExtendedMapping(gate, pinRenamingAndInversion.pinRenamining(), pinRenamingAndInversion.invertedPinNames());
                }
            } catch (ParseException ignored) {
            }
        }
        return null;
    }

    private static PinRenamingAndInversion getExtendedVariableMappingIfEquivalentOrNull(
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
                    Gate.PinRenamining pinRenamining = new Gate.PinRenamining();
                    Set<String> invertedPinNames = new HashSet<>();
                    for (int varIndex = 0; varIndex < varCount; varIndex++) {
                        String varLabel = candidateVars.get(varIndex).getLabel();
                        pinRenamining.put(permutatedVars.get(varIndex), varLabel);
                        if (inversionCombination.get(varIndex)) {
                            invertedPinNames.add(varLabel);
                        }
                    }
                    return new PinRenamingAndInversion(pinRenamining, invertedPinNames);
                }
            }
        }
        return null;
    }

    private static PinRenamingAndInversion mergeExtendedVariableMappingsIfCompatibleOrNull(
            PinRenamingAndInversion setPinRenamingAndInversion, PinRenamingAndInversion resetPinRenamingAndInversion) {

        if ((setPinRenamingAndInversion == null) || (resetPinRenamingAndInversion == null)) {
            return null;
        }
        Gate.PinRenamining pinRenamining = new Gate.PinRenamining();
        pinRenamining.putAll(setPinRenamingAndInversion.pinRenamining());
        Set<String> invertedPinNames = new HashSet<>(setPinRenamingAndInversion.invertedPinNames());
        for (BooleanVariable variable : resetPinRenamingAndInversion.pinRenamining().keySet()) {
            String pinRename = pinRenamining.get(variable);
            String resetPinRename = resetPinRenamingAndInversion.pinRenamining().get(variable);
            Set<String> resetInvertedPinNames = resetPinRenamingAndInversion.invertedPinNames();
            if (pinRename == null) {
                pinRenamining.put(variable, resetPinRename);
                if (resetInvertedPinNames.contains(resetPinRename)) {
                    invertedPinNames.add(resetPinRename);
                }
            } else if (!pinRename.equals(resetPinRename)
                    || (invertedPinNames.contains(pinRename) != resetInvertedPinNames.contains(pinRename))) {

                return null;
            }
        }
        return new PinRenamingAndInversion(pinRenamining, invertedPinNames);
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

    public static String getExtendedMappingInfo(Gate.ExtendedMapping extendedMapping,
            List<BooleanVariable> inputVars, BooleanVariable outputVar) {

        Gate gate = extendedMapping.gate();
        Map<BooleanVariable, String> assignments = extendedMapping.pinRenamining();
        Set<String> invertedPins = extendedMapping.invertedPinNames();
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