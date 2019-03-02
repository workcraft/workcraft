package org.workcraft.plugins.petrify.commands;

import org.workcraft.Framework;
import org.workcraft.commands.AbstractConversionCommand;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.gui.ExceptionDialog;
import org.workcraft.gui.workspace.Path;
import org.workcraft.plugins.fsm.VisualState;
import org.workcraft.plugins.fst.Fst;
import org.workcraft.plugins.fst.FstDescriptor;
import org.workcraft.plugins.fst.VisualFst;
import org.workcraft.plugins.petrify.tasks.WriteSgConversionOutput;
import org.workcraft.plugins.petrify.tasks.WriteSgConversionTask;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.shared.ColorGenerator;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.TaskManager;
import org.workcraft.utils.ColorUtils;
import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.Hierarchy;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

import java.awt.*;
import java.util.HashMap;

public class PetrifyStgToFstConversionCommand extends AbstractConversionCommand {

    private final ColorGenerator colorGenerator = new ColorGenerator(ColorUtils.getHsbPalette(
            new float[]{0.45f, 0.15f, 0.70f, 0.25f, 0.05f, 0.80f, 0.55f, 0.20f, 075f, 0.50f},
            new float[]{0.30f}, new float[]{0.9f, 0.7f, 0.5f}));

    public boolean isBinary() {
        return false;
    }

    public Position getPosition() {
        return Position.TOP;
    }

    @Override
    public String getDisplayName() {
        if (isBinary()) {
            return "Finite State Transducer (binary-encoded) [Petrify]";
        } else {
            return "Finite State Transducer (basic) [Petrify]";
        }
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, Stg.class);
    }

    @Override
    public WorkspaceEntry execute(WorkspaceEntry we) {
        if (Hierarchy.isHierarchical(we.getModelEntry())) {
            DialogUtils.showError(
                    "Finite State Transducer cannot be derived from a hierarchical Signal Transition Graph.",
                    "Conversion error");
            return null;
        }
        TaskManager taskManager = Framework.getInstance().getTaskManager();
        WriteSgConversionTask task = new WriteSgConversionTask(we, isBinary());
        Result<? extends WriteSgConversionOutput> result = taskManager.execute(task, "Building state graph");
        return processResult(result, we.getWorkspacePath());
    }

    private WorkspaceEntry processResult(Result<? extends WriteSgConversionOutput> result, Path<String> path) {
        WorkspaceEntry we = null;
        WriteSgConversionOutput output = result.getPayload();
        if (result.getOutcome() == Result.Outcome.SUCCESS) {
            Fst model = output.getFst();
            ModelEntry me = new ModelEntry(new FstDescriptor(), model);
            we = Framework.getInstance().createWork(me, path);
            // NOTE: WorkspaceEntry with a new ModelEntry is created
            VisualModel visualModel = we.getModelEntry().getVisualModel();
            if (visualModel instanceof VisualFst) {
                highlightCscConflicts((VisualFst) visualModel);
            }
        } else if (result.getOutcome() == Result.Outcome.FAILURE) {
            if (result.getCause() != null) {
                ExceptionDialog.show(result.getCause());
            } else {
                DialogUtils.showWarning("Petrify output:\n" + output.getErrorsHeadAndTail());
            }
        }
        return we;
    }

    private void highlightCscConflicts(final VisualFst visualFst) {
        final HashMap<String, Color> codeToColorMap = new HashMap<>();
        for (final VisualState state: visualFst.getVisualStates()) {
            final String name = visualFst.getMathName(state);
            if (name.endsWith("_csc")) {
                String code = null;
                final String[] nameParts = name.split("_");
                if (nameParts.length == 3) {
                    code = nameParts[1];
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

    @Override
    public ModelEntry convert(ModelEntry me) {
        return null; // !!!
    }

}
