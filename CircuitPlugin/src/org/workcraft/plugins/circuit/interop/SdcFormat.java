package org.workcraft.plugins.circuit.interop;

import java.util.UUID;

import org.workcraft.interop.Format;

public final class SdcFormat implements Format {

    private static SdcFormat instance = null;

    private SdcFormat() {
    }

    public static SdcFormat getInstance() {
        if (instance == null) {
            instance = new SdcFormat();
        }
        return instance;
    }

    @Override
    public UUID getUuid() {
        return UUID.fromString("fd92a9c6-e13a-4785-83ff-1fb6f666b8ed");
    }

    @Override
    public String getName() {
        return "SDC";
    }

    @Override
    public String getExtension() {
        return ".sdc";
    }

    @Override
    public String getDescription() {
        return "Synopsys Design Constraints";
    }

}
