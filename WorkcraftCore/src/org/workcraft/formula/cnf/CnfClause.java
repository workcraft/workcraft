package org.workcraft.formula.cnf;

import org.workcraft.formula.visitors.BooleanVisitor;
import org.workcraft.formula.Clause;
import org.workcraft.formula.workers.DumbBooleanWorker;
import org.workcraft.formula.Literal;
import org.workcraft.formula.FormulaUtils;

import java.util.List;

public class CnfClause extends Clause {

    private static final DumbBooleanWorker WORKER = DumbBooleanWorker.getInstance();

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
        return FormulaUtils.createOr(getLiterals(), WORKER).accept(visitor);
    }

}
