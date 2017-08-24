package org.workcraft.plugins.interop;

import java.util.UUID;

import org.workcraft.interop.Format;

public final class DotFormat implements Format {

    private static DotFormat instance = null;

    private DotFormat() {
    }

    public static DotFormat getInstance() {
        if (instance == null) {
            instance = new DotFormat();
        }
        return instance;
    }

    @Override
    public UUID getUuid() {
        return UUID.fromString("f1596b60-e294-11de-8a39-0800200c9a66");
    }

    @Override
    public String getName() {
        return "DOT";
    }

    @Override
    public String getExtension() {
        return ".dot";
    }

    @Override
    public String getDescription() {
        return "Graphviz DOT";
    }

}
