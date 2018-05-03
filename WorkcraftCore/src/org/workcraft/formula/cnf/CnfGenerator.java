package org.workcraft.formula.cnf;

public interface CnfGenerator<T> {
    CnfTask getCnf(T t);
}
