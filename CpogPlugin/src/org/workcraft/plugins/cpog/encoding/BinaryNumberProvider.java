package org.workcraft.plugins.cpog.encoding;

import org.workcraft.formula.BooleanFormula;
import org.workcraft.formula.BooleanVariable;
import org.workcraft.formula.FreeVariable;
import org.workcraft.formula.Zero;
import org.workcraft.formula.FormulaUtils;
import org.workcraft.formula.workers.BooleanWorker;
import org.workcraft.formula.workers.MemoryConservingBooleanWorker;
import org.workcraft.formula.workers.PrettifyBooleanWorker;

import java.util.ArrayList;
import java.util.List;

public class BinaryNumberProvider implements NumberProvider<BinaryIntBooleanFormula> {

    private static final BooleanWorker WORKER = new PrettifyBooleanWorker(new MemoryConservingBooleanWorker());

    private final List<BooleanFormula> constraints = new ArrayList<>();

    @Override
    public BinaryIntBooleanFormula generate(String varPrefix, int range) {
        if (range == 0) {
            throw new RuntimeException("range=0");
        }
        int varCount = 0;
        int tmp = range - 1;
        while (tmp > 0) {
            tmp /= 2;
            varCount++;
        }

        List<BooleanVariable> vars = new ArrayList<>();
        for (int i = 0; i < varCount; i++) {
            vars.add(new FreeVariable(varPrefix + "b" + i));
        }
        if (1 << varCount != range) {
            constraints.add(less(vars, varCount - 1, range));
        }
        return new BinaryIntBooleanFormula(vars, range);
    }

    private BooleanFormula less(List<BooleanVariable> a, int n, int b) {
        BooleanVariable an = a.get(n);
        boolean bn = ((b >> n) & 1) > 0;
        BooleanFormula nan = WORKER.not(an);
        if (n == 0) {
            if (bn) {
                return nan;
            } else {
                return Zero.getInstance();
            }
        }
        BooleanFormula l = less(a, n - 1, b);
        if (bn) {
            return WORKER.or(nan, l);
        } else {
            return WORKER.and(nan, l);
        }
    }

    @Override
    public BooleanFormula getConstraints() {
        return FormulaUtils.createAnd(constraints, WORKER);
    }

    @Override
    public BooleanFormula select(BooleanFormula[] vars, BinaryIntBooleanFormula number) {
        if (number.getValuesCount() != vars.length) {
            throw new RuntimeException(
                    "lengths do not match: vars=" + vars.length + ", number=" + number.getValuesCount());
        }
        List<BooleanVariable> bits = number.getVars();
        return select(vars, bits, bits.size(), 0, number.getValuesCount());
    }

    private BooleanFormula select(BooleanFormula[] vars, List<BooleanVariable> bits, int length, int offset, int threshold) {
        if (offset >= threshold) {
            return Zero.getInstance();
        }
        if (length == 0) {
            return vars[offset];
        }
        BooleanVariable x = bits.get(length - 1);
        BooleanFormula nx = WORKER.not(x);
        return WORKER.or(
                WORKER.and(x, select(vars, bits, length - 1, offset + (1 << (length - 1)), threshold)),
                WORKER.and(nx, select(vars, bits, length - 1, offset, threshold)));
    }

    @Override
    public BooleanFormula less(BinaryIntBooleanFormula a, BinaryIntBooleanFormula b) {
        return less(a.getVars(), b.getVars(), a.getVars().size() - 1);
    }

    private BooleanFormula less(List<BooleanVariable> a, List<BooleanVariable> b, int n) {
        BooleanVariable an = a.get(n);
        BooleanFormula bn = b.get(n);
        BooleanFormula nan = WORKER.not(an);
        if (n == 0) {
            return WORKER.and(nan, bn);
        }
        BooleanFormula l = less(a, b, n - 1);
        return WORKER.or(WORKER.and(nan, bn), WORKER.and(WORKER.or(nan, bn), l));
    }

}
