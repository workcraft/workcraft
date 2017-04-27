package org.workcraft.plugins.fst.tasks;

import java.awt.Color;
import java.util.HashMap;

import javax.swing.JOptionPane;

import org.workcraft.Framework;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.gui.ExceptionDialog;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.workspace.Path;
import org.workcraft.plugins.fsm.VisualState;
import org.workcraft.plugins.fst.Fst;
import org.workcraft.plugins.fst.FstDescriptor;
import org.workcraft.plugins.fst.VisualFst;
import org.workcraft.plugins.shared.tasks.ExternalProcessResult;
import org.workcraft.tasks.DummyProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.util.ColorGenerator;
import org.workcraft.util.ColorUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

public class StgToFstConversionResultHandler extends DummyProgressMonitor<WriteSgConversionResult> {

    private final ColorGenerator colorGenerator = new ColorGenerator(ColorUtils.getHsbPalette(
            new float[]{0.45f, 0.15f, 0.70f, 0.25f, 0.05f, 0.80f, 0.55f, 0.20f, 075f, 0.50f},
            new float[]{0.30f}, new float[]{0.9f, 0.7f, 0.5f}));

    private final WriteSgConversionTask task;
    private WorkspaceEntry result;

    public StgToFstConversionResultHandler(final WriteSgConversionTask task) {
        this.task = task;
        this.result = null;
    }

    @Override
    public void finished(final Result<? extends WriteSgConversionResult> result, final String description) {
        final Framework framework = Framework.getInstance();
        if (result.getOutcome() == Outcome.FINISHED) {
            final Fst model = result.getReturnValue().getConversionResult();
            final ModelEntry me = new ModelEntry(new FstDescriptor(), model);
            final Path<String> path = task.getWorkspaceEntry().getWorkspacePath();
            this.result = framework.createWork(me, path);
            final VisualModel visualModel = me.getVisualModel();
            if (visualModel instanceof VisualFst) {
                highlightCscConflicts((VisualFst) visualModel);
            }
        } else if (result.getOutcome() != Outcome.CANCELLED) {
            final MainWindow mainWindow = framework.getMainWindow();
            if (result.getCause() == null) {
                final Result<? extends ExternalProcessResult> petrifyResult = result.getReturnValue().getResult();
                JOptionPane.showMessageDialog(mainWindow,
                        "Petrify output:\n" + petrifyResult.getReturnValue().getErrorsHeadAndTail(),
                        "Conversion failed", JOptionPane.WARNING_MESSAGE);
            } else {
                ExceptionDialog.show(mainWindow, result.getCause());
            }
        }
    }

    protected void highlightCscConflicts(final VisualFst visualFst) {
        final HashMap<String, Color> codeToColorMap = new HashMap<>();
        for (final VisualState state: visualFst.getVisualStates()) {
            final String name = visualFst.getMathName(state);
            if (name.endsWith("_csc")) {
                String code = null;
                final String[] nameParts = name.split("_");
                if (nameParts.length == 3) {
                    code = name.split("_")[1];
                }
                if (code != null) {
                    Color color = codeToColorMap.get(code);
                    if (color == null) {
                        color = colorGenerator.updateColor();
                        codeToColorMap.put(code, color);
                    }
                    state.setFillColor(color);
                }
            }
        }
    }

    public WorkspaceEntry getResult() {
        return result;
    }

}
