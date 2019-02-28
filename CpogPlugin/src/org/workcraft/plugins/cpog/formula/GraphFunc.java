package org.workcraft.plugins.cpog.formula;

import org.workcraft.types.Func;

public interface GraphFunc<Arg, Result> extends Func<Arg, Result> {

    GraphFunc<Arg, Result> removeGraphName(String graphName);

    CpogFormula eval(String label, String boolExpression) throws org.workcraft.plugins.cpog.formula.jj.ParseException;

    void setSequenceCondition(CpogFormula formula, String boolForm);

//    public boolean getRef();
}
