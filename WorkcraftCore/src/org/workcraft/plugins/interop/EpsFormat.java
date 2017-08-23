package org.workcraft.plugins.interop;

import java.util.UUID;

import org.workcraft.interop.Format;

public final class EpsFormat implements Format {

    private static EpsFormat instance = null;

    private EpsFormat() {
    }

    public static EpsFormat getInstance() {
        if (instance == null) {
            instance = new EpsFormat();
        }
        return instance;
    }

    @Override
    public UUID getUuid() {
        return UUID.fromString("c6158d84-e242-4f8c-9ec9-3a6cf045b769");
    }

    @Override
    public String getName() {
        return "EPS";
    }

    @Override
    public String getExtension() {
        return ".eps";
    }

    @Override
    public String getDescription() {
        return "Encapsulated PostScript";
    }

}
