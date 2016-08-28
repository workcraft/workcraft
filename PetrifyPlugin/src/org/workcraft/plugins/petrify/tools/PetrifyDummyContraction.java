package org.workcraft.plugins.petrify.tools;

import org.workcraft.ConversionTool;
import org.workcraft.Framework;
import org.workcraft.plugins.petrify.tasks.TransformationResultHandler;
import org.workcraft.plugins.petrify.tasks.TransformationTask;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.util.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class PetrifyDummyContraction extends ConversionTool {

    @Override
    public String getDisplayName() {
        return "Dummy contraction [Petrify]";
    }

    public Position getPosition() {
        return Position.TOP;
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, StgModel.class);
    }

    @Override
    public void run(WorkspaceEntry we) {
        final TransformationTask task = new TransformationTask(we, "Dummy contraction", new String[] {"-hide", ".dummy" });
        final Framework framework = Framework.getInstance();
        framework.getTaskManager().queue(task, "Petrify dummy contraction", new TransformationResultHandler(we));
    }
}
