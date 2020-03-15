package org.workcraft.plugins.cpog.encoding;

import java.util.Arrays;
import java.util.List;

import org.workcraft.formula.BooleanVariable;
import org.workcraft.formula.Literal;
import org.workcraft.formula.cnf.CnfClause;

public class CnfOperations {
    public static Literal not(Literal x) {
        return new Literal(x.getVariable(), !x.getNegation());
    }

    public static Literal not(BooleanVariable x) {
        return new Literal(x, true);
    }

    public static CnfClause or(List<BooleanVariable> literals) {
        CnfClause result = new CnfClause();
        for (BooleanVariable var : literals) {
            result.getLiterals().add(literal(var));
        }
        return result;
    }

    public static CnfClause or(Literal... literals) {
        return new CnfClause(Arrays.asList(literals));
    }

    public static Literal literal(BooleanVariable var) {
        return new Literal(var);
    }
}
