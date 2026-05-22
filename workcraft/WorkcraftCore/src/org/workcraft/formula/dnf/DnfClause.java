package org.workcraft.formula.dnf;

import org.workcraft.formula.Clause;
import org.workcraft.formula.FormulaUtils;
import org.workcraft.formula.Literal;
import org.workcraft.formula.visitors.BooleanVisitor;

import java.util.List;

public class DnfClause extends Clause {

    public DnfClause() {
    }

    public DnfClause(Literal... literals) {
        super(literals);
    }

    public DnfClause(List<Literal> literals) {
        super(literals);
    }

    @Override
    public <T> T accept(BooleanVisitor<T> visitor) {
        return FormulaUtils.createAnd(getLiterals()).accept(visitor);
    }

}
