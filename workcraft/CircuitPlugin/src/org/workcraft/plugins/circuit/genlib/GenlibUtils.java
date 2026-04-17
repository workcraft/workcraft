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
import org.workcraft.utils.ListUtils;
import org.workcraft.utils.LogUtils;

import java.util.*;

public final class GenlibUtils {

    private static final String RIGHT_ARROW_SYMBOL = Character.toString((char) 0x2192);

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
        if (DebugCommonSettings.getVerboseImport()) {
            LogUtils.logInfo("Instantiating gate " + gate.name + ' ' + gate.function.name + '=' + gate.function.formula);
            LogUtils.logInfo("  Set function: " + gate.getSetExpression());
            LogUtils.logInfo("  Reset function: " + gate.getResetExpression());
        }
        try {
            contact.setSetFunctionQuiet(CircuitUtils.parsePinFunction(circuit, component, gate.getSetExpression()));
            contact.setResetFunctionQuiet(CircuitUtils.parsePinFunction(circuit, component, gate.getResetExpression()));
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
        try {
            contact.setBothFunctions(
                    CircuitUtils.parsePinFunction(circuit, component, gate.getSetExpression()),
                    CircuitUtils.parsePinFunction(circuit, component, gate.getResetExpression()));
        } catch (org.workcraft.formula.jj.ParseException e) {
            throw new RuntimeException(e);
        }
        contact.setInitToOne(CircuitUtils.cannotFall(contact.getReferencedComponent()));
        contact.setDefaultDirection();
    }

    public static Gate.Mapping findMapping(BooleanFormula formula, Library library) {
        return GenlibUtils.findMapping(formula, null, library);
    }

