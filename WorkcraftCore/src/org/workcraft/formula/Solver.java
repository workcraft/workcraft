package org.workcraft.formula;

public interface Solver<Res, Task> {
    Res solve(Task task);
}
