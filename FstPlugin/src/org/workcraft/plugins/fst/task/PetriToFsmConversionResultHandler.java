package org.workcraft.plugins.fst.task;

import java.io.File;

import javax.swing.JOptionPane;

import org.workcraft.Framework;
import org.workcraft.dom.math.MathModel;
import org.workcraft.gui.ExceptionDialog;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.workspace.Path;
import org.workcraft.plugins.fsm.Fsm;
import org.workcraft.plugins.fsm.FsmDescriptor;
import org.workcraft.plugins.fsm.VisualFsm;
import org.workcraft.plugins.fst.VisualFst;
import org.workcraft.plugins.fst.converters.FstToFsmConverter;
import org.workcraft.plugins.shared.CommonEditorSettings;
import org.workcraft.plugins.shared.tasks.ExternalProcessResult;
import org.workcraft.tasks.DummyProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.util.FileUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.Workspace;
import org.workcraft.workspace.WorkspaceEntry;

public class PetriToFsmConversionResultHandler extends DummyProgressMonitor<WriteSgConversionResult> {
    private final WriteSgConversionTask task;
    private WorkspaceEntry result;

    public PetriToFsmConversionResultHandler(WriteSgConversionTask task) {
        this.task = task;
        this.result = null;
    }

    @Override
    public void finished(final Result<? extends WriteSgConversionResult> result, String description) {
        final Framework framework = Framework.getInstance();
        WorkspaceEntry we = task.getWorkspaceEntry();
        Path<String> path = we.getWorkspacePath();
        if (result.getOutcome() == Outcome.FINISHED) {
            final VisualFst fst = new VisualFst(result.getReturnValue().getConversionResult());
            final VisualFsm fsm = new VisualFsm(new Fsm());
            final FstToFsmConverter converter = new FstToFsmConverter(fst, fsm);

            MathModel model = converter.getDstModel().getMathModel();
            final Workspace workspace = framework.getWorkspace();
            final Path<String> directory = path.getParent();
            final String name = FileUtils.getFileNameWithoutExtension(new File(path.getNode()));
            final ModelEntry me = new ModelEntry(new FsmDescriptor(), model);
            boolean openInEditor = me.isVisual() || CommonEditorSettings.getOpenNonvisual();
            this.result = workspace.addWork(directory, name, me, true, openInEditor);
        } else if (result.getOutcome() != Outcome.CANCELLED) {
            MainWindow mainWindow = framework.getMainWindow();
            if (result.getCause() == null) {
                Result<? extends ExternalProcessResult> writeSgResult = result.getReturnValue().getResult();
                JOptionPane.showMessageDialog(mainWindow,
                        "Petrify output: \n\n" + new String(writeSgResult.getReturnValue().getErrors()),
                        "Conversion failed", JOptionPane.WARNING_MESSAGE);
            } else {
                ExceptionDialog.show(mainWindow, result.getCause());
            }
        }
    }

    public WorkspaceEntry getResult() {
        return result;
    }

}
