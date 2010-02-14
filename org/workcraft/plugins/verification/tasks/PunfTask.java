package org.workcraft.plugins.verification.tasks;

import java.io.IOException;
import java.util.ArrayList;

import org.workcraft.interop.ExternalProcess;
import org.workcraft.interop.ExternalProcessListener;
import org.workcraft.plugins.verification.PunfSettings;
import org.workcraft.tasks.ProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Task;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.util.DataAccumulator;

public class PunfTask implements Task<ExternalProcessResult>, ExternalProcessListener {
		private String inputPath, outputPath;
		private volatile boolean finished;
		private volatile int returnCode;
		private boolean userCancelled = false;
		private ProgressMonitor<ExternalProcessResult> monitor;

		private DataAccumulator stdoutAccum = new DataAccumulator();
		private DataAccumulator stderrAccum = new DataAccumulator();

		public PunfTask(String inputPath, String outputPath) {
			this.inputPath = inputPath;
			this.outputPath = outputPath;
		}

		@Override
		public Result<ExternalProcessResult> run(ProgressMonitor<ExternalProcessResult> monitor) {
			this.monitor = monitor;

			ArrayList<String> command = new ArrayList<String>();
			command.add(PunfSettings.getPunfCommand());

			for (String arg : PunfSettings.getPunfArgs().split(" "))
				if (!arg.isEmpty())
					command.add(arg);

			command.add("-m="+outputPath);
			command.add(inputPath);

			ExternalProcess punfProcess = new ExternalProcess(command.toArray(new String[command.size()]), ".");

			punfProcess.addListener(this);

			try {
				punfProcess.start();
			} catch (IOException e) {
				return new Result<ExternalProcessResult>(Outcome.FAILED);
			}

			while (true) {
				if (monitor.isCancelRequested() && punfProcess.isRunning()) {
					punfProcess.cancel();
					userCancelled = true;
				}
				if (finished)
					break;
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					punfProcess.cancel();
					userCancelled = true;
					break;
				}
			}

			if (userCancelled)
				return new Result<ExternalProcessResult>(Outcome.CANCELLED);

			ExternalProcessResult result = new ExternalProcessResult(returnCode, stdoutAccum.getData(), stderrAccum.getData());

			if (returnCode < 2)
				return new Result<ExternalProcessResult>(Outcome.FINISHED, result);

			return new Result<ExternalProcessResult>(Outcome.FAILED, result);
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