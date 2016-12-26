package org.workcraft.formula.sat;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.workcraft.formula.BooleanVariable;
import org.workcraft.formula.Literal;
import org.workcraft.formula.cnf.Cnf;
import org.workcraft.formula.cnf.CnfClause;
import org.workcraft.formula.cnf.CnfPrinter;

public class MiniSatCnfPrinter implements CnfPrinter {
    Map<BooleanVariable, Integer> numbers = new HashMap<>();
    int varCount = 0;
    private Cnf cnf;

    @Override
    public String print(Cnf cnf) {
        this.cnf = cnf;
        StringBuilder result = new StringBuilder();
        for (CnfClause clause : cnf.getClauses()) {
            for (Literal literal : clause.getLiterals()) {
                Integer number = getNumber(literal.getVariable());
                if (literal.getNegation()) {
                    result.append("-");
                }
                result.append(number.toString());
                result.append(" ");
            }
            result.append("0");
            result.append("\n");
        }

        result.insert(0, getHeadLine());
        result.insert(0, getHeadComments());

        return result.toString();
    }

    private String getHeadComments() {
        StringBuilder result = new StringBuilder();

        for (BooleanVariable var : new TreeMap<BooleanVariable, Integer>(numbers).keySet()) {
            String label = var.getLabel();
            if (!label.isEmpty()) {
                result.append("c " + numbers.get(var) + " " + label + "\n");
            }
        }

        return result.toString();
    }

    private String getHeadLine() {
        return "p cnf " + varCount + " " + cnf.getClauses().size() + "\n";
    }

    private Integer getNumber(BooleanVariable variable) {
        Integer res = numbers.get(variable);
        if (res == null) {
            res = ++varCount;
            numbers.put(variable, res);
        }
        return res;
    }

}
