package org.workcraft.workspace;

import org.workcraft.shared.DataAccumulator;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class RawData {

    private final byte[] data;

    public RawData(byte[] data) {
        this.data = data;
    }

    public RawData(InputStream is) throws IOException {
        this.data = DataAccumulator.loadStream(is);
    }

    public RawData(ByteArrayOutputStream os) {
        this.data = os.toByteArray();
    }

    public InputStream getStream() {
        return new ByteArrayInputStream(data);
    }

    public byte[] getData() {
        return Arrays.copyOf(data, data.length);
    }

}
