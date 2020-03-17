package org.workcraft.plugins.cpog.formula;

public abstract class BinaryCpogFormula implements CpogFormula {
    private final CpogFormula x;
    private final CpogFormula y;
    public BinaryCpogFormula(CpogFormula x, CpogFormula y) {
        this.x = x;
        this.y = y;
    }
    public CpogFormula getX() {
        return x;
    }
    public CpogFormula getY() {
        return y;
    }
}
