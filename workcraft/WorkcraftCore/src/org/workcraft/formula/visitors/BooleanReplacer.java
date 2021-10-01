package org.workcraft.formula.visitors;

import org.workcraft.formula.*;
import org.workcraft.formula.workers.BooleanWorker;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        return map.getOrDefault(node, node);
    }

    @Override
    public BooleanFormula visit(Not node) {
        if (map.containsKey(node)) {
            return map.get(node);
        }
        BooleanFormula result = null;
        BooleanFormula x = node.getX();
        if (x != null) {
            BooleanFormula evalX = x.accept(this);
            if (evalX != null) {
                result = (x == evalX) ? node : worker.not(evalX);
            }
        }
        map.put(node, result);
        return result;
    }

    @Override
    public BooleanFormula visit(And node) {
        return visitBinaryFunc(node, worker::and);
    }

    @Override
    public BooleanFormula visit(Or node) {
        return visitBinaryFunc(node, worker::or);
    }

    @Override
    public BooleanFormula visit(Xor node) {
        return visitBinaryFunc(node, worker::xor);
    }

    @Override
    public BooleanFormula visit(Imply node) {
        return visitBinaryFunc(node, worker::imply);
    }

    @Override
    public BooleanFormula visit(Iff node) {
        return visitBinaryFunc(node, worker::iff);
    }

    private BooleanFormula visitBinaryFunc(BinaryBooleanFormula node, BinaryOperation op) {
        if (map.containsKey(node)) {
            return map.get(node);
        }
        BooleanFormula x = node.getX();
        BooleanFormula y = node.getY();
        BooleanFormula result = null;
        if (x == null) {
            result = y;
        } else if (y == null) {
            result = x;
        } else {
            BooleanFormula evalX = x.accept(this);
            BooleanFormula evalY = y.accept(this);
            if (evalX == null) {
                result = evalY;
            } else if (evalY == null) {
                result = evalX;
            } else {
                result = ((x == evalX) && (y == evalY)) ? node : op.apply(evalX, evalY);
            }
        }
        map.put(node, result);
        return result;
    }

}
