package org.workcraft.formula.sat;

public interface CnfGenerator<T> {
    CnfTask getCnf(T t);
}
