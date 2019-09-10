package org.workcraft.plugins.cpog.sat;

import org.workcraft.formula.BooleanFormula;

public class OptimisationTask<T> {
    private final BooleanFormula[][] encodingVars;
    private final BooleanFormula[] functionVars;
    private final T task;

    public OptimisationTask(BooleanFormula[] functionVars, BooleanFormula[][] encodingVars, T task) {
        this.functionVars = functionVars;
        this.encodingVars = encodingVars;
        this.task = task;
    }

    public BooleanFormula[][] getEncodingVars() {
        return encodingVars;
    }
    public BooleanFormula[] getFunctionVars() {
        return functionVars;
    }

    public T getTask() {
        return task;
    }
}
