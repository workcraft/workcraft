package org.workcraft.plugins.verification.tasks;

import java.io.IOException;
import java.util.ArrayList;

import org.workcraft.interop.ExternalProcess;
import org.workcraft.interop.ExternalProcessListener;
import org.workcraft.plugins.verification.MpsatSettings;
import org.workcraft.tasks.ExceptionResult;
import org.workcraft.tasks.ExternalProcessResult;
import org.workcraft.tasks.ProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Task;

public class MpsatTask implements Task, ExternalProcessListener {
		private String[] args;
		private String inputFileName;

		private volatile boolean finished;
		private volatile int returnCode;
		private boolean userCancelled = false;
		private ProgressMonitor monitor;

		public MpsatTask(String[] args, String inputFileName) {
			this.args = args;
			this.inputFileName = inputFileName;
		}

		@Override
		public Result run(ProgressMonitor monitor) {
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
				return new ExceptionResult (e);
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

			return new ExternalProcessResult(returnCode, userCancelled);
		}

		@Override
		public void errorData(byte[] data) {
			monitor.logErrorMessage(new String(data));
		}

		@Override
		public void outputData(byte[] data) {
			monitor.logMessage(new String(data));
		}

		@Override
		public void processFinished(int returnCode) {
			this.returnCode = returnCode;
			this.finished = true;
		}
	}