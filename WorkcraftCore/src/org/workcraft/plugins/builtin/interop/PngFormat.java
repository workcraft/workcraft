package org.workcraft.plugins.builtin.interop;

import java.util.UUID;

import org.workcraft.interop.Format;

public final class PngFormat implements Format {

    private static PngFormat instance = null;

    private PngFormat() {
    }

    public static PngFormat getInstance() {
        if (instance == null) {
            instance = new PngFormat();
        }
        return instance;
    }

    @Override
    public UUID getUuid() {
        return UUID.fromString("c09714a6-cae9-4744-95cb-17ba4d28f5ef");
    }

    @Override
    public String getName() {
        return "PNG";
    }

    @Override
    public String getExtension() {
        return ".png";
    }

    @Override
    public String getDescription() {
        return "Portable Network Graphics";
    }

}
