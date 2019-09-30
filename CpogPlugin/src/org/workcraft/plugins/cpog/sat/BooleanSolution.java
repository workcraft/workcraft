package org.workcraft.plugins.cpog.sat;

import org.workcraft.formula.BooleanVariable;

import java.util.Collection;

public interface BooleanSolution {
    Collection<BooleanVariable> getVariables();
    boolean getSolution(BooleanVariable variable);
}
