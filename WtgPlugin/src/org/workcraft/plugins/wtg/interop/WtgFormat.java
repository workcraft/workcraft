package org.workcraft.plugins.wtg.interop;

import java.util.UUID;

import org.workcraft.interop.Format;

public final class WtgFormat implements Format {

    private static WtgFormat instance = null;

    private WtgFormat() {
    }

    public static WtgFormat getInstance() {
        if (instance == null) {
            instance = new WtgFormat();
        }
        return instance;
    }

    @Override
    public UUID getUuid() {
        return UUID.fromString("ff127612-f14d-4afd-90dc-8c74daa4083c");
    }

    @Override
    public String getName() {
        return "WTG";
    }

    @Override
    public String getExtension() {
        return ".wtg";
    }

    @Override
    public String getDescription() {
        return "Waveform Transition Graph";
    }

}
