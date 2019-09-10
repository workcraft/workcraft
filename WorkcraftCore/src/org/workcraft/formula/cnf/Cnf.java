package org.workcraft.formula.cnf;

import org.workcraft.formula.BooleanOperations;
import org.workcraft.formula.BooleanVisitor;
import org.workcraft.formula.Nf;

import java.util.List;

public class Cnf extends Nf<CnfClause> {

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
        return BooleanOperations.and(getClauses()).accept(visitor);
    }

}
