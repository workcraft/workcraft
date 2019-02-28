package org.workcraft.types;

public interface Func<Arg, Result> {
    Result eval(Arg arg);
}
