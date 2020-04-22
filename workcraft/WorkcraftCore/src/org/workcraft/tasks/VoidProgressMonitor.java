package org.workcraft.tasks;

public class VoidProgressMonitor implements ProgressMonitor<Void> {

    @Override
    public boolean isCancelRequested() {
        return false;
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
    public void isFinished(Result<? extends Void> result) {
    }

    @Override
    public final Result<? extends Void> waitResult() {
        return null;
    }

}
