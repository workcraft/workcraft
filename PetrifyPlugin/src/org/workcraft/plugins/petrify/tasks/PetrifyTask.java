package org.workcraft.plugins.petrify.tasks;

import java.io.IOException;
import java.util.ArrayList;

import org.workcraft.interop.ExternalProcess;
import org.workcraft.interop.ExternalProcessListener;
import org.workcraft.plugins.petrify.PetrifyUtilitySettings;
import org.workcraft.plugins.shared.tasks.ExternalProcessResult;
import org.workcraft.tasks.ProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.tasks.Task;
import org.workcraft.util.DataAccumulator;

public class PetrifyTask implements Task<ExternalProcessResult>, ExternalProcessListener {
		private String[] args;
		private String inputFileName;

		private volatile boolean finished;
		private volatile int returnCode;
		private boolean userCancelled = false;
		private ProgressMonitor<? super ExternalProcessResult> monitor;

		private DataAccumulator stdoutAccum = new DataAccumulator();
		private DataAccumulator stderrAccum = new DataAccumulator();

		public PetrifyTask(String[] args, String inputFileName) {
			this.args = args;
			this.inputFileName = inputFileName;
		}

		@Override
		public Result<? extends ExternalProcessResult> run(ProgressMonitor<? super ExternalProcessResult> monitor) {
			this.monitor = monitor;
			ArrayList<String> command = new ArrayList<String>();

			// Name of the executable
			String toolName = PetrifyUtilitySettings.getPetrifyCommand();
			command.add(toolName);

			// Extra arguments
			for (String arg : PetrifyUtilitySettings.getPetrifyArgs().split(" ")) {
				if (!arg.isEmpty()) {
					command.add(arg);
				}
			}

			// Built-in arguments
			for (String arg : args) {
				command.add(arg);
			}
			command.add(inputFileName);

			ExternalProcess petrifyProcess = new ExternalProcess(command.toArray(new String[command.size()]), ".");
			petrifyProcess.addListener(this);

			try {
				System.out.println("Running external command: " + getCommandLine(command));
				petrifyProcess.start();
			} catch (IOException e) {
				return new Result<ExternalProcessResult>(e);
			}

			while (true) {
				if (monitor.isCancelRequested() && petrifyProcess.isRunning()) {
					petrifyProcess.cancel();
					userCancelled = true;
				}
				if (finished) {
					break;
				}
				try {
					Thread.sleep(20);
				} catch (InterruptedException e) {
					petrifyProcess.cancel();
					userCancelled = true;
					break;
				}
			}

			if (userCancelled) {
				return new Result<ExternalProcessResult>(Outcome.CANCELLED);
			}

			ExternalProcessResult result = new ExternalProcessResult(returnCode, stdoutAccum.getData(), stderrAccum.getData());

			if (returnCode == 0) {
				return new Result<ExternalProcessResult>(Outcome.FINISHED, result);
			}

			return new Result<ExternalProcessResult>(Outcome.FAILED, result);
		}

		private String getCommandLine(ArrayList<String> command) {
			String commandLine = "";
			for (String arg: command) {
				if (commandLine.isEmpty()) {
					commandLine = "";
				} else {
					commandLine += " ";
				}
				commandLine += arg;
			}
			return commandLine;
		}

		@Override
		public void errorData(byte[] data) {
			try {
				stderrAccum.write(data);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			monitor.stderr(data);
		}

		@Override
		public void outputData(byte[] data) {
			try {
				stdoutAccum.write(data);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			monitor.stdout(data);
		}

		@Override
		public void processFinished(int returnCode) {
			this.returnCode = returnCode;
			this.finished = true;
		}
	}