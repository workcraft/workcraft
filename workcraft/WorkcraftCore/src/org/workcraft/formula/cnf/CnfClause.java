package org.workcraft.formula.cnf;

import org.workcraft.formula.Clause;
import org.workcraft.formula.FormulaUtils;
import org.workcraft.formula.Literal;
import org.workcraft.formula.visitors.BooleanVisitor;

import java.util.List;

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
        return FormulaUtils.createOr(getLiterals()).accept(visitor);
    }

}
