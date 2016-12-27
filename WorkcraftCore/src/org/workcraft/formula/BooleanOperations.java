package org.workcraft.formula;

import java.util.List;

public class BooleanOperations {
    //private static BooleanWorker worker = new DumbBooleanWorker();
    //private static BooleanWorker worker = new CleverBooleanWorker();
    // FIXME: It looks like CPOG relies on PrettifyBooleanWorker.
    private static final BooleanWorker defaultWorker = new PrettifyBooleanWorker(new MemoryConservingBooleanWorker());

    public static final Zero ZERO = Zero.instance();
    public static final One ONE = One.instance();

    public static BooleanFormula not(BooleanFormula x) {
        return not(x, defaultWorker);
    }

    public static BooleanFormula not(BooleanFormula x, BooleanWorker worker) {
        return worker.not(x);
    }

    public static BooleanFormula and(BooleanFormula x, BooleanFormula y) {
        return and(x, y, defaultWorker);
    }

    public static BooleanFormula and(BooleanFormula x, BooleanFormula y, BooleanWorker worker) {
        return worker.and(x, y);
    }

    public static BooleanFormula and(List<? extends BooleanFormula> conditions) {
        return createAnd(conditions, 0, conditions.size());
    }

    private static BooleanFormula createAnd(List<? extends BooleanFormula> conditions, int start, int end) {
        int size = end - start;
        if (size == 0) {
            return ONE;
        } else {
            if (size == 1) {
                return conditions.get(start);
            } else {
                int split = (end + start) / 2;
                return and(createAnd(conditions, start, split), createAnd(conditions, split, end));
            }
        }
    }

    public static BooleanFormula or(BooleanFormula x, BooleanFormula y) {
        return or(x, y, defaultWorker);
    }

    public static BooleanFormula or(BooleanFormula x, BooleanFormula y, BooleanWorker worker) {
        return worker.or(x, y);
    }

    public static BooleanFormula or(List<? extends BooleanFormula> conditions) {
        return createOr(conditions, 0, conditions.size());
    }

    private static BooleanFormula createOr(List<? extends BooleanFormula> conditions, int start, int end) {
        int size = end - start;
        if (size == 0) {
            return ZERO;
        } else {
            if (size == 1) {
                return conditions.get(start);
            } else {
                int split = (end + start) / 2;
                return or(createOr(conditions, start, split), createOr(conditions, split, end));
            }
        }
    }

    public static BooleanFormula iff(BooleanFormula x, BooleanFormula y) {
        return iff(x, y, defaultWorker);
    }

    public static BooleanFormula iff(BooleanFormula x, BooleanFormula y, BooleanWorker worker) {
        return worker.iff(x, y);
    }

    public static BooleanFormula xor(BooleanFormula x, BooleanFormula y) {
        return xor(x, y, defaultWorker);
    }

    public static BooleanFormula xor(BooleanFormula x, BooleanFormula y, BooleanWorker worker) {
        return worker.xor(x, y);
    }

    public static BooleanFormula imply(BooleanFormula x, BooleanFormula y) {
        return imply(x, y, defaultWorker);
    }

    public static BooleanFormula imply(BooleanFormula x, BooleanFormula y, BooleanWorker worker) {
        return worker.imply(x, y);
    }

}
