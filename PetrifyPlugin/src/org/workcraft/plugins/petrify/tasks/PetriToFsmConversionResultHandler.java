package org.workcraft.plugins.petrify.tasks;

import org.workcraft.Framework;
import org.workcraft.dom.math.MathModel;
import org.workcraft.gui.ExceptionDialog;
import org.workcraft.gui.workspace.Path;
import org.workcraft.plugins.fsm.Fsm;
import org.workcraft.plugins.fsm.FsmDescriptor;
import org.workcraft.plugins.fsm.VisualFsm;
import org.workcraft.plugins.fst.VisualFst;
import org.workcraft.plugins.fst.converters.FstToFsmConverter;
import org.workcraft.tasks.AbstractExtendedResultHandler;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.util.DialogUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

public class PetriToFsmConversionResultHandler extends AbstractExtendedResultHandler<WriteSgConversionOutput, WorkspaceEntry> {
    private final WriteSgConversionTask task;

    public PetriToFsmConversionResultHandler(final WriteSgConversionTask task) {
        this.task = task;
    }

    @Override
    public WorkspaceEntry handleResult(final Result<? extends WriteSgConversionOutput> result) {
        WorkspaceEntry weResult = null;
        WriteSgConversionOutput output = result.getPayload();
        if (result.getOutcome() == Outcome.SUCCESS) {
            final VisualFst fst = new VisualFst(output.getFst());
            final VisualFsm fsm = new VisualFsm(new Fsm());
            final FstToFsmConverter converter = new FstToFsmConverter(fst, fsm);
            final MathModel model = converter.getDstModel().getMathModel();
            final ModelEntry me = new ModelEntry(new FsmDescriptor(), model);
            final Path<String> path = task.getWorkspaceEntry().getWorkspacePath();
            final Framework framework = Framework.getInstance();
            weResult = framework.createWork(me, path);
        } else if (result.getOutcome() == Outcome.FAILURE) {
            if (result.getCause() == null) {
                DialogUtils.showWarning("Petrify output:\n" + output.getErrorsHeadAndTail());
            } else {
                ExceptionDialog.show(result.getCause());
            }
        }
        return weResult;
    }

}
