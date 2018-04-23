package org.workcraft.plugins.stg.interop;

import java.util.UUID;

import org.workcraft.interop.Format;

public final class StgFormat implements Format {

    private static StgFormat instance = null;

    private StgFormat() {
    }

    public static StgFormat getInstance() {
        if (instance == null) {
            instance = new StgFormat();
        }
        return instance;
    }

    @Override
    public UUID getUuid() {
        return UUID.fromString("000199d9-4ac1-4423-b8ea-9017d838e45b");
    }

    @Override
    public String getName() {
        return "STG";
    }

    @Override
    public String getExtension() {
        return ".g";
    }

    @Override
    public String getDescription() {
        return "Signal Transition Graph";
    }

    @Override
    public String getKeyword() {
        return ".graph";
    }

}
