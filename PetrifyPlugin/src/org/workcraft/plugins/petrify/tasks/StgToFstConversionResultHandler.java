package org.workcraft.plugins.petrify.tasks;

import java.awt.Color;
import java.util.HashMap;

import org.workcraft.Framework;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.gui.ExceptionDialog;
import org.workcraft.gui.workspace.Path;
import org.workcraft.plugins.fsm.VisualState;
import org.workcraft.plugins.fst.Fst;
import org.workcraft.plugins.fst.FstDescriptor;
import org.workcraft.plugins.fst.VisualFst;
import org.workcraft.plugins.shared.tasks.ExternalProcessResult;
import org.workcraft.tasks.AbstractExtendedResultHandler;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.util.ColorGenerator;
import org.workcraft.util.ColorUtils;
import org.workcraft.util.DialogUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

public class StgToFstConversionResultHandler extends AbstractExtendedResultHandler<WriteSgConversionResult, WorkspaceEntry> {

    private final ColorGenerator colorGenerator = new ColorGenerator(ColorUtils.getHsbPalette(
            new float[]{0.45f, 0.15f, 0.70f, 0.25f, 0.05f, 0.80f, 0.55f, 0.20f, 075f, 0.50f},
            new float[]{0.30f}, new float[]{0.9f, 0.7f, 0.5f}));

    private final WriteSgConversionTask task;

    public StgToFstConversionResultHandler(final WriteSgConversionTask task) {
        this.task = task;
    }

    @Override
    public WorkspaceEntry handleResult(final Result<? extends WriteSgConversionResult> result) {
        WorkspaceEntry weResult = null;
        if (result.getOutcome() == Outcome.SUCCESS) {
            Fst model = result.getReturnValue().getConversionResult();
            ModelEntry me = new ModelEntry(new FstDescriptor(), model);
            Path<String> path = task.getWorkspaceEntry().getWorkspacePath();
            Framework framework = Framework.getInstance();
            weResult = framework.createWork(me, path);
            // NOTE: WorkspaceEntry with a new ModelEntry is created
            VisualModel visualModel = weResult.getModelEntry().getVisualModel();
            if (visualModel instanceof VisualFst) {
                highlightCscConflicts((VisualFst) visualModel);
            }
        } else if (result.getOutcome() == Outcome.FAILURE) {
            if (result.getCause() != null) {
                ExceptionDialog.show(result.getCause());
            } else {
                final Result<? extends ExternalProcessResult> petrifyResult = result.getReturnValue().getResult();
                DialogUtils.showWarning("Petrify output:\n" + petrifyResult.getReturnValue().getErrorsHeadAndTail());
            }
        }
        return weResult;
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

}
