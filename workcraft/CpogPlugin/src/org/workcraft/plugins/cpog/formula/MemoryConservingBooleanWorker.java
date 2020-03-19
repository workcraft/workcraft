package org.workcraft.plugins.cpog.formula;

import org.workcraft.formula.And;
import org.workcraft.formula.BooleanFormula;
import org.workcraft.formula.FormulaUtils;
import org.workcraft.formula.Iff;

import java.util.HashMap;
import java.util.Map;

public class MemoryConservingBooleanWorker implements ReducedBooleanWorker {

    private final Map<BooleanFormula, Integer> codes = new HashMap<>();
    private final Map<IntPair, BooleanFormula> ands = new HashMap<>();
    private final Map<IntPair, BooleanFormula> iffs = new HashMap<>();
    private final Map<Integer, BooleanFormula> nots = new HashMap<>();
    private int nextCode = 0;

    private Integer getCode(BooleanFormula f) {
        Integer code = codes.get(f);
        if (code == null) {
            code = newCode(f);
        }
        return code;
    }

    private Integer newCode(BooleanFormula f) {
        if (codes.containsKey(f)) {
            throw new RuntimeException("Code already exists for formula: ");
        }
        int code = nextCode++;
        codes.put(f, code);
        return code;
    }

    class IntPair {
        private final Integer x;
        private final Integer y;

        IntPair(Integer x, Integer y) {
            if (y < x) {
                Integer tmp = x;
                x = y;
                y = tmp;
            }
            this.x = x;
            this.y = y;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            IntPair other = (IntPair) obj;
            if (!other.x.equals(x)) {
                return false;
            }
            if (!other.y.equals(y)) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            return x * 1037 + y;
        }
    }

    @Override
    public BooleanFormula not(BooleanFormula f) {
        Integer code = getCode(f);
        BooleanFormula res = nots.get(code);
        if (res == null) {
            res = FormulaUtils.invert(f);
            ensureHaveCode(res);
            nots.put(code, res);
        }
        return res;
    }

    @Override
    public BooleanFormula and(BooleanFormula x, BooleanFormula y) {
        IntPair pair = getCodePair(x, y);
        BooleanFormula result = ands.get(pair);
        if (result == null) {
            result = new And(x, y);
            ands.put(pair, result);
            newCode(result);
        }
        return result;
    }

    private IntPair getCodePair(BooleanFormula x, BooleanFormula y) {
        Integer xCode = getCode(x);
        Integer yCode = getCode(y);
        return new IntPair(xCode, yCode);
    }

    @Override
    public BooleanFormula iff(BooleanFormula x, BooleanFormula y) {
        IntPair pair = getCodePair(x, y);
        IntPair pairInv = getCodePair(not(x), not(y));
        if (pair.x > pairInv.x) {
            pair = pairInv;
            x = not(x);
            y = not(y);
        }
        BooleanFormula result = iffs.get(pair);
        if (result == null) {
            result = new Iff(x, y);
            iffs.put(pair, result);
            newCode(result);
        }
        return result;
    }

    private void ensureHaveCode(BooleanFormula inverted) {
        if (!codes.containsKey(inverted)) {
            newCode(inverted);
        }
    }

}
