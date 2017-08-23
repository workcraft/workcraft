package org.workcraft.plugins.stg.interop;

import java.util.UUID;

import org.workcraft.interop.Format;

public final class LpnFormat implements Format {

    private static LpnFormat instance = null;

    private LpnFormat() {
    }

    public static LpnFormat getInstance() {
        if (instance == null) {
            instance = new LpnFormat();
        }
        return instance;
    }

    @Override
    public UUID getUuid() {
        return UUID.fromString("3d3432eb-a993-430f-a47d-a1efb4280cc8");
    }

    @Override
    public String getName() {
        return "LPN";
    }

    @Override
    public String getExtension() {
        return ".lpn";
    }

    @Override
    public String getDescription() {
        return "Labeled Petri Net";
    }

}
