package org.workcraft.plugins.cpog.optimisation.booleanvisitors;

import java.util.HashMap;

import org.workcraft.plugins.cpog.Variable;
import org.workcraft.plugins.cpog.optimisation.BooleanFormula;
import org.workcraft.plugins.cpog.optimisation.BooleanVariable;
import org.workcraft.plugins.cpog.optimisation.expressions.BooleanWorker;
import org.workcraft.plugins.cpog.optimisation.expressions.MemoryConservingBooleanWorker;
import org.workcraft.plugins.cpog.optimisation.expressions.One;
import org.workcraft.plugins.cpog.optimisation.expressions.PrettifyBooleanWorker;
import org.workcraft.plugins.cpog.optimisation.expressions.Zero;

public class PrettifyBooleanReplacer extends BooleanReplacer {

    private static final HashMap<BooleanVariable, BooleanFormula> MAP = new HashMap<BooleanVariable, BooleanFormula>();
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
