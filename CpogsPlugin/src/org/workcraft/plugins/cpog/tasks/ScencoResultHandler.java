package org.workcraft.plugins.cpog.tasks;

import javax.swing.JOptionPane;

import org.workcraft.Framework;
import org.workcraft.tasks.DummyProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;

public class ScencoResultHandler extends DummyProgressMonitor<ScencoResult> {

	private ScencoExternalToolTask scenco;
	private ScencoSolver solver;

	public ScencoResultHandler(	ScencoExternalToolTask scencoTask) {
		this.scenco = scencoTask;
		this.solver = scencoTask.getSolver();
	}

	@Override
	public void finished(Result<? extends ScencoResult> result, String description) {
		if (result.getOutcome() == Outcome.FINISHED) {
			String[] stdoutLines = result.getReturnValue().getStdout().split("\n");
			String resultDirectory = result.getReturnValue().getResultDirectory();
			solver.handleResult(stdoutLines, resultDirectory);
		} else if (result.getOutcome() == Outcome.FAILED) {
			String errorMessage = getErrorMessage(result.getReturnValue().getStdout());
			final Framework framework = Framework.getInstance();
			if(errorMessage.contains("Internal error")){
				String[] sentence = result.getReturnValue().getStdout().split("\n");
				for (int i=0; i <sentence.length; i++){
					System.out.println(sentence[i]);
				}
			}
			JOptionPane.showMessageDialog(framework.getMainWindow(), errorMessage, "SCENCO error", JOptionPane.ERROR_MESSAGE);
		}
	}

	// Get the error from the STDOUT of SCENCO
	private String getErrorMessage(String msg) {
		String[] sentence = msg.split("\n");
		int i=0;
		for (i=0; i <sentence.length; i++){
			if(sentence[i].contains(".error")){
					return sentence[i+1];
			}
		}
		return "Internal error. Contact developers at www.workcraft.org/";
	}

	// GETTER AND SETTER
	public ScencoExternalToolTask getScenco() {
		return scenco;
	}

	public void setScenco(ScencoExternalToolTask scenco) {
		this.scenco = scenco;
	}
}
