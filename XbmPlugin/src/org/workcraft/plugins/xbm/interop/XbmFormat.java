package org.workcraft.plugins.xbm.interop;

import org.workcraft.interop.Format;

import java.util.UUID;

public final class XbmFormat implements Format {

    private static XbmFormat instance = null;

    private XbmFormat() {
    }

    public static XbmFormat getInstance() {
        if (instance == null) {
            instance = new XbmFormat();
        }
        return instance;
    }

    @Override
    public UUID getUuid() {
        return UUID.fromString("6336ae86-8ef8-4f71-9649-0b80329a081a");
    }

    @Override
    public String getName() {
        return "XBM";
    }

    @Override
    public String getExtension() {
        return ".org.workcraft.plugins.xbm";
    }

    @Override
    public String getDescription() {
        return "eXtended Burst Mode";
    }

    @Override
    public String getKeyword() {
        return ".eXtended burst mode";
    }
}
