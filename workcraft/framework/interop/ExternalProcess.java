package org.workcraft.framework.interop;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.LinkedList;


public class ExternalProcess {
	class InputReaderThread extends Thread {
		private ByteBuffer buffer = ByteBuffer.allocate(1024);

		public void run() {
			while (true)
				try {
					buffer.rewind();
					int result = inputStream.read(buffer);

					if (result == -1) {
						System.out.println ("End of output stream.");
						return;
					}
					if (result == 0)
						continue;


					buffer.rewind();
					byte[] data = new byte[result];
					buffer.get(data);

					outputData(data);
				} catch (IOException e) {
					e.printStackTrace();
					return;
				}
		}
	}

	class ErrorReaderThread extends Thread {
		private ByteBuffer buffer = ByteBuffer.allocate(1024);

		public void run() {
			while (true)
				try {
					buffer.rewind();
					int result = errorStream.read(buffer);

					if (result == -1)
						return;
					if (result == 0)
						continue;

					buffer.rewind();
					byte[] data = new byte[result];
					buffer.get(data);

					errorData(data);

				} catch (IOException e) {
					e.printStackTrace();
					return;
				}
		}
	}

	class WaiterThread extends Thread {
		public void run() {
			try {
				process.waitFor();
				processFinished();
			} catch (InterruptedException e) {
			}
		}
	}

	private ProcessBuilder processBuilder;
	private Process process = null;

	private ReadableByteChannel inputStream = null;
	private ReadableByteChannel errorStream = null;
	private WritableByteChannel outputStream = null;

	private LinkedList<ExternalProcessListener> listeners = new LinkedList<ExternalProcessListener>();

	public ExternalProcess (String[] command, String workingDirectory) {
		processBuilder = new ProcessBuilder(command);
		processBuilder.directory(workingDirectory == null? null : new File(workingDirectory));
	}


	private void outputData(byte[] data) {
		for (ExternalProcessListener l : listeners)
			l.outputData(data);
	}

	private void errorData(byte[] data) {
		for (ExternalProcessListener l : listeners)
			l.errorData(data);
	}

	private void processFinished() {
		for (ExternalProcessListener l : listeners){
				l.processFinished(process.exitValue());
		}

		process = null;
		inputStream = null;
		errorStream = null;
		outputStream = null;
	}

	public boolean isRunning() {
		return (process != null);
	}

	public void start() throws IOException {
		if (isRunning())
			return;

		process = processBuilder.start();

		outputStream = Channels.newChannel(process.getOutputStream());
		errorStream = Channels.newChannel(process.getErrorStream());
		inputStream = Channels.newChannel(process.getInputStream());


		new InputReaderThread().start();
		new ErrorReaderThread().start();
		new WaiterThread().start();
	}

	public void cancel() {
		if (isRunning())
			process.destroy();
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
}
