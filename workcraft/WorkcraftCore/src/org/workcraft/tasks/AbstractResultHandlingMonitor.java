package org.workcraft.tasks;

public abstract class AbstractResultHandlingMonitor<T, U> extends BasicProgressMonitor<T> {

    private U handledResult = null;

    @Override
    public final void isFinished(Result<? extends T> result) {
        handledResult = handle(result);
        super.isFinished(result);
    }

    public final U waitForHandledResult() {
        waitResult();
        return handledResult;
    }

    public abstract U handle(Result<? extends T> result);

}
