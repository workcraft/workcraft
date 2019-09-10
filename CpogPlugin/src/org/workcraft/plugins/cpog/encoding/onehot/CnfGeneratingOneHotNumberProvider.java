package org.workcraft.plugins.cpog.encoding.onehot;

import static org.workcraft.plugins.cpog.encoding.CnfOperations.not;
import static org.workcraft.plugins.cpog.encoding.CnfOperations.or;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.workcraft.formula.BooleanFormula;
import org.workcraft.formula.BooleanOperations;
import org.workcraft.formula.BooleanVariable;
import org.workcraft.formula.FreeVariable;
import org.workcraft.formula.Literal;
import org.workcraft.formula.One;
import org.workcraft.formula.cnf.Cnf;
import org.workcraft.formula.cnf.CnfClause;
import org.workcraft.plugins.cpog.encoding.CnfSorter;
import org.workcraft.plugins.cpog.encoding.NumberProvider;
import org.workcraft.formula.utils.BooleanUtils;

class CnfGeneratingOneHotNumberProvider implements NumberProvider<OneHotIntBooleanFormula> {
    private final List<CnfClause> rho = new ArrayList<>();

    CnfGeneratingOneHotNumberProvider() {
    }

    public OneHotIntBooleanFormula generate(String varPrefix, int range) {
        List<BooleanVariable> vars = new ArrayList<>();
        for (int i = 0; i < range; i++) {
            vars.add(new FreeVariable(varPrefix + "sel" + i));
        }

        List<Literal> literals = new ArrayList<>();

        boolean useSorting = true;

        if (!useSorting) {
            for (int i = 0; i < range; i++) {
                for (int j = i + 1; j < range; j++) {
                    rho.add(or(not(vars.get(i)), not(vars.get(j))));
                }
            }

            rho.add(or(vars));
        } else {
            List<Literal> sorted = new ArrayList<>();
            for (int i = 0; i < range; i++) {
                literals.add(new Literal(vars.get(i)));
                sorted.add(new Literal(varPrefix + "sorted" + i));
            }

            Cnf sorting = CnfSorter.sortRound(sorted, literals);

            for (int i = 0; i < range - 1; i++) {
                rho.add(or(not(sorted.get(i))));
            }
            rho.add(or(sorted.get(range - 1)));
            rho.addAll(sorting.getClauses());
        }

        return new OneHotIntBooleanFormula(vars);
    }

    public static List<CnfClause> select(Literal[] vars, OneHotIntBooleanFormula number, boolean inverse) {
        List<CnfClause> conditions = new ArrayList<>();

        if (number.getRange() != vars.length) {
            throw new RuntimeException("Lengths do not match");
        }

        for (int i = 0; i < vars.length; i++) {
            conditions.add(or(not(number.get(i)), inverse ? not(vars[i]) : vars[i]));
        }

        return conditions;
    }

    public static List<CnfClause> select(Literal result, Literal[] vars, OneHotIntBooleanFormula code) {
        List<CnfClause> conditions = new ArrayList<>();

        if (code.getRange() != vars.length) {
            throw new RuntimeException("Lengths do not match");
        }

        Literal notResult = not(result);
        for (int i = 0; i < vars.length; i++) {
            conditions.add(or(notResult, not(code.get(i)), vars[i]));
            conditions.add(or(result, not(code.get(i)), not(vars[i])));
        }

        return conditions;
    }

    public List<CnfClause> getConstraintClauses() {
        return rho;
    }

    public BooleanFormula less(OneHotIntBooleanFormula a,
            OneHotIntBooleanFormula b) {
        return One.instance();
    }

    @Override
    public BooleanFormula select(BooleanFormula[] vars, OneHotIntBooleanFormula number) {
        List<BooleanVariable> params = new ArrayList<>();
        Literal[]literals = new Literal[vars.length];

        for (int i = 0; i < vars.length; i++) {
            BooleanVariable var = new FreeVariable("param" + i);
            params.add(var);
            literals[i] = new Literal(var);
        }

        List<CnfClause> result = select(literals, number, false);

        Cnf cnf = new Cnf(result);
        return BooleanUtils.replacePretty(cnf, params, Arrays.asList(vars));
    }

    @Override
    public BooleanFormula getConstraints() {
        return BooleanOperations.and(getConstraintClauses());
    }

}
