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

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.LinkedList;


public class ExternalProcess {

    abstract static class StreamReaderThread extends Thread {
        private final ReadableByteChannel channel;
        private final ByteBuffer buffer = ByteBuffer.allocate(1024);

        public StreamReaderThread(ReadableByteChannel channel) {
            this.channel = channel;
        }

        abstract void handleData(byte [] data);

        @Override
        public void run() {
            while (true)
                try {
                    buffer.rewind();
                    int result = channel.read(buffer);
                    if (result == -1) {
                        return;
                    }
                    if (result == 0) {
                        continue;
                    }
                    buffer.rewind();
                    byte[] data = new byte[result];
                    buffer.get(data);
                    handleData(data);
                } catch (IOException e) {
//                    e.printStackTrace(); -- This exception is mostly caused by the process termination and spams the user with information about exceptions that should
                    //                        just be ignored, so removed printing. mech.
                    return;
                }
        }
    }

    class InputReaderThread extends StreamReaderThread {
        InputReaderThread() {
            super(inputStream);
        }
        @Override
        void handleData(byte[] data) {
            outputData(data);
        }
    }

    class ErrorReaderThread extends StreamReaderThread {
        ErrorReaderThread() {
            super(errorStream);
        }
        @Override
        void handleData(byte[] data) {
            errorData(data);
        }
    }

    class WaiterThread extends Thread {
        @Override
        public void run() {
            try {
                process.waitFor();
                processFinished();
            } catch (InterruptedException e) {
            }
        }
    }

    private final ProcessBuilder processBuilder;
    private Process process = null;
    private boolean finished = false;

    private ReadableByteChannel inputStream = null;
    private ReadableByteChannel errorStream = null;
    private WritableByteChannel outputStream = null;

    private LinkedList<ExternalProcessListener> listeners = new LinkedList<ExternalProcessListener>();

    public ExternalProcess(String[] command, String workingDirectoryPath) {
        processBuilder = new ProcessBuilder(command);
        processBuilder.directory(workingDirectoryPath == null? null : new File(workingDirectoryPath));
    }

    public ExternalProcess(String[] array, File workingDir) {
        this(array, (workingDir == null ? null : workingDir.getAbsolutePath()));
    }

    private void outputData(byte[] data) {
        for (ExternalProcessListener l : listeners) {
            l.outputData(data);
        }
    }

    private void errorData(byte[] data) {
        for (ExternalProcessListener l : listeners) {
            l.errorData(data);
        }
    }

    private void processFinished() {
        for (ExternalProcessListener l : listeners){
            l.processFinished(process.exitValue());
        }
        finished = true;
    }

    public boolean isRunning() {
        return process != null && !finished;
    }

    public void start() throws IOException {
        if (isRunning()) {
            return;
        }
        process = processBuilder.start();
        outputStream = Channels.newChannel(process.getOutputStream());
        errorStream = Channels.newChannel(process.getErrorStream());
        inputStream = Channels.newChannel(process.getInputStream());
        if (outputStream == null) {
            throw new RuntimeException("No output stream!");
        }
        new InputReaderThread().start();
        new ErrorReaderThread().start();
        new WaiterThread().start();
    }

    public void cancel() {
        if (isRunning()) {
            process.destroy();
        }
    }

    public void writeData(byte[] data) {
        try {
            outputStream.write(ByteBuffer.wrap(data));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addListener(ExternalProcessListener listener) {
        listeners.add(listener);
    }

    public void removeListener(ExternalProcessListener listener) {
        listeners.remove(listener);
    }

    public void closeInput() throws IOException {
        outputStream.close();
    }

}
