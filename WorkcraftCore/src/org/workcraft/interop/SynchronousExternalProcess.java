package org.workcraft.interop;

import java.io.IOException;
import java.util.LinkedList;

public class SynchronousExternalProcess {
    class SynchronousListener implements ExternalProcessListener {
        @Override
        public void errorData(byte[] data) {
            synchronized (errorData) {
                errorData.add(data);
            }
        }
        @Override
        public void outputData(byte[] data) {
            synchronized (outputData) {
                outputData.add(data);
            }
        }
        @Override
        public void processFinished(int returnCode) {
            SynchronousExternalProcess.this.returnCode = returnCode;
            finished = true;
        }
    }

    private int returnCode;
    private boolean finished = false;
    private final ExternalProcess process;

    private final LinkedList<byte[]> errorData = new LinkedList<>();
    private final LinkedList<byte[]> outputData = new LinkedList<>();

    public SynchronousExternalProcess(String[] command, String workingDirectory) {
        process = new ExternalProcess(command, workingDirectory);
        process.addListener(new SynchronousListener());
    }

    public boolean start(long timeout) throws IOException {
        return start(timeout, new byte[]{});
    }

    public boolean start(long timeout, byte[] input) throws IOException {
        errorData.clear();
        outputData.clear();
        finished = false;
        returnCode = -1;

        long endTime = System.currentTimeMillis() + timeout;

        process.start();
        process.writeData(input);
        process.closeInput();

        while (System.currentTimeMillis() < endTime) {
            if (finished) {
                return true;
            }
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                return false;
            }
        }

        process.cancel();
        return false;
    }

    public int getReturnCode() {
        return returnCode;
    }

    private static byte[] mergeChunksToArray(LinkedList<byte[]> chunks) {
        int len = 0;
        for (byte[] dataChunk : chunks) {
            len += dataChunk.length;
        }
        byte[] result = new byte[len];
        int cur = 0;
        for (byte[] dataChunk : chunks) {
            for (int i = 0; i < dataChunk.length; i++) {
                result[cur++] = dataChunk[i];
            }
        }
        return result;
    }

    public byte[] getOutputData() {
        return mergeChunksToArray(outputData);
    }

    public byte[] getErrorData() {
        return mergeChunksToArray(errorData);
    }

}
