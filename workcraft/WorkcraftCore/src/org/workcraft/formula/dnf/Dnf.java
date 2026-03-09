package org.workcraft.formula.dnf;

import org.workcraft.formula.FormulaUtils;
import org.workcraft.formula.Nf;
import org.workcraft.formula.visitors.BooleanVisitor;
import org.workcraft.formula.workers.DumbBooleanWorker;

import java.util.List;

public class Dnf extends Nf<DnfClause> {

    private static final DumbBooleanWorker WORKER = DumbBooleanWorker.getInstance();

    public Dnf() {
    }

    public Dnf(DnfClause clause) {
        super(clause);
    }

    public Dnf(List<DnfClause> clauses) {
        super(clauses);
    }

    @Override
    public <T> T accept(BooleanVisitor<T> visitor) {
        return FormulaUtils.createOr(getClauses(), WORKER).accept(visitor);
    }

}
