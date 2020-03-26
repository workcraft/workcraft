package org.workcraft.tasks;

public interface ResultHandler<T, R> {

    R handle(Result<? extends T> result);

}
