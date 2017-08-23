package org.workcraft.interop;

import java.util.UUID;

public final class WorkMathFormat implements Format {

    private static WorkMathFormat instance = null;

    private WorkMathFormat() {
    }

    public static WorkMathFormat getInstance() {
        if (instance == null) {
            instance = new WorkMathFormat();
        }
        return instance;
    }

    @Override
    public UUID getUuid() {
        return UUID.fromString("6ea20f69-c9c4-4888-9124-252fe4345309");
    }

    @Override
    public String getName() {
        return "WorkMathXML";
    }

    @Override
    public String getExtension() {
        return ".xml";
    }

    @Override
    public String getDescription() {
        return "Workcraft math model";
    }

}
