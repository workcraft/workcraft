package org.workcraft.workspace;

import org.workcraft.shared.DataAccumulator;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class Resource {

    public static final String FILE_ATTRIBUTE_SUFFIX = "file";

    private final String name;
    private final byte[] data;

    public Resource(String name, InputStream is) throws IOException {
        this.name = name;
        this.data = DataAccumulator.loadStream(is);
    }

    public Resource(String name, ByteArrayOutputStream os) {
        this.name = name;
        this.data = os.toByteArray();
    }

    public String getName() {
        return name;
    }

    public InputStream toStream() {
        return new ByteArrayInputStream(data);
    }

    public byte[] toByteArray() {
        return Arrays.copyOf(data, data.length);
    }
}
