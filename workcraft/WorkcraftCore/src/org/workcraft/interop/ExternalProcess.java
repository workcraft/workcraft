package org.workcraft.interop;

import org.workcraft.utils.LogUtils;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class ExternalProcess {

    abstract static class StreamReaderThread extends Thread {
        private final ReadableByteChannel channel;
        // Buffer is increased to 1MiB to reduce the number of updates for external processes with heavy output.
        private final ByteBuffer buffer = ByteBuffer.allocate(1024 * 1024);

        StreamReaderThread(ReadableByteChannel channel) {
            this.channel = channel;
        }

        public abstract void handleData(byte[] data);

        @Override
        public void run() {
            while (true) {
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
                    // This exception is mostly caused by the process termination and spams the user with
                    // information about exceptions that should just be ignored, so removed printing. mech.
                    return;
                }
            }
        }
    }

    class InputReaderThread extends StreamReaderThread {
        InputReaderThread() {
            super(inputStream);
        }
        @Override
        public void handleData(byte[] data) {
            outputData(data);
        }
    }

    class ErrorReaderThread extends StreamReaderThread {
        ErrorReaderThread() {
            super(errorStream);
        }
        @Override
        public void handleData(byte[] data) {
            errorData(data);
        }
    }

    class WaiterThread extends Thread {
        private final InputReaderThread inputReaderThread;
        private final ErrorReaderThread errorReaderThread;

        WaiterThread(InputReaderThread inputReaderThread, ErrorReaderThread errorReaderThread) {
            this.inputReaderThread = inputReaderThread;
            this.errorReaderThread = errorReaderThread;
        }

        @Override
        public void run() {
            try {
                process.waitFor();
                inputReaderThread.join();
                errorReaderThread.join();
                processFinished();
            } catch (InterruptedException ignored) {
            }
        }
    }

    private final ProcessBuilder processBuilder;
    private Process process = null;
    private boolean finished = false;

    private ReadableByteChannel inputStream = null;
    private ReadableByteChannel errorStream = null;
    private WritableByteChannel outputStream = null;

    private final LinkedList<ExternalProcessListener> listeners = new LinkedList<>();

    public ExternalProcess(String[] command) {
        this(command, null);
    }

    public ExternalProcess(String[] command, File directory) {
        processBuilder = new ProcessBuilder(command);
        processBuilder.directory(directory);
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
        for (ExternalProcessListener l : listeners) {
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
        InputReaderThread inputReaderThread = new InputReaderThread();
        ErrorReaderThread errorReaderThread = new ErrorReaderThread();
        WaiterThread waiterTread = new WaiterThread(inputReaderThread, errorReaderThread);
        inputReaderThread.start();
        errorReaderThread.start();
        waiterTread.start();
    }

    public void cancel() {
        if (isRunning()) {
            process.destroyForcibly();
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

    public static void printCommandLine(String[] args) {
        printCommandLine(Arrays.asList(args));
    }

    public static void printCommandLine(List<String> args) {
        LogUtils.logInfo("Running external command: " + String.join(" ", args));
    }

}
