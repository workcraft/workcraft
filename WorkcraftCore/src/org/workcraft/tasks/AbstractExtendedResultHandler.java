package org.workcraft.tasks;

public abstract class AbstractExtendedResultHandler<T, R> extends BasicProgressMonitor<T> {
    private R handledResult = null;

    @Override
    public final void finished(Result<? extends T> result) {
        handledResult = handleResult(result);
        super.finished(result);
    }

    public final R waitForHandledResult() {
        waitResult();
        return handledResult;
    }

    public abstract R handleResult(Result<? extends T> result);

}
