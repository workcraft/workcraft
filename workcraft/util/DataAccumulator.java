package org.workcraft.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;

public class DataAccumulator extends OutputStream {
	private LinkedList<byte[]> dataChunks = new LinkedList<byte[]>();

	public static byte[] loadStream (InputStream is) throws IOException {
		DataAccumulator accum = new DataAccumulator();

		int available;
		while ( (available=is.available()) > 0) {
			byte[] chunk = new byte[available];
			is.read(chunk, 0, available);
			accum.write(chunk);
		}

		return accum.getData();
	}

	public static ByteArrayInputStream bufferStream(InputStream is) throws IOException {
		return new ByteArrayInputStream(loadStream(is));
	}

	private void addDataChunk (byte[] data) {
		dataChunks.add(data);
	}

	public byte [] getData () {
		int len = 0;
		for (byte[] dataChunk : dataChunks)
			len += dataChunk.length;

		byte [] result = new byte[len];

		int cur = 0;
		for (byte[] dataChunk : dataChunks)
			for(int i=0;i<dataChunk.length;i++)
				result[cur++] = dataChunk[i];

		return result;
	}

	public InputStream getInputStream() {
		return new ByteArrayInputStream(getData());
	}

	public void write(int b) throws IOException {
		addDataChunk(new byte[] { (byte)b} );
	}

	public void write(byte[] b, int off, int len) throws IOException {
		byte[] chunk = new byte[len];
		System.arraycopy(b, off, chunk, 0, len);
		addDataChunk(chunk);
	}

	public void write(byte[] b) throws IOException {
		addDataChunk(b.clone());
	}
}