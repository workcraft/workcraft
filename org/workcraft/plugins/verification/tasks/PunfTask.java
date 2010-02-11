package org.workcraft.plugins.verification.tasks;

import java.io.IOException;
import java.util.ArrayList;

import org.workcraft.interop.ExternalProcess;
import org.workcraft.interop.ExternalProcessListener;
import org.workcraft.plugins.verification.PunfSettings;
import org.workcraft.tasks.ExceptionResult;
import org.workcraft.tasks.ExternalProcessResult;
import org.workcraft.tasks.ProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Task;

public class PunfTask implements Task, ExternalProcessListener {
		private String inputPath, outputPath;
		private volatile boolean finished;
		private volatile int returnCode;
		private boolean userCancelled = false;
		private ProgressMonitor monitor;

		public PunfTask(String inputPath, String outputPath) {
			this.inputPath = inputPath;
			this.outputPath = outputPath;
		}

		@Override
		public Result run(ProgressMonitor monitor) {
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
				return new ExceptionResult (e);
			}

			while (true) {
				if (monitor.isCancelRequested() && punfProcess.isRunning()) {
					punfProcess.cancel();
					userCancelled = true;
				}
				if (finished)
					break;
				try {
					Thread.sleep(20);
				} catch (InterruptedException e) {
					punfProcess.cancel();
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