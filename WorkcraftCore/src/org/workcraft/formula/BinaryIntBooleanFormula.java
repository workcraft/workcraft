package org.workcraft.formula;

import java.util.List;

public class BinaryIntBooleanFormula {

    private final List<BooleanVariable> bits;
    private final int valuesCount;

    public BinaryIntBooleanFormula(List<BooleanVariable> bits, int valuesCount) {
        this.bits = bits;
        this.valuesCount = valuesCount;
    }

    public List<BooleanVariable> getVars() {
        return bits;
    }

    public int getValuesCount() {
        return valuesCount;
    }
}
