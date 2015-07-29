package org.workcraft.plugins.cpog.tasks;

import java.io.File;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import org.workcraft.interop.ExternalProcessListener;
import org.workcraft.plugins.cpog.EncoderSettings;
import org.workcraft.plugins.shared.tasks.ExternalProcessResult;
import org.workcraft.plugins.shared.tasks.ExternalProcessTask;
import org.workcraft.tasks.ProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.tasks.SubtaskMonitor;
import org.workcraft.tasks.Task;
import org.workcraft.workspace.WorkspaceEntry;

public class ScencoExternalToolTask implements Task<ScencoResult>, ExternalProcessListener  {

	private ArrayList<String> args;

	private WorkspaceEntry we;
	private ScencoExecutionSupport cpogBuilder;
	private File scenarioFile, encodingFile,resultDir;
	private ScencoSolver solver;

	public ScencoExternalToolTask(EncoderSettings settings, WorkspaceEntry we, ScencoSolver scencoSolver){
		this.setSettings(settings);
		this.args = new ArrayList<String>();
		this.we = we;
		cpogBuilder = new ScencoExecutionSupport();
		this.solver = scencoSolver;
	}

	@Override
	public Result<? extends ScencoResult> run(ProgressMonitor<? super ScencoResult> monitor){

		args = solver.getScencoArguments();

		// Error handling
		if(args.get(0).contains("ERROR")){
			JOptionPane.showMessageDialog(null,
					args.get(1),
					args.get(2),
					JOptionPane.ERROR_MESSAGE);
			we.cancelMemento();
			ScencoResult result = new ScencoResult(args.get(2));
			return new Result<ScencoResult>(Outcome.FAILED, result);
		}

		// Running the tool through external process interface
		ExternalProcessTask externalProcessTask = new ExternalProcessTask(args, new File("."));
		SubtaskMonitor<Object> mon = new SubtaskMonitor<Object>(monitor);
		Result<? extends ExternalProcessResult> result = externalProcessTask.run(mon);

		// Handling the result
		final Outcome outcome;
		if (result.getOutcome() == Outcome.CANCELLED) {
			cpogBuilder.deleteTempFiles(scenarioFile, encodingFile, resultDir);
			we.cancelMemento();
			return new Result<ScencoResult>(Outcome.CANCELLED);
		} else {
			if (result.getReturnValue().getReturnCode() == 0) {
				outcome = Outcome.FINISHED;
			} else {
				cpogBuilder.deleteTempFiles(scenarioFile, encodingFile, resultDir);
				we.cancelMemento();
				outcome = Outcome.FAILED;
			}
			String stdout = new String(result.getReturnValue().getOutput());
			ScencoResult finalResult = new ScencoResult(stdout);
			return new Result<ScencoResult>(outcome, finalResult);
		}
	}

	public void setSettings(EncoderSettings settings) {
	}

	@Override
	public void processFinished(int returnCode) {
	}

	@Override
	public void errorData(byte[] data) {
	}

	@Override
	public void outputData(byte[] data) {
	}

	public void setWe(WorkspaceEntry we) {
		this.we = we;
	}

	public ScencoSolver getSolver() {
			return solver;
		}

		public void setSolver(ScencoSolver solver) {
			this.solver = solver;
		}
}

