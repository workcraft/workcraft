package org.workcraft.formula.dnf;

import java.util.List;

import org.workcraft.formula.BooleanOperations;
import org.workcraft.formula.BooleanVisitor;
import org.workcraft.formula.Nf;

public class Dnf extends Nf<DnfClause> {

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
        return BooleanOperations.or(getClauses()).accept(visitor);
    }
}
