package org.workcraft.plugins.cpog.formula;

import java.util.HashMap;

import org.workcraft.formula.BooleanFormula;
import org.workcraft.formula.BooleanVariable;
import org.workcraft.formula.BooleanWorker;
import org.workcraft.formula.MemoryConservingBooleanWorker;
import org.workcraft.formula.One;
import org.workcraft.formula.PrettifyBooleanWorker;
import org.workcraft.formula.Zero;
import org.workcraft.formula.utils.BooleanReplacer;
import org.workcraft.plugins.cpog.Variable;

public class PrettifyBooleanReplacer extends BooleanReplacer {

    private static final HashMap<BooleanVariable, BooleanFormula> MAP = new HashMap<>();
    private static final BooleanWorker WORKER = new PrettifyBooleanWorker(new MemoryConservingBooleanWorker());

    public PrettifyBooleanReplacer() {
        super(MAP, WORKER);
    }

    @Override
    public BooleanFormula visit(BooleanVariable node) {
        switch (((Variable) node).getState()) {
        case TRUE:
            return One.instance();
        case FALSE:
            return Zero.instance();
        default:
            return node;
        }
    }

}
