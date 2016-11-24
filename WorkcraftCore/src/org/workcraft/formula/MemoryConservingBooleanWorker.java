/*
 *
 * Copyright 2008,2009 Newcastle University
 *
 * This file is part of Workcraft.
 *
 * Workcraft is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Workcraft is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Workcraft.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.workcraft.formula;

import java.util.HashMap;
import java.util.Map;

public class MemoryConservingBooleanWorker implements ReducedBooleanWorker {
    Map<BooleanFormula, Integer> codes = new HashMap<>();
    Map<IntPair, BooleanFormula> ands = new HashMap<>();
    Map<IntPair, BooleanFormula> iffs = new HashMap<>();
    Map<Integer, BooleanFormula> nots = new HashMap<>();
    int nextCode = 0;

    Integer getCode(BooleanFormula f) {
        Integer code = codes.get(f);
        if (code == null) {
            // if (!(f instanceof FreeVariable))
            // System.out.println("Warning: Unknown code for formula f=[" +
            // f.getClass().getSimpleName()+"] " + formulaToStr(f));
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
        IntPair(Integer x, Integer y) {
            if (y < x) {
                Integer tmp = x;
                x = y;
                y = tmp;
            }
            this.x = x;
            this.y = y;
        }

        final Integer x;
        final Integer y;

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
