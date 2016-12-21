package org.workcraft.workspace;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class Memento {
    private final byte[] data;

    public Memento(byte[] data) {
        this.data = data;
    }

    public InputStream getStream() {
        return new ByteArrayInputStream(data);
    }
}
