package org.workcraft.plugins.pcomp.tasks;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.workcraft.plugins.pcomp.gui.PCompOutputMode;
import org.workcraft.plugins.shared.PcompUtilitySettings;
import org.workcraft.plugins.shared.tasks.ExternalProcessResult;
import org.workcraft.plugins.verification.tasks.ExternalProcessTask;
import org.workcraft.tasks.ProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Task;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.util.FileUtils;

public class PcompTask implements Task<ExternalProcessResult> {
	private File[] inputs;
	private final PCompOutputMode mode;
	private final boolean improved;

	public PcompTask(File[] inputs, PCompOutputMode mode, boolean improved)
	{
		this.inputs = inputs;
		this.mode = mode;
		this.improved = improved;
	}

	@Override
	public Result<? extends ExternalProcessResult> run(ProgressMonitor<? super ExternalProcessResult> monitor)
	{
		ArrayList<String> command = new ArrayList<String>();
		command.add(PcompUtilitySettings.getPcompCommand());

		for (String arg : PcompUtilitySettings.getPcompArgs().split(" "))
			if (!arg.isEmpty())
				command.add(arg);

		if(mode == PCompOutputMode.DUMMY)
		{
			command.add("-d");
			command.add("-r");
		}

		if(mode == PCompOutputMode.INTERNAL)
			command.add("-i");

		if(improved)
			command.add("-p");


		File listFile;

		try {
			listFile = File.createTempFile("pcomp_", ".list");
		} catch (IOException e) {
			return Result.exception(e);
		}

		try
		{
			StringBuilder fileList = new StringBuilder();
			for (File f : inputs)
			{
				fileList.append(f.getAbsolutePath());
				fileList.append('\n');
			}

			try {
				FileUtils.writeAllText(listFile, fileList.toString());
			} catch (IOException e) {
				return Result.exception(e);
			}

			command.add("@"+listFile.getAbsolutePath());

			Result<? extends ExternalProcessResult> res = new ExternalProcessTask(command, new File(".")).run(monitor);
			if (res.getOutcome() != Outcome.FINISHED)
				return res;

			ExternalProcessResult retVal = res.getReturnValue();
			if (retVal.getReturnCode() < 2)
				return Result.finished(retVal);
			else
				return Result.failed(retVal);
		}
		finally {
			listFile.delete();
		}
	}
}