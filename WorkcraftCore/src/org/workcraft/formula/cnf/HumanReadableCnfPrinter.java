package org.workcraft.formula.cnf;

import org.workcraft.formula.Literal;

public class HumanReadableCnfPrinter implements CnfPrinter {

    @Override
    public String print(Cnf cnf) {
        StringBuilder result = new StringBuilder();
        for (CnfClause clause : cnf.getClauses()) {
            for (Literal literal : clause.getLiterals()) {
                result.append(literal.getVariable().getLabel());
                if (literal.getNegation()) {
                    result.append("'");
                }
                result.append(" ");
            }
            result.append("\n");
        }
        return result.toString();
    }

}
