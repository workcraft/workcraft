package org.workcraft.types;

public interface Func<T, R> {
    R eval(T arg);
}
