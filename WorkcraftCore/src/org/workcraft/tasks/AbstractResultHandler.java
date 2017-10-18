package org.workcraft.tasks;

public abstract class AbstractResultHandler<T> extends BasicProgressMonitor<T> {

    @Override
    public final void finished(Result<? extends T> result) {
        handleResult(result);
        super.finished(result);
    }

    public abstract void handleResult(Result<? extends T> result);

}
