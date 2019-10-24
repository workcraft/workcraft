package org.workcraft.plugins.xbm.interop;

import org.workcraft.interop.Format;

import java.util.UUID;

public class NouncFormat implements Format {

    private static NouncFormat instance = null;

    public static NouncFormat getInstance() {
        if (instance == null) {
            instance = new NouncFormat();
        }
        return instance;
    }

    @Override
    public UUID getUuid() {
        return UUID.fromString("4d7r748e-ae71-a12a-bb28-982ca2ed212");
    }

    @Override
    public String getName() {
        return "NOUNC (3D)";
    }

    @Override
    public String getExtension() {
        return ".nounc";
    }

    @Override
    public String getDescription() {
        return "eXtended Burst Mode (3D)";
    }

    @Override
    public String getKeyword() {
        return ".extended burst mode";
    }
}
