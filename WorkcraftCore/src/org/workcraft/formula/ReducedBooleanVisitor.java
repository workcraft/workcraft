package org.workcraft.formula;

public interface ReducedBooleanVisitor {
    void visit(And node);
    void visit(Not node);
    void visit(Iff node);
    void visit(BooleanVariable node);
}
