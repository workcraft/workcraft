package org.workcraft.plugins.builtin.interop;

import org.workcraft.interop.Format;

import java.util.UUID;

public final class PsFormat implements Format {

    private static PsFormat instance = null;

    private PsFormat() {
    }

    public static PsFormat getInstance() {
        if (instance == null) {
            instance = new PsFormat();
        }
        return instance;
    }

    @Override
    public UUID getUuid() {
        return UUID.fromString("9b5bd9f0-b5cf-11df-8d81-0800200c9a66");
    }

    @Override
    public String getName() {
        return "PS";
    }

    @Override
    public String getExtension() {
        return ".ps";
    }

    @Override
    public String getDescription() {
        return "PostScript";
    }

}
