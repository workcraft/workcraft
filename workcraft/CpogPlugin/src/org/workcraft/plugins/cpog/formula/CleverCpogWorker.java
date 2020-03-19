package org.workcraft.plugins.cpog.formula;

public class CleverCpogWorker implements CpogWorker {
    @Override
    public CpogFormula overlay(CpogFormula x, CpogFormula y) {
        if (x == y) {
            return x;
        }
        return new Overlay(x, y);
    }

    @Override
    public CpogFormula sequence(CpogFormula x, CpogFormula y) {
        return new Sequence(x, y);
    }
}
