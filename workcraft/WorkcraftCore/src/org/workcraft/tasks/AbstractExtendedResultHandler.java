package org.workcraft.tasks;

public abstract class AbstractExtendedResultHandler<T, R> extends BasicProgressMonitor<T> implements ResultHandler<T, R> {

    private R handledResult = null;

    @Override
    public final void isFinished(Result<? extends T> result) {
        handledResult = handle(result);
        super.isFinished(result);
    }

    public final R waitForHandledResult() {
        waitResult();
        return handledResult;
    }

}
