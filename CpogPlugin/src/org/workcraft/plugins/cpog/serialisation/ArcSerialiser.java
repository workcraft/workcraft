package org.workcraft.plugins.cpog.serialisation;

import org.workcraft.formula.BooleanFormula;
import org.workcraft.formula.serialisation.BooleanFormulaSerialiser;
import org.workcraft.plugins.cpog.Arc;

public class ArcSerialiser extends BooleanFormulaSerialiser<Arc> {

    @Override
    public String getClassName() {
        return Arc.class.getName();
    }

    @Override
    protected BooleanFormula getFormula(Arc serialisee) {
        return serialisee.getCondition();
    }

}
