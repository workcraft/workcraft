package org.workcraft.plugins.fst.task;

import java.awt.Color;
import java.io.File;
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
import org.workcraft.util.FileUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

public class StgToFstConversionResultHandler extends DummyProgressMonitor<WriteSgConversionResult> {

    private final ColorGenerator colorGenerator = new ColorGenerator(ColorUtils.getHsbPalette(
            new float[]{0.45f, 0.15f, 0.70f, 0.25f, 0.05f, 0.80f, 0.55f, 0.20f, 075f, 0.50f},
            new float[]{0.30f}, new float[]{0.9f, 0.7f, 0.5f}));

    private final WriteSgConversionTask task;
    private WorkspaceEntry result;

    public StgToFstConversionResultHandler(WriteSgConversionTask task) {
        this.task = task;
        this.result = null;
    }

    @Override
    public void finished(final Result<? extends WriteSgConversionResult> result, String description) {
        final Framework framework = Framework.getInstance();
        WorkspaceEntry we = task.getWorkspaceEntry();
        Path<String> path = we.getWorkspacePath();
        if (result.getOutcome() == Outcome.FINISHED) {
            Fst model = result.getReturnValue().getConversionResult();
            final Path<String> directory = path.getParent();
            final String name = FileUtils.getFileNameWithoutExtension(new File(path.getNode()));
            final ModelEntry me = new ModelEntry(new FstDescriptor(), model);
            this.result = framework.createWork(me, directory, name);
            VisualModel visualModel = me.getVisualModel();
            if (visualModel instanceof VisualFst) {
                highlightCscConflicts((VisualFst) visualModel);
            }
        } else if (result.getOutcome() != Outcome.CANCELLED) {
            MainWindow mainWindow = framework.getMainWindow();
            if (result.getCause() == null) {
                Result<? extends ExternalProcessResult> petrifyResult = result.getReturnValue().getResult();
                JOptionPane.showMessageDialog(mainWindow,
                        "Petrify output: \n\n" + new String(petrifyResult.getReturnValue().getErrors()),
                        "Conversion failed", JOptionPane.WARNING_MESSAGE);
            } else {
                ExceptionDialog.show(mainWindow, result.getCause());
            }
        }
    }

    protected void highlightCscConflicts(VisualFst visualFst) {
        HashMap<String, Color> codeToColorMap = new HashMap<>();
        for (VisualState state: visualFst.getVisualStates()) {
            String name = visualFst.getMathName(state);
            if (name.endsWith("_csc")) {
                String code = null;
                String[] nameParts = name.split("_");
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
