package org.workcraft.formula.cnf;

import org.workcraft.formula.visitors.BooleanVisitor;
import org.workcraft.formula.workers.DumbBooleanWorker;
import org.workcraft.formula.Nf;
import org.workcraft.formula.FormulaUtils;

import java.util.List;

public class Cnf extends Nf<CnfClause> {

    private static final DumbBooleanWorker WORKER = DumbBooleanWorker.getInstance();

    public Cnf() {
    }

    public Cnf(CnfClause... clauses) {
        super(clauses);
    }

    public Cnf(List<CnfClause> clauses) {
        super(clauses);
    }

    @Override
    public <T> T accept(BooleanVisitor<T> visitor) {
        return FormulaUtils.createAnd(getClauses(), WORKER).accept(visitor);
    }

}
