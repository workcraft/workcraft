package org.workcraft.formula;

import org.workcraft.formula.visitors.BooleanVisitor;

public interface BooleanFormula {
    <T> T accept(BooleanVisitor<T> visitor);
}
