package org.workcraft.plugins.interop;

import java.util.UUID;

import org.workcraft.interop.Format;

public final class SvgFormat implements Format {

    private static SvgFormat instance = null;

    private SvgFormat() {
    }

    public static SvgFormat getInstance() {
        if (instance == null) {
            instance = new SvgFormat();
        }
        return instance;
    }

    @Override
    public UUID getUuid() {
        return UUID.fromString("99439c3c-753b-46e3-a5d5-6a0993305a2c");
    }

    @Override
    public String getName() {
        return "SVG";
    }

    @Override
    public String getExtension() {
        return ".svg";
    }

    @Override
    public String getDescription() {
        return "Scalable Vector Graphics";
    }

}
