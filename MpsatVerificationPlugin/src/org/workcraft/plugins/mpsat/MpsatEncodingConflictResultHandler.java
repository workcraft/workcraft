package org.workcraft.plugins.mpsat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;

import javax.swing.JOptionPane;

import org.workcraft.Framework;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.ToolboxPanel;
import org.workcraft.gui.graph.GraphEditorPanel;
import org.workcraft.plugins.mpsat.gui.MpsatSolution;
import org.workcraft.plugins.shared.tasks.ExternalProcessResult;
import org.workcraft.plugins.stg.tools.Core;
import org.workcraft.plugins.stg.tools.EncodingConflictAnalyserTool;
import org.workcraft.tasks.Result;
import org.workcraft.util.ColorGenerator;
import org.workcraft.util.ColorUtils;
import org.workcraft.workspace.WorkspaceEntry;

final class MpsatEncodingConflictResultHandler implements Runnable {

    private final ColorGenerator colorGenerator = new ColorGenerator(ColorUtils.getHsbPalette(
            new float[]{0.45f, 0.15f, 0.70f, 0.25f, 0.05f, 0.80f, 0.55f, 0.20f, 075f, 0.50f},
            new float[]{0.30f}, new float[]{0.9f, 0.7f, 0.5f}));

    private final WorkspaceEntry we;
    private final Result<? extends ExternalProcessResult> result;

    MpsatEncodingConflictResultHandler(WorkspaceEntry we, Result<? extends ExternalProcessResult> result) {
        this.we = we;
        this.result = result;
    }

    @Override
    public void run() {
        MpsatResultParser mdp = new MpsatResultParser(result.getReturnValue());
        List<MpsatSolution> solutions = mdp.getSolutions();
        if (!MpsatSolution.hasTraces(solutions)) {
            MainWindow mainWindow = Framework.getInstance().getMainWindow();
            JOptionPane.showMessageDialog(mainWindow, "No encoding conflicts.",
                    "Verification results", JOptionPane.INFORMATION_MESSAGE);
        } else {
            final Framework framework = Framework.getInstance();
            final MainWindow mainWindow = framework.getMainWindow();
            GraphEditorPanel currentEditor = mainWindow.getEditor(we);
            final ToolboxPanel toolbox = currentEditor.getToolBox();
            final EncodingConflictAnalyserTool tool = toolbox.getToolInstance(EncodingConflictAnalyserTool.class);
            toolbox.selectTool(tool);
            ArrayList<Core> cores = new ArrayList<>(convertSolutionsToCores(solutions));
            Collections.sort(cores, new Comparator<Core>() {
                @Override
                public int compare(Core c1, Core c2) {
                    if (c1.size() > c2.size()) return 1;
                    if (c1.size() < c2.size()) return -1;
                    if (c1.toString().length() > c2.toString().length()) return 1;
                    if (c1.toString().length() < c2.toString().length()) return -1;
                    return 0;
                }
            });
            tool.setCores(cores);
        }
    }

    private LinkedHashSet<Core> convertSolutionsToCores(List<MpsatSolution> solutions) {
        LinkedHashSet<Core> cores = new LinkedHashSet<>();
        for (MpsatSolution solution: solutions) {
            Core core = new Core(solution.getMainTrace(), solution.getBranchTrace(), solution.getComment());
            boolean isDuplicateCore = cores.contains(core);
            if (!isDuplicateCore) {
                core.setColor(colorGenerator.updateColor());
                cores.add(core);
            }
            if (MpsatUtilitySettings.getDebugCores()) {
                if (solution.getComment() == null) {
                    System.out.println("Encoding conflict:");
                } else {
                    System.out.println("Encoding conflict for signal '" + solution.getComment() + "':");
                }
                System.out.println("    Configuration 1: " + solution.getMainTrace());
                System.out.println("    Configuration 2: " + solution.getBranchTrace());
                System.out.println("    Conflict core" + (isDuplicateCore ? " (duplicate)" : "") + ": " + core);
                System.out.println();
            }
        }
        return cores;
    }

}
