package org.workcraft.tasks;

public abstract class AbstractResultHandler<T> extends BasicProgressMonitor<T> {

    @Override
    public final void isFinished(Result<? extends T> result) {
        handleResult(result);
        super.isFinished(result);
    }

    public abstract void handleResult(Result<? extends T> result);

}
