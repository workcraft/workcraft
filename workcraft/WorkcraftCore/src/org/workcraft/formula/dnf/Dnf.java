package org.workcraft.formula.dnf;

import org.workcraft.formula.FormulaUtils;
import org.workcraft.formula.Nf;
import org.workcraft.formula.visitors.BooleanVisitor;

import java.util.List;

public class Dnf extends Nf<DnfClause> {

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
        return FormulaUtils.createOr(getClauses()).accept(visitor);
    }

}
