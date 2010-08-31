package org.workcraft.plugins.petrify.tasks;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.workcraft.interop.ExternalProcess;
import org.workcraft.interop.ExternalProcessListener;
import org.workcraft.plugins.shared.PetrifyUtilitySettings;
import org.workcraft.tasks.ProgressMonitor;
import org.workcraft.tasks.Result;
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
				command.add("-nolog");
			else {
				command.add("-log");
				command.add(logFile.getCanonicalPath());
			}

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
		ExternalProcess petrifyProcess = new ExternalProcess(command.toArray(new String[command.size()]), ".");

		// supervise petrify process
		petrifyProcess.addListener(this);

		try {
			petrifyProcess.start();
		} catch (IOException e) {
			return new Result<SynthesisResult>(e);
		}

		while (true) {
			if (monitor.isCancelRequested() && petrifyProcess.isRunning()) {
				petrifyProcess.cancel();
				userCancelled = true;
			}
			if (finished)
				break;
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				petrifyProcess.cancel();
				userCancelled = true;
				break;
			}
		}

		if (userCancelled)
			return new Result<SynthesisResult>(Outcome.CANCELLED);

		// build SynthesisResult
		//ExternalProcessResult result = new ExternalProcessResult(returnCode, stdoutAccum.getData(), stderrAccum.getData());
		SynthesisResult result = new SynthesisResult(this.equationsFile, this.logFile);

		if (returnCode < 2)
			return new Result<SynthesisResult>(Outcome.FINISHED, result);
		else
			return new Result<SynthesisResult>(Outcome.FAILED, result);
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
