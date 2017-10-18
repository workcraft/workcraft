package org.workcraft.tasks;

import java.io.IOException;

import org.workcraft.util.DataAccumulator;

public class SubtaskMonitor<T> extends BasicProgressMonitor<T> {
    private final ProgressMonitor<?> parent;

    private final DataAccumulator stdoutAccum = new DataAccumulator();
    private final DataAccumulator stderrAccum = new DataAccumulator();

    public SubtaskMonitor(ProgressMonitor<?> parent) {
        this.parent = parent;
    }

    @Override
    public boolean isCancelRequested() {
        return parent.isCancelRequested();
    }

    @Override
    public void stderr(byte[] data) {
        try {
            stderrAccum.write(data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void stdout(byte[] data) {
        try {
            stdoutAccum.write(data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] getStderrData() {
        return stderrAccum.getData();
    }

    public byte[] getStdoutData() {
        return stdoutAccum.getData();
    }

}
