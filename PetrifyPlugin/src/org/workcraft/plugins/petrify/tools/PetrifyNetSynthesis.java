package org.workcraft.plugins.petrify.tools;

import java.util.ArrayList;

import org.workcraft.ConversionTool;
import org.workcraft.Framework;
import org.workcraft.plugins.fsm.Fsm;
import org.workcraft.plugins.fst.Fst;
import org.workcraft.plugins.petri.PetriNetModel;
import org.workcraft.plugins.petrify.tasks.TransformationResultHandler;
import org.workcraft.plugins.petrify.tasks.TransformationTask;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.tasks.TaskManager;
import org.workcraft.util.WorkspaceUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

public class PetrifyNetSynthesis extends ConversionTool {

    @Override
    public String getDisplayName() {
        return "Net synthesis [Petrify]";
    }

    @Override
    public Position getPosition() {
        return Position.MIDDLE;
    }

    @Override
    public boolean isApplicableTo(ModelEntry me) {
        return WorkspaceUtils.isApplicable(me, PetriNetModel.class) || WorkspaceUtils.isApplicable(me, Fsm.class);
    }

    @Override
    public ModelEntry run(ModelEntry me) {
        return null; // !!!
    }

    @Override
    public WorkspaceEntry run(WorkspaceEntry we) {
        ArrayList<String> args = getArgs();
        final TransformationTask task = new TransformationTask(we, "Net synthesis", args.toArray(new String[args.size()]));

        ModelEntry me = we.getModelEntry();
        boolean hasSignals = WorkspaceUtils.isApplicable(me, StgModel.class)
                || WorkspaceUtils.isApplicable(me, Fst.class);

        final Framework framework = Framework.getInstance();
        final TaskManager taskManager = framework.getTaskManager();
        final TransformationResultHandler monitor = new TransformationResultHandler(we, hasSignals);
        taskManager.queue(task, "Petrify net synthesis", monitor);
        return we;
    }

    @Override
    public ModelEntry convert(ModelEntry me) {
        return null; // !!!
    }

    public ArrayList<String> getArgs() {
        return new ArrayList<>();
    }

}
