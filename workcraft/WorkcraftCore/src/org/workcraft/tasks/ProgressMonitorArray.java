package org.workcraft.tasks;

import java.util.ArrayList;

public class ProgressMonitorArray<T> extends ArrayList<ProgressMonitor<? super T>> implements ProgressMonitor<T> {

    private static final long serialVersionUID = 1L;
    private boolean finished = false;
    private Result<? extends T> result = null;

    @Override
    public boolean isCancelRequested() {
        boolean requested = false;
        for (ProgressMonitor<? super T> o : this) {
            requested |= o.isCancelRequested();
        }
        return requested;
    }

    @Override
    public void stdout(byte[] data) {
        for (ProgressMonitor<? super T> o : this) {
            o.stdout(data);
        }
    }

    @Override
    public void setDetails(String details) {
        for (ProgressMonitor<? super T> o : this) {
            o.setDetails(details);
        }
    }

    @Override
    public void progressUpdate(double completion) {
        for (ProgressMonitor<? super T> o : this) {
            o.progressUpdate(completion);
        }
    }

    @Override
    public void stderr(byte[] data) {
        for (ProgressMonitor<? super T> o : this) {
            o.stderr(data);
        }
    }

    @Override
    public void isFinished(Result<? extends T> result) {
        for (ProgressMonitor<? super T> o : this) {
            o.isFinished(result);
        }
        this.result = result;
        finished = true;
    }

    @Override
    public Result<? extends T> waitResult() {
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
