package org.workcraft.formula;

public interface Generator<Res, Task> {
    Res generate(Task task);
}
