package org.workcraft.formula.encoding;

import static org.workcraft.formula.encoding.CnfOperations.not;
import static org.workcraft.formula.encoding.CnfOperations.or;

import java.util.ArrayList;
import java.util.List;

import org.workcraft.formula.Literal;
import org.workcraft.formula.cnf.Cnf;
import org.workcraft.formula.cnf.CnfClause;

public class CnfSorter {
    public static Cnf sortRound(List<Literal> result, List<Literal> x) {
        if (x.size() != result.size()) {
            throw new RuntimeException("sizes do not match");
        }

        List<Literal> s = new ArrayList<>();
        for (Literal var : x) {
            s.add(new Literal(var.getVariable().getLabel() + "_th"));
        }

        return sortRound(result, s, x);
    }

    private static Cnf makeThermometer(List<Literal> s, List<Literal> x) {
        // s[i] = s[i-1] + x[i]
        // (!s[i] + s[i-1] + x[i]) (!s[i-1] + s[i]) (!x[i] + s[i])
        Cnf result = new Cnf();

        result.addClauses(or(not(s.get(0)), x.get(0)));
        result.addClauses(or(not(x.get(0)), s.get(0)));
        for (int i = 1; i < s.size(); i++) {
            result.addClauses(or(not(s.get(i)), s.get(i - 1), x.get(i)));
            result.addClauses(or(not(s.get(i - 1)), s.get(i)));
            result.addClauses(or(not(x.get(i)), s.get(i)));
        }

        return result;
    }

    public static Cnf sortRound(List<Literal> result, List<Literal> s, List<Literal> x) {

        List<CnfClause> clauses = new ArrayList<>();

        Cnf thermoCnf = makeThermometer(s, x);
        clauses.addAll(thermoCnf.getClauses());

        // y[x] = s[i] x[i+1]
        // (!y[i] + s[i]) (!y[i] + x[i+1]) (!s[i] + !x[i+1] + y[i])
        for (int i = 0; i < x.size() - 1; i++) {
            clauses.add(or(not(result.get(i)), s.get(i)));
            clauses.add(or(not(result.get(i)), x.get(i + 1)));
            clauses.add(or(not(s.get(i)), not(x.get(i + 1)), result.get(i)));
        }

        int n = x.size() - 1;
        clauses.add(or(not(result.get(n)), s.get(n)));
        clauses.add(or(not(s.get(n)), result.get(n)));

        return new Cnf(clauses);
    }
}
