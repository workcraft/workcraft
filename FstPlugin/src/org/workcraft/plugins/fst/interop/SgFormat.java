package org.workcraft.plugins.fst.interop;

import java.util.UUID;

import org.workcraft.interop.Format;

public final class SgFormat implements Format {

    private static SgFormat instance = null;

    private SgFormat() {
    }

    public static SgFormat getInstance() {
        if (instance == null) {
            instance = new SgFormat();
        }
        return instance;
    }

    @Override
    public UUID getUuid() {
        return UUID.fromString("f309012a-ab89-4036-bb80-8b1a161e8899");
    }

    @Override
    public String getName() {
        return "SG";
    }

    @Override
    public String getExtension() {
        return ".sg";
    }

    @Override
    public String getDescription() {
        return "State Graph";
    }

}
