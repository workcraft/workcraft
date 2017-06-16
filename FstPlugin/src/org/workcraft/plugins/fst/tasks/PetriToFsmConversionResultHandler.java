package org.workcraft.plugins.fst.tasks;

import org.workcraft.Framework;
import org.workcraft.dom.math.MathModel;
import org.workcraft.gui.ExceptionDialog;
import org.workcraft.gui.workspace.Path;
import org.workcraft.plugins.fsm.Fsm;
import org.workcraft.plugins.fsm.FsmDescriptor;
import org.workcraft.plugins.fsm.VisualFsm;
import org.workcraft.plugins.fst.VisualFst;
import org.workcraft.plugins.fst.converters.FstToFsmConverter;
import org.workcraft.plugins.shared.tasks.ExternalProcessResult;
import org.workcraft.tasks.DummyProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.util.MessageUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

public class PetriToFsmConversionResultHandler extends DummyProgressMonitor<WriteSgConversionResult> {
    private final WriteSgConversionTask task;
    private WorkspaceEntry result;

    public PetriToFsmConversionResultHandler(final WriteSgConversionTask task) {
        this.task = task;
        this.result = null;
    }

    @Override
    public void finished(final Result<? extends WriteSgConversionResult> result, final String description) {
        if (result.getOutcome() == Outcome.FINISHED) {
            final VisualFst fst = new VisualFst(result.getReturnValue().getConversionResult());
            final VisualFsm fsm = new VisualFsm(new Fsm());
            final FstToFsmConverter converter = new FstToFsmConverter(fst, fsm);
            final MathModel model = converter.getDstModel().getMathModel();
            final ModelEntry me = new ModelEntry(new FsmDescriptor(), model);
            final Path<String> path = task.getWorkspaceEntry().getWorkspacePath();
            final Framework framework = Framework.getInstance();
            this.result = framework.createWork(me, path);
        } else if (result.getOutcome() != Outcome.CANCELLED) {
            if (result.getCause() == null) {
                final Result<? extends ExternalProcessResult> writeSgResult = result.getReturnValue().getResult();
                MessageUtils.showWarning("Petrify output:\n" + writeSgResult.getReturnValue().getErrorsHeadAndTail());
            } else {
                ExceptionDialog.show(result.getCause());
            }
        }
    }

    public WorkspaceEntry getResult() {
        return result;
    }

}
