package org.workcraft.plugins.cpog.formula;

public interface CpogFormula {
    <T> T accept(CpogVisitor<T> visitor);
}
