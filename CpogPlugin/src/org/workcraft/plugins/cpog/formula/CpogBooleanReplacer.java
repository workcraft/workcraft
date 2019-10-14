package org.workcraft.plugins.cpog.formula;

import org.workcraft.formula.BooleanFormula;
import org.workcraft.formula.BooleanVariable;
import org.workcraft.formula.One;
import org.workcraft.formula.Zero;
import org.workcraft.formula.visitors.BooleanReplacer;
import org.workcraft.formula.workers.BooleanWorker;
import org.workcraft.plugins.cpog.Variable;

import java.util.HashMap;

public class CpogBooleanReplacer extends BooleanReplacer {

    private static final HashMap<BooleanVariable, BooleanFormula> MAP = new HashMap<>();
    private static final BooleanWorker WORKER = new PrettifyBooleanWorker(new MemoryConservingBooleanWorker());

    public CpogBooleanReplacer() {
        super(MAP, WORKER);
    }

    @Override
    public BooleanFormula visit(BooleanVariable node) {
        switch (((Variable) node).getState()) {
        case TRUE:
            return One.getInstance();
        case FALSE:
            return Zero.getInstance();
        default:
            return node;
        }
    }

}
