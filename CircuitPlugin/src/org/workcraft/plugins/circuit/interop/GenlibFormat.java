package org.workcraft.plugins.circuit.interop;

import java.util.UUID;

import org.workcraft.interop.Format;

public final class GenlibFormat implements Format {

    private static GenlibFormat instance = null;

    private GenlibFormat() {
    }

    public static GenlibFormat getInstance() {
        if (instance == null) {
            instance = new GenlibFormat();
        }
        return instance;
    }

    @Override
    public UUID getUuid() {
        return UUID.fromString("27490a48-b3c1-4d18-b165-a7d4cb6eeb6b");
    }

    @Override
    public String getName() {
        return "Genlib";
    }

    @Override
    public String getExtension() {
        return ".lib";
    }

    @Override
    public String getDescription() {
        return "SIS Genlib";
    }

}
