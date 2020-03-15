package org.workcraft.plugins.cpog.sat;

public interface CnfGenerator<T> {
    CnfTask getCnf(T t);
}
