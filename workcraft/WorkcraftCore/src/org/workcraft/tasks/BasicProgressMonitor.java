package org.workcraft.tasks;

public class BasicProgressMonitor<T> implements ProgressMonitor<T> {
    private boolean finished = false;
    private Result<? extends T> result = null;

    @Override
    public boolean isCancelRequested() {
        return false;
    }

    @Override
    public void setDetails(String details) {
    }

    @Override
    public void progressUpdate(double completion) {
    }

    @Override
    public void stdout(byte[] data) {
    }

    @Override
    public void stderr(byte[] data) {
    }

    @Override
    public void isFinished(Result<? extends T> result) {
        this.result = result;
        finished = true;
    }

    @Override
    public final Result<? extends T> waitResult() {
        while (!finished) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                return null;
            }
        }
        return result;
    }

}
