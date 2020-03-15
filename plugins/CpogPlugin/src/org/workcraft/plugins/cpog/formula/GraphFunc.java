package org.workcraft.plugins.cpog.formula;

import org.workcraft.plugins.cpog.formula.jj.ParseException;
import org.workcraft.types.Func;

public interface GraphFunc<T, R> extends Func<T, R> {
    GraphFunc<T, R> removeGraphName(String graphName);
    CpogFormula eval(String label, String boolExpression) throws ParseException;
    void setSequenceCondition(CpogFormula formula, String boolForm);
}
