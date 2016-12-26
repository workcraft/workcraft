package org.workcraft.plugins.cpog.serialisation;

import org.workcraft.formula.BooleanFormula;
import org.workcraft.formula.serialisation.BooleanFormulaSerialiser;
import org.workcraft.plugins.cpog.Vertex;

public class VertexSerialiser extends BooleanFormulaSerialiser {
    @Override
    public String getClassName() {
        return Vertex.class.getName();
    }

    @Override
    protected BooleanFormula getFormula(Object serialisee) {
        return ((Vertex) serialisee).getCondition();
    }
}
