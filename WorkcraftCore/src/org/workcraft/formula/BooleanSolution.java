package org.workcraft.formula;

import java.util.Collection;

public interface BooleanSolution {
    Collection<BooleanVariable> getVariables();
    boolean getSolution(BooleanVariable variable);
}
