package org.workcraft.formula.cnf;

import java.util.List;

import org.workcraft.formula.BooleanOperations;
import org.workcraft.formula.BooleanVisitor;
import org.workcraft.formula.Clause;
import org.workcraft.formula.Literal;

public class CnfClause extends Clause {

    public CnfClause() {
    }

    public CnfClause(Literal... literals) {
        super(literals);
    }

    public CnfClause(List<Literal> literals) {
        super(literals);
    }

    @Override
    public <T> T accept(BooleanVisitor<T> visitor) {
        return BooleanOperations.or(getLiterals()).accept(visitor);
    }

}
