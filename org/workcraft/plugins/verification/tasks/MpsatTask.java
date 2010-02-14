package org.workcraft.plugins.verification.tasks;

import java.io.IOException;
import java.util.ArrayList;

import org.workcraft.interop.ExternalProcess;
import org.workcraft.interop.ExternalProcessListener;
import org.workcraft.plugins.verification.MpsatSettings;
import org.workcraft.tasks.ProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Task;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.util.DataAccumulator;

public class MpsatTask implements Task<ExternalProcessResult>, ExternalProcessListener {
		private String[] args;
		private String inputFileName;

		private volatile boolean finished;
		private volatile int returnCode;
		private boolean userCancelled = false;
		private ProgressMonitor<ExternalProcessResult> monitor;

		private DataAccumulator stdoutAccum = new DataAccumulator();
		private DataAccumulator stderrAccum = new DataAccumulator();

		public MpsatTask(String[] args, String inputFileName) {
			this.args = args;
			this.inputFileName = inputFileName;
		}

		@Override
		public Result<ExternalProcessResult> run(ProgressMonitor<ExternalProcessResult> monitor) {
			this.monitor = monitor;

			ArrayList<String> command = new ArrayList<String>();
			command.add(MpsatSettings.getMpsatCommand());

			for (String arg : MpsatSettings.getMpsatArgs().split(" "))
				if (!arg.isEmpty())
					command.add(arg);

			for (String arg : args)
				command.add(arg);

			command.add(inputFileName);

			ExternalProcess mpsatProcess = new ExternalProcess(command.toArray(new String[command.size()]), ".");

			mpsatProcess.addListener(this);

			try {
				mpsatProcess.start();
			} catch (IOException e) {
				return new Result<ExternalProcessResult>(e);
			}

			while (true) {
				if (monitor.isCancelRequested() && mpsatProcess.isRunning()) {
					mpsatProcess.cancel();
					userCancelled = true;
				}
				if (finished)
					break;
				try {
					Thread.sleep(20);
				} catch (InterruptedException e) {
					mpsatProcess.cancel();
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