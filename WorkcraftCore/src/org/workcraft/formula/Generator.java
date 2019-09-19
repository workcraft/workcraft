package org.workcraft.formula;

public interface Generator<R, T> {
    R generate(T task);
}
