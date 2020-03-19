package org.workcraft.plugins.cpog.formula;

public class DumbCpogWorker implements CpogWorker {
    @Override
    public CpogFormula overlay(CpogFormula x, CpogFormula y) {
        return new Overlay(x, y);
    }

    @Override
    public CpogFormula sequence(CpogFormula x, CpogFormula y) {
        return new Sequence(x, y);
    }
}