    public static Gate.Mapping findMapping(BooleanFormula setFormula, BooleanFormula resetFormula, Library library) {
        // Ignore resetFormula if it is complementary to setFormula
        if ((setFormula != null) && (resetFormula != null)) {
            BddManager bdd = new BddManager();
            if (bdd.isEquivalent(setFormula, new Not(resetFormula))) {
                resetFormula = null;
            }
        }
        List<BooleanVariable> inputPins = FormulaUtils.extractOrderedVariables(setFormula, resetFormula);
        List<Gate> orderedCandidateGates = library.getGatesOrderedBySize(inputPins.size() + 1, (resetFormula != null));
        for (Gate gate : orderedCandidateGates) {
            Gate.PinRenamining pinRenamining = getVariableMappingIfEquivalentOrNull(setFormula, gate.getSetFormula());
            if (resetFormula != null) {
                Gate.PinRenamining resetPinRenamining
                        = getVariableMappingIfEquivalentOrNull(resetFormula, gate.getResetFormula());

                pinRenamining = mergePinRenamingsIfCompatibleOrNull(pinRenamining, resetPinRenamining);
            }
            if (pinRenamining != null) {
                return new Gate.Mapping(gate, pinRenamining);
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

    private static Gate.PinRenamining mergePinRenamingsIfCompatibleOrNull(
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

    public static Gate.ExtendedMapping findExtendedMapping(BooleanFormula formula, Library library) {
        return findExtendedMapping(formula, null, library, true, true, true);
    }

    public static Gate.ExtendedMapping findExtendedMapping(
            BooleanFormula setFormula, BooleanFormula resetFormula, Library library,
            boolean allowOutputInversion, boolean allowExtraPin, boolean allowInputInversion) {

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
        // Try 1: match gates directly
        Gate.Mapping mapping = findMapping(setFormula, resetFormula, library);
        if (mapping != null) {
            return new Gate.ExtendedMapping(mapping.gate(), mapping.pinRenamining(), Set.of());
        }
        // Try 2: match inverted gates
        BooleanFormula notSetFormula = (setFormula == null) ? null : new Not(setFormula);
        BooleanFormula notResetFormula = (resetFormula == null) ? null : new Not(resetFormula);
        if (allowOutputInversion) {
            Gate.Mapping invMapping = findMapping(notSetFormula, notResetFormula, library);
            if (invMapping != null) {
                Gate gate = invMapping.gate();
                return new Gate.ExtendedMapping(gate, invMapping.pinRenamining(), Set.of(gate.function.name));
            }
        }
        // Try 3: match gates with extra pin
        if (allowExtraPin) {
            Gate.ExtendedMapping extendedMapping
                    = getEquivalentExtendedMappingOrNull(setFormula, resetFormula, library, true, false);

            if (extendedMapping != null) {
                return extendedMapping;
            }
        }
        // Try 4: match inverted gates with extra pin
        if (allowExtraPin && allowOutputInversion) {
            Gate.ExtendedMapping invExtendedMapping
                    = getEquivalentExtendedMappingOrNull(notSetFormula, notResetFormula, library, true, false);

            if (invExtendedMapping != null) {
                invExtendedMapping.addGateOutputToInvertedPinNames();
                return invExtendedMapping;
            }
        }
        // Try 5: match gates with input inverters, possibly with extra pin
        if (allowInputInversion) {
            Gate.ExtendedMapping extendedMapping
                    = getEquivalentExtendedMappingOrNull(setFormula, resetFormula, library, false, true);

            if (extendedMapping == null) {
                extendedMapping = getEquivalentExtendedMappingOrNull(setFormula, resetFormula, library, true, true);
            }
            if (extendedMapping != null) {
                return extendedMapping;
            }
        }
        // Try 6: match inverted gates with input inverters, possibly with extra pin
        if (allowOutputInversion && allowInputInversion) {
            Gate.ExtendedMapping invExtendedMapping
                    = getEquivalentExtendedMappingOrNull(notSetFormula, notResetFormula, library, false, true);

            if (invExtendedMapping == null) {
                invExtendedMapping = getEquivalentExtendedMappingOrNull(notSetFormula, notResetFormula, library, true, true);
            }
            if (invExtendedMapping != null) {
                invExtendedMapping.addGateOutputToInvertedPinNames();
                return invExtendedMapping;
            }
        }
        return null;
    }

    private static Gate.ExtendedMapping getEquivalentExtendedMappingOrNull(
            BooleanFormula setFormula, BooleanFormula resetFormula, Library library,
            boolean withExtraPin, boolean withInputInversion) {

        List<BooleanVariable> inputPins = FormulaUtils.extractOrderedVariables(setFormula, resetFormula);
        int pinCount = inputPins.size() + (withExtraPin ? 1 : 0) + 1;
        List<Gate> orderedCandidateGates = library.getGatesOrderedBySize(pinCount, (resetFormula != null));
        for (Gate gate : orderedCandidateGates) {
            Gate.ExtendedMapping extendedMapping = getEquivalentExtendedMappingOrNull(
                    setFormula, gate.getSetFormula(), withExtraPin, withInputInversion);

            if (resetFormula != null) {
                Gate.ExtendedMapping resetExtendedMapping = getEquivalentExtendedMappingOrNull(
                        resetFormula, gate.getResetFormula(), withExtraPin, withInputInversion);

                extendedMapping = mergeCompatibleExtendedMappingsOrNull(extendedMapping, resetExtendedMapping);
            }

            if (extendedMapping != null) {
                return new Gate.ExtendedMapping(gate, extendedMapping);
            }
        }
        return null;
    }


    private static Gate.ExtendedMapping getEquivalentExtendedMappingOrNull(
            BooleanFormula formula, BooleanFormula candidateFormula,
            boolean withExtraPin, boolean withInputInversion) {

        List<BooleanVariable> vars = FormulaUtils.extractOrderedVariables(formula);
        List<BooleanVariable> candidateVars = FormulaUtils.extractOrderedVariables(candidateFormula);
        if (vars.size() + (withExtraPin ? 1 : 0) != candidateVars.size()) {
            return null;
        }
        BddManager bdd = new BddManager();
        int varCount = candidateVars.size();
        List<List<Boolean>> inversionCombinations = withInputInversion
                ? ListUtils.combine(List.of(false, true), varCount)
                : List.of(Collections.nCopies(varCount, false));

        // Skip the first combination of inversions (all false), if there are more than 1
        if (inversionCombinations.size() > 1) {
            inversionCombinations = inversionCombinations.subList(1, inversionCombinations.size());
        }
        if (!withExtraPin) {
            return getEquivalentExtendedMappingOrNull(formula, vars, candidateFormula, candidateVars, inversionCombinations, bdd);
        } else {
            // Insert extra input pin before its replica
            int varIndex = 0;
            for (BooleanVariable replicaVar : vars) {
                List<BooleanVariable> extendedVars = new ArrayList<>(vars);
                extendedVars.add(varIndex, replicaVar);
                Gate.ExtendedMapping extendedMapping = getEquivalentExtendedMappingOrNull(
                        formula, extendedVars, candidateFormula, candidateVars, inversionCombinations, bdd);

                if (extendedMapping != null) {
                    return extendedMapping;
                }
                varIndex++;
            }
        }
        return null;
    }

    private static Gate.ExtendedMapping getEquivalentExtendedMappingOrNull(
            BooleanFormula formula, List<BooleanVariable> vars,
            BooleanFormula candidateFormula, List<BooleanVariable> candidateVars,
            List<List<Boolean>> inversionCombinations, BddManager bdd) {

        for (List<BooleanVariable> permutatedVars : ListUtils.permutate(vars)) {
            Gate.ExtendedMapping extendedMapping = getPermutationEquivalentExtendedMappingOrNull(
                    formula, permutatedVars, candidateFormula, candidateVars, inversionCombinations, bdd);

            if (extendedMapping != null) {
                return extendedMapping;
            }
        }
        return null;
    }

    private static Gate.ExtendedMapping getPermutationEquivalentExtendedMappingOrNull(
            BooleanFormula formula, List<BooleanVariable> permutatedVars,
            BooleanFormula candidateFormula, List<BooleanVariable> candidateVars,
            List<List<Boolean>> inversionCombinations, BddManager bdd) {

        int varCount = candidateVars.size();
        for (List<Boolean> inversionCombination : inversionCombinations) {
            List<BooleanFormula> invPermutatedVars = new ArrayList<>(varCount);
            for (int varIndex = 0; varIndex < varCount; varIndex++) {
                BooleanVariable var = permutatedVars.get(varIndex);
                Boolean varInversion = inversionCombination.get(varIndex);
                invPermutatedVars.add(varIndex, varInversion ? new Not(var) : var);
            }
            BooleanFormula substitutedCandidateFormula
                    = FormulaUtils.replace(candidateFormula, candidateVars, invPermutatedVars);

            if (bdd.isEquivalent(formula, substitutedCandidateFormula)) {
                Gate.PinRenamining pinRenamining = new Gate.PinRenamining();
                Set<String> invertedPinNames = new HashSet<>();
                Map<String, BooleanVariable> extraPinAssignment = new HashMap<>();
                for (int varIndex = 0; varIndex < varCount; varIndex++) {
                    String varLabel = candidateVars.get(varIndex).getLabel();
                    BooleanVariable var = permutatedVars.get(varIndex);
                    if (pinRenamining.containsKey(var)) {
                        extraPinAssignment.put(varLabel, var);
                    } else {
                        pinRenamining.put(var, varLabel);
                    }
                    if (inversionCombination.get(varIndex)) {
                        invertedPinNames.add(varLabel);
                    }
                }
                return new Gate.ExtendedMapping(null, pinRenamining, invertedPinNames, extraPinAssignment);
            }
        }
        return null;
    }

    private static Gate.ExtendedMapping mergeCompatibleExtendedMappingsOrNull(
            Gate.ExtendedMapping setExtendedMapping, Gate.ExtendedMapping resetExtendedMapping) {

        if ((setExtendedMapping == null) || (resetExtendedMapping == null)) {
            return null;
        }
        Map<String, BooleanVariable> extraPinAssignment = setExtendedMapping.extraPinAssignment();
        if (!extraPinAssignment.equals(resetExtendedMapping.extraPinAssignment())) {
            return null;
        }
        Gate.PinRenamining pinRenamining = new Gate.PinRenamining();
        pinRenamining.putAll(setExtendedMapping.pinRenamining());
        Set<String> invertedPinNames = new HashSet<>(setExtendedMapping.invertedPinNames());
        for (BooleanVariable variable : resetExtendedMapping.pinRenamining().keySet()) {
            String pinRename = pinRenamining.get(variable);
            String resetPinRename = resetExtendedMapping.pinRenamining().get(variable);
            Set<String> resetInvertedPinNames = resetExtendedMapping.invertedPinNames();
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
        return new Gate.ExtendedMapping(null, pinRenamining, invertedPinNames, extraPinAssignment);
    }

    public static String getExtendedMappingInfo(Gate.ExtendedMapping extendedMapping,
            List<BooleanVariable> inputVars, BooleanVariable outputVar) {

        Gate gate = extendedMapping.gate();
        Map<BooleanVariable, String> pinRenamining = extendedMapping.pinRenamining();
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
            s.append(getExtendedAssignmentInfo(inputVar, pinRenamining.get(inputVar), invertedPins));
            isFirstAssignment = false;
        }
        Map<String, BooleanVariable> extraPinAssignment = extendedMapping.extraPinAssignment();
        for (String extraPinName : extraPinAssignment.keySet()) {
            BooleanFormula replicatedVar = extraPinAssignment.get(extraPinName);
            s.append(" + ");
            s.append(getExtendedAssignmentInfo(replicatedVar, extraPinName, invertedPins));
        }
        return s.toString();
    }

    private static String gateToString(Gate gate) {
        String details = "";
        try {
            BooleanFormula formula = BooleanFormulaParser.parse(gate.function.formula);
            details =  " [" + gate.function.name + " = " + StringGenerator.toString(formula) + "]";
        } catch (ParseException ignored) {
        }
        return gate.name + details;
    }

    private static String getExtendedAssignmentInfo(BooleanFormula formula, String pin, Set<String> invertedPins) {
        return StringGenerator.toString(formula) + RIGHT_ARROW_SYMBOL + pin + (invertedPins.contains(pin) ? "'" : "");
    }

}
