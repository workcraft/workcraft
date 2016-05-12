package org.workcraft.plugins.fst.tools;

import org.workcraft.ConversionTool;
import org.workcraft.Framework;
import org.workcraft.plugins.fst.task.StgToFstConversionResultHandler;
import org.workcraft.plugins.fst.task.WriteSgConversionTask;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.util.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class StgToFstConverterTool extends ConversionTool {

    public boolean isBinary() {
        return false;
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.canHas(we, Stg.class);
    }

    @Override
    public String getDisplayName() {
        return isBinary() ? "Finate State Transducer (binary-encoded) [Petrify]" : "Finate State Transducer (basic) [Petrify]";
    }

    @Override
    public void run(WorkspaceEntry we) {
        WriteSgConversionTask task = new WriteSgConversionTask(we, isBinary());
        final Framework framework = Framework.getInstance();
        framework.getTaskManager().queue(task, "Building state graph", new StgToFstConversionResultHandler(task));
    }

}
