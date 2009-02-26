package org.workcraft.framework.interop;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;

public class SynchronousExternalProcess {
	class SynchronousListener implements ExternalProcessListener {
		public void errorData(byte[] data) {
			synchronized (errorData) {
				errorData.add(data);
			}
		}

		public void outputData(byte[] data) {
			synchronized (outputData) {
				outputData.add(data);
			}
		}

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

	public boolean start (long timeout) throws IOException {
		errorData.clear();
		outputData.clear();
		finished = false;
		returnCode = -1;

		long endTime = System.currentTimeMillis() + timeout;

		process.start();

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

	private ByteBuffer mergeChunks (LinkedList<byte[]> chunks) {
		int len = 0;
		for (byte[] dataChunk : errorData)
			len += dataChunk.length;

		ByteBuffer ret = ByteBuffer.allocate(len);

		for (byte[] dataChunk : errorData)
			ret.put(dataChunk);

		return ret;
	}

	public ByteBuffer getOutputData() {
		return mergeChunks(outputData);

	}

	public ByteBuffer getErrorData() {
		return mergeChunks(errorData);
	}
}
