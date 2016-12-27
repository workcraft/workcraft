package org.workcraft.formula.cnf;

import org.workcraft.formula.sat.CnfTask;

public interface RawCnfGenerator<T> {
    CnfTask getCnf(T t);
}
