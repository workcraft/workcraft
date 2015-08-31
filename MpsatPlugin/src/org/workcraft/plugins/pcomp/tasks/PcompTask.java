package org.workcraft.plugins.pcomp.tasks;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.workcraft.plugins.pcomp.PcompUtilitySettings;
import org.workcraft.plugins.shared.tasks.ExternalProcessResult;
import org.workcraft.plugins.shared.tasks.ExternalProcessTask;
import org.workcraft.tasks.ProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.tasks.Task;
import org.workcraft.util.FileUtils;

public class PcompTask implements Task<ExternalProcessResult> {

	public enum ConversionMode {
		DUMMY,
		INTERNAL,
		OUTPUT
	}

	private final File[] inputFiles;
	private final ConversionMode conversionMode;
	private final boolean useSharedOutputs;
	private final boolean useImprovedComposition;
	private final File directory;

	public PcompTask(File[] inputFiles, ConversionMode conversionMode, boolean useSharedOutputs, boolean useImprovedComposition, File directory) {
		this.inputFiles = inputFiles;
		this.conversionMode = conversionMode;
		this.useSharedOutputs = useSharedOutputs;
		this.useImprovedComposition = useImprovedComposition;
		this.directory = directory;
	}

	@Override
	public Result<? extends ExternalProcessResult> run(ProgressMonitor<? super ExternalProcessResult> monitor) {
		ArrayList<String> command = new ArrayList<String>();
		command.add(PcompUtilitySettings.getCommand());

		for (String arg : PcompUtilitySettings.getExtraArgs().split(" ")) {
			if (!arg.isEmpty()) {
				command.add(arg);
			}
		}

		if (conversionMode == ConversionMode.DUMMY) {
			command.add("-d");
			command.add("-r");
		} else if (conversionMode == ConversionMode.INTERNAL) {
			command.add("-i");
		}

		if (useSharedOutputs) {
			command.add("-o");
		}

		if (useImprovedComposition) {
			command.add("-p");
		}

		File listFile = null;
		try {
			if (directory == null) {
				listFile = File.createTempFile("places_", ".list");
			} else {
				listFile = new File(directory, "places.list");
			}
		} catch (IOException e) {
			return Result.exception(e);
		}
		command.add("-l" + listFile.getAbsolutePath());

		for (File inputFile: inputFiles) {
			command.add(inputFile.getAbsolutePath());
		}

		Result<? extends ExternalProcessResult> res = new ExternalProcessTask(command).run(monitor);
		if (res.getOutcome() != Outcome.FINISHED) {
			return res;
		}

		Map<String, byte[]> outputFiles = new HashMap<String, byte[]>();
		try {
			if(listFile.exists()) {
				outputFiles.put("places.list", FileUtils.readAllBytes(listFile));
			}
		} catch (IOException e) {
			return new Result<ExternalProcessResult>(e);
		}

		ExternalProcessResult retVal = res.getReturnValue();
		ExternalProcessResult result = new ExternalProcessResult(retVal.getReturnCode(), retVal.getOutput(), retVal.getErrors(), outputFiles);
		if (retVal.getReturnCode() < 2) {
			return Result.finished(result);
		} else {
			return Result.failed(result);
		}

	}
}