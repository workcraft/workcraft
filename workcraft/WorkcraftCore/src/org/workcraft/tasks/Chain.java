package org.workcraft.tasks;

import java.util.function.Function;
import java.util.function.Supplier;

public class Chain<T> {

    private Function<T, Result<? extends T>> func;
    private final ProgressMonitor<? super T> monitor;

    public Chain(Supplier<Result<? extends T>> init, ProgressMonitor<? super T> monitor) {
        this.func = payload -> init.get();
        this.monitor = monitor;
    }

    public void andOnSuccess(Function<T, Result<? extends T>> after, double progress) {
        andOnSuccess(after);
        if (monitor != null) {
            andThen(() -> monitor.progressUpdate(progress));
        }
    }

    public void andOnSuccess(Function<T, Result<? extends T>> after) {
        func = func.andThen(result -> result.isSuccess() ? after.apply(result.getPayload()) : result);
    }

    public void andThen(Runnable after) {
        func = func.andThen(result -> {
            after.run();
            return result;
        });
    }

    public Result<? extends T> process() {
        return func.apply(null);
    }

}
