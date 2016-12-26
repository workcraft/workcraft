package org.workcraft.plugins.cpog.serialisation;

import org.workcraft.formula.BooleanFormula;
import org.workcraft.formula.serialisation.BooleanFormulaSerialiser;
import org.workcraft.plugins.cpog.Arc;

public class ArcSerialiser extends BooleanFormulaSerialiser {
    @Override
    public String getClassName() {
        return Arc.class.getName();
    }

    @Override
    protected BooleanFormula getFormula(Object serialisee) {
        return ((Arc) serialisee).getCondition();
    }
}
