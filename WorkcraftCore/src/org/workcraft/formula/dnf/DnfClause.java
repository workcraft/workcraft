package org.workcraft.formula.dnf;

import java.util.List;

import org.workcraft.formula.BooleanOperations;
import org.workcraft.formula.BooleanVisitor;
import org.workcraft.formula.Clause;
import org.workcraft.formula.Literal;

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
        return BooleanOperations.and(getLiterals()).accept(visitor);
    }
}
