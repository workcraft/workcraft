package org.workcraft.plugins.cpog.formula;

public interface CpogVisitor<T> {
    T visit(Overlay node);
    T visit(Sequence node);

    T visit(CpogFormulaVariable variable);
}
