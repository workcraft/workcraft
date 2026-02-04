package org.workcraft.plugins.circuit.genlib;

import org.workcraft.formula.BooleanFormula;
import org.workcraft.formula.BooleanVariable;
import org.workcraft.formula.FormulaUtils;
import org.workcraft.formula.jj.BooleanFormulaParser;
import org.workcraft.formula.jj.ParseException;
import org.workcraft.plugins.circuit.utils.ExpressionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Gate {

    public static class PinRenamining extends HashMap<BooleanVariable, String> {
    }

    public record Mapping(Gate gate, PinRenamining pinRenamining) {
    }

    public record ExtendedMapping(Gate gate, PinRenamining pinRenamining, Set<String> invertedPinNames,
            Map<String, BooleanVariable> extraPinAssignment) {

        public ExtendedMapping(Gate gate, PinRenamining pinRenamining, Set<String> invertedPinNames) {
            this(gate, pinRenamining, invertedPinNames, new HashMap<>());
        }

        public ExtendedMapping(Gate gate, ExtendedMapping extendedMapping) {
            this(gate, extendedMapping.pinRenamining, extendedMapping.invertedPinNames, extendedMapping.extraPinAssignment);
        }

        public void addGateOutputToInvertedPinNames() {
            invertedPinNames.add(gate.function.name);
        }
    }

    public final String name;
    public final double size;
    public final Function function;
    public final String seq;
    public final boolean primitive;
    private final String setExpression;
    private final String resetExpression;
    private final BooleanFormula setFormula;
    private final BooleanFormula resetFormula;
    private final int pinCount;

    public Gate(String name, double size, Function function, String seq, boolean primitive)
            throws ParseException {

        this.name = name;
        this.size = size;
        this.function = function;
        this.seq = seq;
        this.primitive = primitive;

        setExpression = isSequential() ? ExpressionUtils.extractSetFunction(function.formula, seq) : function.formula;
        resetExpression = isSequential() ? ExpressionUtils.extractResetFunction(function.formula, seq) : null;

        setFormula = BooleanFormulaParser.parse(setExpression);
        resetFormula = BooleanFormulaParser.parse(resetExpression);
        BooleanFormula formula = BooleanFormulaParser.parse(function.formula);
        List<BooleanVariable> pins = FormulaUtils.extractOrderedVariables(formula);
        pinCount = pins.size() + (isSequential() ? 0 : 1);
    }

    public boolean isSequential() {
        return seq != null;
    }

    public boolean isPrimitive() {
        return primitive;
    }

    public String getSetExpression() {
        return setExpression;
    }

    public String getResetExpression() {
        return resetExpression;
    }

    public BooleanFormula getSetFormula() {
        return setFormula;
    }

    public BooleanFormula getResetFormula() {
        return resetFormula;
    }

    public int getPinCount() {
        return pinCount;
    }

}
