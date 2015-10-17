package org.workcraft.plugins.mpsat.tasks;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.workcraft.plugins.circuit.CircuitSettings;
import org.workcraft.plugins.mpsat.MpsatSynthesisUtilitySettings;
import org.workcraft.plugins.punf.PunfUtilitySettings;
import org.workcraft.plugins.shared.tasks.ExternalProcessResult;
import org.workcraft.plugins.shared.tasks.ExternalProcessTask;
import org.workcraft.tasks.ProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.tasks.Task;
import org.workcraft.util.FileUtils;

public class MpsatSynthesisTask implements Task<ExternalProcessResult> {
	public static final String OUTPUT_FILE_NAME = "mpsat.v";
	private final String[] args;
	private final String inputFileName;
	private final File directory;
	private final boolean tryPnml;

	public MpsatSynthesisTask(String[] args, String inputFileName, File directory, boolean tryPnml) {
		this.args = args;
		this.inputFileName = inputFileName;
		if (directory == null) {
			// Prefix must be at least 3 symbols long.
			directory = FileUtils.createTempDirectory("mpsat-");
		}
		this.directory = directory;
		this.tryPnml = tryPnml;
	}

	@Override
	public Result<? extends ExternalProcessResult> run(ProgressMonitor<? super ExternalProcessResult> monitor) {

		ArrayList<String> command = new ArrayList<>();
		// Name of the executable
		command.add(MpsatSynthesisUtilitySettings.getCommand() + PunfUtilitySettings.getCommandSuffix(tryPnml));
		// Built-in arguments
		for (String arg : args) {
			command.add(arg);
		}
		// Extra arguments
		for (String arg : MpsatSynthesisUtilitySettings.getExtraArgs().split("\\s")) {
			if (!arg.isEmpty()) {
				command.add(arg);
			}
		}
		// Technology mapping library (if needed and accepted)
		String gateLibrary = CircuitSettings.getGateLibrary();
		if ((gateLibrary != null) && !gateLibrary.isEmpty()) {
			if (tryPnml && PunfUtilitySettings.getUsePnmlUnfolding()) {
				File gateLibraryFile = new File(gateLibrary);
				if (gateLibraryFile.exists()) {
					command.add("-d");
					command.add(gateLibraryFile.getAbsolutePath());
				}
			}
		}
		// Input file argument
		command.add(inputFileName);
		// Output file
		File outputFile = new File(directory, OUTPUT_FILE_NAME);
		command.add(outputFile.getAbsolutePath());

		ExternalProcessTask externalProcessTask = new ExternalProcessTask(command, directory);
		Result<? extends ExternalProcessResult> res = externalProcessTask.run(monitor);
		if(res.getOutcome() == Outcome.CANCELLED) {
			return res;
		}

		Map<String, byte[]> outputFiles = new HashMap<String, byte[]>();
		try {
			File g = new File(directory, OUTPUT_FILE_NAME);
			if(g.exists()) {
				outputFiles.put(OUTPUT_FILE_NAME, FileUtils.readAllBytes(g));
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
