package org.workcraft.formula.visitors;

import org.workcraft.formula.*;

public interface BooleanVisitor<T> {
    // Constant
    T visit(Zero node);
    T visit(One node);
    // Unary
    T visit(BooleanVariable variable);
    T visit(Not node);
    // Binary
    T visit(And node);
    T visit(Or node);
    T visit(Xor node);
    T visit(Iff node);
    T visit(Imply node);
}
