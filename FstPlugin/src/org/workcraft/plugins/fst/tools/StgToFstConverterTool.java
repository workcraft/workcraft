package org.workcraft.plugins.fst.tools;

import org.workcraft.ConversionTool;
import org.workcraft.Framework;
import org.workcraft.plugins.fst.task.StgToFstConversionResultHandler;
import org.workcraft.plugins.fst.task.WriteSgConversionTask;
import org.workcraft.plugins.stg.STG;
import org.workcraft.util.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class StgToFstConverterTool extends ConversionTool {
    private final boolean binary;

    public StgToFstConverterTool(boolean binary) {
        this.binary = binary;
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.canHas(we, STG.class);
    }

    @Override
    public String getDisplayName() {
        return (binary ? "Finate State Transducer (binary-encoded) [Petrify]" : "Finate State Transducer (basic) [Petrify]");
    }

    @Override
    public void run(WorkspaceEntry we) {
        WriteSgConversionTask task = new WriteSgConversionTask(we, binary);
        final Framework framework = Framework.getInstance();
        framework.getTaskManager().queue(task, "Building state graph", new StgToFstConversionResultHandler(task));
    }

}
