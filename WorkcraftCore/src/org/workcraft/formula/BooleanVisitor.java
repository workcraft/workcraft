package org.workcraft.formula;

public interface BooleanVisitor<T> {
    T visit(And node);
    T visit(Iff node);
    T visit(Xor node);
    T visit(Zero node);
    T visit(One node);
    T visit(Not node);
    T visit(Imply node);

    T visit(BooleanVariable variable);
    T visit(Or node);
}
