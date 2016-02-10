/*
*
* Copyright 2008,2009 Newcastle University
*
* This file is part of Workcraft.
*
* Workcraft is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* Workcraft is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with Workcraft.  If not, see <http://www.gnu.org/licenses/>.
*
*/

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
    private ExternalProcess process;

    private LinkedList<byte[]> errorData = new LinkedList<byte[]>();
    private LinkedList<byte[]> outputData = new LinkedList<byte[]>();

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
            if (finished)
                return true;
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
            for(int i=0;i<dataChunk.length;i++) {
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
