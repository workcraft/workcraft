package org.workcraft.formula.cnf;

import java.util.List;

import org.workcraft.formula.BooleanOperations;
import org.workcraft.formula.BooleanVisitor;
import org.workcraft.formula.Nf;

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
    public String toString() {
        return new SimpleCnfPrinter().print(this);
    }

    public String toString(CnfPrinter cnfPrinter) {
        return cnfPrinter.print(this);
    }

    @Override
    public <T> T accept(BooleanVisitor<T> visitor) {
        return BooleanOperations.and(getClauses()).accept(visitor);
    }

}
