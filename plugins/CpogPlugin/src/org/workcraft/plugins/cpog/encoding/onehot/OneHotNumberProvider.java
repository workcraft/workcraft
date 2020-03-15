package org.workcraft.plugins.cpog.encoding.onehot;

import org.workcraft.formula.BooleanFormula;
import org.workcraft.formula.BooleanVariable;
import org.workcraft.formula.FreeVariable;
import org.workcraft.formula.One;
import org.workcraft.formula.FormulaUtils;
import org.workcraft.formula.workers.BooleanWorker;
import org.workcraft.plugins.cpog.formula.MemoryConservingBooleanWorker;
import org.workcraft.plugins.cpog.formula.PrettifyBooleanWorker;
import org.workcraft.plugins.cpog.encoding.NumberProvider;

import java.util.ArrayList;
import java.util.List;

public class OneHotNumberProvider implements NumberProvider<OneHotIntBooleanFormula> {

    private static final BooleanWorker WORKER = new PrettifyBooleanWorker(new MemoryConservingBooleanWorker());

    private final List<BooleanFormula> rho = new ArrayList<>();

    @Override
    public OneHotIntBooleanFormula generate(String varPrefix, int range) {
        List<BooleanVariable> vars = new ArrayList<>();
        for (int i = 0; i < range; i++) {
            vars.add(new FreeVariable(varPrefix + "sel" + i));
        }

        for (int i = 0; i < range; i++) {
            for (int j = i + 1; j < range; j++) {
                rho.add(WORKER.or(WORKER.not(vars.get(i)), WORKER.not(vars.get(j))));
            }
        }
        rho.add(FormulaUtils.createOr(vars, WORKER));
        return new OneHotIntBooleanFormula(vars);
    }

    @Override
    public BooleanFormula select(BooleanFormula[] booleanFormulas, OneHotIntBooleanFormula number) {
        if (number.getRange() != booleanFormulas.length) {
            throw new RuntimeException("Lengths do not match");
        }
        List<BooleanFormula> result = new ArrayList<>();
        for (int i = 0; i < booleanFormulas.length; i++) {
            result.add(WORKER.imply(number.get(i), booleanFormulas[i]));
        }
        return FormulaUtils.createAnd(result, WORKER);
    }

    @Override
    public BooleanFormula getConstraints() {
        return FormulaUtils.createAnd(rho, WORKER);
    }

    @Override
    public BooleanFormula less(OneHotIntBooleanFormula a, OneHotIntBooleanFormula b) {
        return One.getInstance();
    }
}
