package org.workcraft.plugins.xbm.interop;

import org.workcraft.interop.Format;

import java.util.UUID;

public final class BmFormat implements Format {

    private static BmFormat instance = null;

    private BmFormat() {
    }

    public static BmFormat getInstance() {
        if (instance == null) {
            instance = new BmFormat();
        }
        return instance;
    }

    @Override
    public UUID getUuid() {
        return UUID.fromString("c758294e-da89-1345-be56-8e8c184f1833");
    }

    @Override
    public String getName() {
        return "BM (Minimalist)";
    }

    @Override
    public String getExtension() {
        return ".bms";
    }

    @Override
    public String getDescription() {
        return "Burst Mode (Minimalist)";
    }
}
