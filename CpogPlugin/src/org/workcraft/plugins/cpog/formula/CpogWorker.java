package org.workcraft.plugins.cpog.formula;

public interface CpogWorker {

    CpogFormula overlay(CpogFormula x, CpogFormula y);
    CpogFormula sequence(CpogFormula x, CpogFormula y);
}
