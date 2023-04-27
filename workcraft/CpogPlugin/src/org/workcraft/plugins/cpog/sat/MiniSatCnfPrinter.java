package org.workcraft.plugins.cpog.sat;

import org.workcraft.formula.BooleanVariable;
import org.workcraft.formula.Literal;
import org.workcraft.formula.cnf.Cnf;
import org.workcraft.formula.cnf.CnfClause;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class MiniSatCnfPrinter {

    public static String print(Cnf cnf) {
        StringBuilder result = new StringBuilder();
        Map<BooleanVariable, Integer> numbers = new HashMap<>();
        int varCount = 0;
        for (CnfClause clause : cnf.getClauses()) {
            for (Literal literal : clause.getLiterals()) {
                BooleanVariable variable = literal.getVariable();
                Integer number = numbers.get(variable);
                if (number == null) {
                    number = ++varCount;
                    numbers.put(variable, number);
                }
                if (literal.getNegation()) {
                    result.append('-');
                }
                result.append(number.toString());
                result.append(' ');
            }
            result.append('0');
            result.append('\n');
        }
        result.insert(0, "p cnf " + varCount + ' ' + cnf.getClauses().size() + '\n');

        StringBuilder head = new StringBuilder();
        for (BooleanVariable var : new TreeMap<>(numbers).keySet()) {
            String label = var.getLabel();
            if (!label.isEmpty()) {
                head.append("c " + numbers.get(var) + ' ' + label + '\n');
            }
        }
        result.insert(0, head.toString());
        return result.toString();
    }

}
