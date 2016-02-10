package org.workcraft.util;

public interface Func <Arg,Result> {
    Result eval(Arg arg);
}
