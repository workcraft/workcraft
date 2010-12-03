package org.workcraft.plugins.circuit.tools;

import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.workcraft.Framework;
import org.workcraft.Tool;
import org.workcraft.Trace;
import org.workcraft.plugins.circuit.Circuit;
import org.workcraft.plugins.circuit.VisualCircuit;
import org.workcraft.plugins.circuit.tasks.CheckCircuitTask;
import org.workcraft.plugins.mpsat.MpsatResultParser;
import org.workcraft.plugins.mpsat.gui.SolutionsDialog;
import org.workcraft.plugins.mpsat.tasks.MpsatChainResult;
import org.workcraft.tasks.ProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.util.GUI;
import org.workcraft.workspace.Workspace;
import org.workcraft.workspace.WorkspaceEntry;

public class CheckCircuitTool implements Tool {

	private final Framework framework;

	VisualCircuit circuit;
	private CheckCircuitTask checkTask;


	ProgressMonitor<? super MpsatChainResult> monitor;

	public CheckCircuitTool(Framework framework, Workspace ws) {
		this.framework = framework;
//		this.ws = ws;
	}


	public String getDisplayName() {
		return "Check circuit for deadlocks and hazards";
	}


	@Override
	public String getSection() {
		return "Verification";
	}

	@Override
	public boolean isApplicableTo(WorkspaceEntry we) {
		return we.getModelEntry().getMathModel() instanceof Circuit;
	}

	@Override
	public void run(WorkspaceEntry we) {

		checkTask = new CheckCircuitTask(we, framework);

		framework.getTaskManager().queue(checkTask, "Checking circuit for deadlocks and hazards",
				new ProgressMonitor<MpsatChainResult>() {

					@Override
					public void finished(final Result<? extends MpsatChainResult> result, String description) {
						SwingUtilities.invokeLater(new Runnable(){

							@Override
							public void run() {

								MpsatResultParser mdp = new MpsatResultParser(result.getReturnValue().getMpsatResult().getReturnValue());

								List<Trace> solutions = mdp.getSolutions();

								if (!solutions.isEmpty()) {
									final SolutionsDialog solutionsDialog = new SolutionsDialog(checkTask, result.getReturnValue().getMessage(), solutions);

									GUI.centerAndSizeToParent(solutionsDialog, framework.getMainWindow());

									solutionsDialog.setVisible(true);

								} else
									JOptionPane.showMessageDialog(null, result.getReturnValue().getMessage());
							}

						});
					}

					@Override
					public boolean isCancelRequested() {
						return false;
					}

					@Override
					public void progressUpdate(double completion) {
					}

					@Override
					public void stderr(byte[] data) {
					}

					@Override
					public void stdout(byte[] data) {
					}
		}
		);

	}

}
