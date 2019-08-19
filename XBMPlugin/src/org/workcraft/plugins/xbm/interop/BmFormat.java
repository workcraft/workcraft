package org.workcraft.plugins.xbm.interop;

import org.workcraft.interop.Format;

import java.util.UUID;

public class BmFormat implements Format {

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
        return "BM";
    }

    @Override
    public String getExtension() {
        return ".bm";
    }

    @Override
    public String getDescription() {
        return "Burst Mode";
    }

    @Override
    public String getKeyword() {
        return ".burst mode";
    }
}
