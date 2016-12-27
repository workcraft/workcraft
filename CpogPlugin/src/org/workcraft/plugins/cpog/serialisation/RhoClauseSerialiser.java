package org.workcraft.plugins.cpog.serialisation;

import org.workcraft.formula.BooleanFormula;
import org.workcraft.formula.serialisation.BooleanFormulaSerialiser;
import org.workcraft.plugins.cpog.RhoClause;

public class RhoClauseSerialiser extends BooleanFormulaSerialiser {
    @Override
    public String getClassName() {
        return RhoClause.class.getName();
    }

    @Override
    protected BooleanFormula getFormula(Object serialisee) {
        return ((RhoClause) serialisee).getFormula();
    }
}
