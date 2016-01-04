package org.workcraft.plugins.mpsat;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import javax.swing.JOptionPane;

import org.workcraft.Framework;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.ToolboxPanel;
import org.workcraft.gui.graph.GraphEditorPanel;
import org.workcraft.plugins.mpsat.gui.Solution;
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
            new float[]{0.30f},  new float[]{0.9f, 0.7f, 0.5f}));

	private final WorkspaceEntry we;
	private final Result<? extends ExternalProcessResult> result;

	MpsatEncodingConflictResultHandler(WorkspaceEntry we, Result<? extends ExternalProcessResult> result) {
		this.we = we;
		this.result = result;
	}

	@Override
	public void run() {
		MpsatResultParser mdp = new MpsatResultParser(result.getReturnValue());
		List<Solution> solutions = mdp.getSolutions();
		if ( !Solution.hasTraces(solutions) ) {
			JOptionPane.showMessageDialog(null, "No encodning conflicts.", "Verification results", JOptionPane.INFORMATION_MESSAGE);
		} else {
            GraphEditorPanel currentEditor = getCurrentEditor(we);
            final ToolboxPanel toolbox = currentEditor.getToolBox();
            final EncodingConflictAnalyserTool tool = toolbox.getToolInstance(EncodingConflictAnalyserTool.class);
            toolbox.selectTool(tool);
            ArrayList<Core> cores = new ArrayList<>(convertSolutionsToCores(solutions));
            tool.setCores(cores);
		}
	}

	private GraphEditorPanel getCurrentEditor(final WorkspaceEntry we) {
        final Framework framework = Framework.getInstance();
        final MainWindow mainWindow = framework.getMainWindow();
		GraphEditorPanel currentEditor = mainWindow.getCurrentEditor();
		if ((currentEditor == null) || (currentEditor.getWorkspaceEntry() != we)) {
		    final List<GraphEditorPanel> editors = mainWindow.getEditors(we);
		    if (editors.size()>0) {
		        currentEditor = editors.get(0);
		        mainWindow.requestFocus(currentEditor);
		    } else {
		        currentEditor = mainWindow.createEditorWindow(we);
		    }
		}
		return currentEditor;
	}

    private LinkedHashSet<Core> convertSolutionsToCores(List<Solution> solutions) {
        LinkedHashSet<Core> cores = new LinkedHashSet<>();
        for (Solution solution: solutions) {
            Core core = new Core(solution.getMainTrace(), solution.getBranchTrace(), solution.getComment());
            boolean isDuplicateCore = cores.contains(core);
            if (solution.getComment() == null) {
            	System.out.println("Encoding conflict:");
            } else {
            	System.out.println("Encoding conflict for signal '" + solution.getComment() + "':");
            }
            System.out.println("    Configuration 1: " + solution.getMainTrace());
            System.out.println("    Configuration 2: " + solution.getBranchTrace());
            System.out.println("    Conflict core" + (isDuplicateCore ? " (duplicate)" : "") + ": " + core);
            System.out.println();
            if ( !isDuplicateCore ) {
            	core.setColor(colorGenerator.updateColor());
            	cores.add(core);
            }
        }
        return cores;
    }

}
