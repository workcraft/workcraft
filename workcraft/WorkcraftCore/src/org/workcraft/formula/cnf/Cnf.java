package org.workcraft.formula.cnf;

import org.workcraft.formula.FormulaUtils;
import org.workcraft.formula.Nf;
import org.workcraft.formula.visitors.BooleanVisitor;

import java.util.List;

public class Cnf extends Nf<CnfClause> {

    public Cnf() {
    }

    public Cnf(CnfClause clause) {
        super(clause);
    }

    public Cnf(List<CnfClause> clauses) {
        super(clauses);
    }

    @Override
    public <T> T accept(BooleanVisitor<T> visitor) {
        return FormulaUtils.createAnd(getClauses()).accept(visitor);
    }

}
