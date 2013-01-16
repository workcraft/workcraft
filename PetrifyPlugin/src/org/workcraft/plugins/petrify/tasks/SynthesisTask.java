package org.workcraft.plugins.petrify.tasks;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.workcraft.interop.ExternalProcessListener;
import org.workcraft.plugins.shared.PetrifyUtilitySettings;
import org.workcraft.plugins.shared.tasks.ExternalProcessResult;
import org.workcraft.plugins.shared.tasks.ExternalProcessTask;
import org.workcraft.tasks.ProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.SubtaskMonitor;
import org.workcraft.tasks.Task;
import org.workcraft.tasks.Result.Outcome;

public class SynthesisTask implements Task<SynthesisResult>, ExternalProcessListener {
	private String[] args;
	private File inputFile;
	private File equationsFile;
	private File libraryFile;
	private File logFile;

	private volatile boolean finished;
	private volatile int returnCode;
	private boolean userCancelled = false;

	/**
	 * @param args - arguments corresponding to type of logic synthesis
	 * @param inputFile - specification (STG)
	 * @param equationsFile - equation Output in EQN format (not BLIF format)
	 * @param libraryFile - could be null
	 * @param logFile - could be null
	 */
	public SynthesisTask(String[] args, File inputFile,
			File equationsFile, File libraryFile,
			File logFile) {
		this.args = args;
		this.inputFile = inputFile;
		this.equationsFile = equationsFile;
		this.libraryFile = libraryFile;
		this.logFile = logFile;
	}

	@Override
	public Result<? extends SynthesisResult> run(ProgressMonitor<? super SynthesisResult> monitor) {

		// build the command line call for petrify
		ArrayList<String> command = new ArrayList<String>();
		command.add(PetrifyUtilitySettings.getPetrifyCommand());

		for (String arg : PetrifyUtilitySettings.getPetrifyArgs().split(" "))
			if (!arg.isEmpty())
				command.add(arg);

		for (String arg : args)
			command.add(arg);

		try {

			if (this.logFile == null)
				this.logFile = File.createTempFile("petrify", ".log");
			command.add("-log");
			command.add(logFile.getCanonicalPath());

			command.add("-eqn");
			command.add(equationsFile.getCanonicalPath());

			if (this.libraryFile != null) {
				command.add("-tm");
				command.add("-lib");
				command.add(libraryFile.getCanonicalPath());
			}

			command.add(inputFile.getCanonicalPath());
		} catch (IOException e1) {
			throw new RuntimeException(e1);
		}

		// call petrify on command line
		ExternalProcessTask externalProcessTask = new ExternalProcessTask(command, new File("."));
		SubtaskMonitor<Object> mon = new SubtaskMonitor<Object>(monitor);
		Result<? extends ExternalProcessResult> res = externalProcessTask.run(mon);

		final Outcome outcome;
		if (res.getOutcome() == Outcome.CANCELLED) {
			return new Result<SynthesisResult>(Outcome.CANCELLED);
		} else {
			if (res.getReturnValue().getReturnCode() == 0) {
				outcome = Outcome.FINISHED;
			} else {
				outcome = Outcome.FAILED;
			}
			String stdout = new String(res.getReturnValue().getOutput());
			String stderr = new String(res.getReturnValue().getErrors());
			SynthesisResult result = new SynthesisResult(this.equationsFile, this.logFile, stdout, stderr);
			return new Result<SynthesisResult>(outcome, result);
		}
	}


	@Override
	public void processFinished(int returnCode) {
		this.returnCode = returnCode;
		this.finished = true;
	}

	@Override
	public void errorData(byte[] data) {
		// TODO Auto-generated method stub

	}

	@Override
	public void outputData(byte[] data) {
		// TODO Auto-generated method stub

	}

}
