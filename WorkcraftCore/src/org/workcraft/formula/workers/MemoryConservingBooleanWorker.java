package org.workcraft.formula.workers;

import org.workcraft.formula.*;
import org.workcraft.formula.visitors.BooleanVisitor;

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
            res = f.accept(Inverter.instance);
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

    private static class Inverter implements BooleanVisitor<BooleanFormula> {
        public static final Inverter instance = new Inverter();

        @Override
        public BooleanFormula visit(Not node) {
            return node.getX();
        }

        protected BooleanFormula visitDefault(BooleanFormula node) {
            return new Not(node);
        }

        @Override
        public BooleanFormula visit(And node) {
            return visitDefault(node);
        }

        @Override
        public BooleanFormula visit(Iff node) {
            return visitDefault(node);
        }

        @Override
        public BooleanFormula visit(Xor node) {
            return visitDefault(node);
        }

        @Override
        public BooleanFormula visit(Zero node) {
            throw new RuntimeException("no constants expected here");
        }

        @Override
        public BooleanFormula visit(One node) {
            throw new RuntimeException("no constants expected here");
        }

        @Override
        public BooleanFormula visit(Imply node) {
            return visitDefault(node);
        }

        @Override
        public BooleanFormula visit(BooleanVariable node) {
            return visitDefault(node);
        }

        @Override
        public BooleanFormula visit(Or node) {
            return visitDefault(node);
        }
    }

}
