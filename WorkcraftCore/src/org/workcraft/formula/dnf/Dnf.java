package org.workcraft.formula.dnf;

import org.workcraft.formula.visitors.BooleanVisitor;
import org.workcraft.formula.workers.DumbBooleanWorker;
import org.workcraft.formula.Nf;
import org.workcraft.formula.FormulaUtils;

import java.util.List;

public class Dnf extends Nf<DnfClause> {

    private static final DumbBooleanWorker WORKER = new DumbBooleanWorker();

    public Dnf() {
    }

    public Dnf(DnfClause... clauses) {
        super(clauses);
    }

    public Dnf(List<DnfClause> clauses) {
        super(clauses);
    }

    @Override
    public <T> T accept(BooleanVisitor<T> visitor) {
        return FormulaUtils.createOr(getClauses(), WORKER).accept(visitor);
    }

}
