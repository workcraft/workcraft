package org.workcraft.formula.utils;

import org.workcraft.formula.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.workcraft.formula.BooleanOperations.*;

public class BooleanReplacer implements BooleanVisitor<BooleanFormula> {

    interface BinaryOperation {
        BooleanFormula apply(BooleanFormula x, BooleanFormula y);
    }

    private final HashMap<BooleanFormula, BooleanFormula> map;
    private final BooleanWorker worker;

    public BooleanReplacer(List<? extends BooleanVariable> from, List<? extends BooleanFormula> to, BooleanWorker worker) {
        this.map = new HashMap<>();
        if (from.size() != to.size()) {
            throw new RuntimeException("Length of the variable list must be equal to that of formula list.");
        }
        for (int i = 0; i < from.size(); i++) {
            this.map.put(from.get(i), to.get(i));
        }
        this.worker = worker;
    }

    public BooleanReplacer(Map<? extends BooleanVariable, ? extends BooleanFormula> map, BooleanWorker worker) {
        this.map = new HashMap<>(map);
        this.worker = worker;
    }

    @Override
    public BooleanFormula visit(Zero node) {
        return node;
    }

    @Override
    public BooleanFormula visit(One node) {
        return node;
    }

    @Override
    public BooleanFormula visit(BooleanVariable node) {
        BooleanFormula replacement = map.get(node);
        return replacement != null ? replacement : node;
    }

    @Override
    public BooleanFormula visit(Not node) {
        BooleanFormula result = map.get(node);
        if (result == null) {
            BooleanFormula x = node.getX().accept(this);
            if (node.getX() == x) {
                result = node;
            } else {
                result = not(x);
            }
            map.put(node, result);
        }
        return result;
    }

    @Override
    public BooleanFormula visit(And node) {
        return visitBinaryFunc(node, (x, y) -> and(x, y, worker));
    }

    @Override
    public BooleanFormula visit(Or node) {
        return visitBinaryFunc(node, (x, y) -> or(x, y, worker));
    }

    @Override
    public BooleanFormula visit(Xor node) {
        return visitBinaryFunc(node, (x, y) -> xor(x, y, worker));
    }

    @Override
    public BooleanFormula visit(Imply node) {
        return visitBinaryFunc(node, (x, y) -> imply(x, y, worker));
    }

    @Override
    public BooleanFormula visit(Iff node) {
        return visitBinaryFunc(node, (x, y) -> iff(x, y, worker));
    }

    private BooleanFormula visitBinaryFunc(BinaryBooleanFormula node, BinaryOperation op) {
        BooleanFormula result = map.get(node);
        if (result == null) {
            BooleanFormula x = node.getX().accept(this);
            BooleanFormula y = node.getY().accept(this);
            if ((node.getX() == x) && (node.getY() == y)) {
                result = node;
            } else {
                result = op.apply(x, y);
            }
            map.put(node, result);
        }
        return result;
    }

}
