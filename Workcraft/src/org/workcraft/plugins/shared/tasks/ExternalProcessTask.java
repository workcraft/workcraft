package org.workcraft.plugins.shared.tasks;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.workcraft.interop.ExternalProcess;
import org.workcraft.interop.ExternalProcessListener;
import org.workcraft.tasks.ProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Task;
import org.workcraft.util.DataAccumulator;

public class ExternalProcessTask implements Task<ExternalProcessResult>, ExternalProcessListener {
	private List<String> args;

	private volatile boolean finished;
	private volatile int returnCode;
	private boolean userCancelled = false;
	private ProgressMonitor<? super ExternalProcessResult> monitor;

	private DataAccumulator stdoutAccum = new DataAccumulator();
	private DataAccumulator stderrAccum = new DataAccumulator();

	private final File workingDir;

	public ExternalProcessTask(List<String> args, File workingDir) {
		this.args = args;
		this.workingDir = workingDir;
	}

	@Override
	public Result<? extends ExternalProcessResult> run(ProgressMonitor<? super ExternalProcessResult> monitor) {
		this.monitor = monitor;

		ExternalProcess process = new ExternalProcess(args.toArray(new String[args.size()]), workingDir);

		process.addListener(this);

		try {
			process.start();
		} catch (IOException e) {
			return Result.exception(e);
		}

		while (true) {
			if (monitor.isCancelRequested() && process.isRunning()) {
				process.cancel();
				userCancelled = true;
			}
			if (finished)
				break;
			try {
				Thread.sleep(20);
			} catch (InterruptedException e) {
				process.cancel();
				userCancelled = true;
				break;
			}
		}

		if (userCancelled)
			return Result.cancelled();

		ExternalProcessResult result = new ExternalProcessResult(returnCode, stdoutAccum.getData(), stderrAccum.getData(), Collections.<String, byte[]>emptyMap());

		return Result.finished(result);
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
