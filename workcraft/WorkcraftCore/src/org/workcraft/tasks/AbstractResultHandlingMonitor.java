package org.workcraft.tasks;

import org.workcraft.Framework;

public abstract class AbstractResultHandlingMonitor<T, U> extends BasicProgressMonitor<T> {

    private U handledResult = null;
    private boolean interactive = true;

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

    public void setInteractive(boolean value) {
        interactive = value;
    }

    public boolean isInteractive() {
        return interactive && Framework.getInstance().isInGuiMode();
    }

}
