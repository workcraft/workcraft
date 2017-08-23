package org.workcraft.interop;

import java.util.UUID;

public final class WorkVisualFormat implements Format {

    private static WorkVisualFormat instance = null;

    private WorkVisualFormat() {
    }

    public static WorkVisualFormat getInstance() {
        if (instance == null) {
            instance = new WorkVisualFormat();
        }
        return instance;
    }

    @Override
    public UUID getUuid() {
        return UUID.fromString("2fa9669c-a1bf-4be4-8622-007635d672e5");
    }

    @Override
    public String getName() {
        return "WorkVisualXML";
    }

    @Override
    public String getExtension() {
        return ".xml";
    }

    @Override
    public String getDescription() {
        return "Workcraft visual model";
    }

}
