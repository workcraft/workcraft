package org.workcraft.plugins.sdfs.tools;

import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.workcraft.Framework;
import org.workcraft.Tool;
import org.workcraft.Trace;
import org.workcraft.plugins.mpsat.MpsatResultParser;
import org.workcraft.plugins.mpsat.gui.SolutionsDialog;
import org.workcraft.plugins.mpsat.tasks.MpsatChainResult;
import org.workcraft.plugins.sdfs.SDFS;
import org.workcraft.plugins.sdfs.tasks.CheckDataflowTask;
import org.workcraft.tasks.ProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.util.GUI;
import org.workcraft.workspace.Workspace;
import org.workcraft.workspace.WorkspaceEntry;

public class CheckDataflowTool implements Tool {
	private final Framework framework;
	private CheckDataflowTask task;
	ProgressMonitor<? super MpsatChainResult> monitor;

	public CheckDataflowTool(Framework framework, Workspace ws) {
		this.framework = framework;
	}

	public String getDisplayName() {
		return "Check dataflow for deadlocks and hazards (reuse unfolding data)";
	}

	@Override
	public String getSection() {
		return "Verification";
	}

	@Override
	public boolean isApplicableTo(WorkspaceEntry we) {
		return we.getModelEntry().getMathModel() instanceof SDFS;
	}

	@Override
	public void run(WorkspaceEntry we) {
		task = new CheckDataflowTask(we, framework);
		framework.getTaskManager().queue(task, "Checking dataflow for deadlocks and hazards",
				new ProgressMonitor<MpsatChainResult>() {

					@Override
					public void finished(final Result<? extends MpsatChainResult> result, String description) {
						SwingUtilities.invokeLater(new Runnable(){
							@Override
							public void run() {
								MpsatResultParser mdp = new MpsatResultParser(result.getReturnValue().getMpsatResult().getReturnValue());
								List<Trace> solutions = mdp.getSolutions();
								if (!solutions.isEmpty()) {
									final SolutionsDialog solutionsDialog = new SolutionsDialog(task, result.getReturnValue().getMessage(), solutions);
									GUI.centerAndSizeToParent(solutionsDialog, framework.getMainWindow());
									solutionsDialog.setVisible(true);
								} else {
									JOptionPane.showMessageDialog(null, result.getReturnValue().getMessage());
								}
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
