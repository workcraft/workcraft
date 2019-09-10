package org.workcraft.plugins.cpog.encoding.onehot;

import java.util.List;

import org.workcraft.formula.BooleanVariable;

public class ThermometerBooleanFormula {

    private final List<BooleanVariable> vars;

    public ThermometerBooleanFormula(List<BooleanVariable> vars) {
        this.vars = vars;
    }

    public List<BooleanVariable> getVars() {
        return vars;
    }

}
