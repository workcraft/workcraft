package org.workcraft.plugins.mpsat.tasks;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

import org.workcraft.Framework;
import org.workcraft.interop.Exporter;
import org.workcraft.plugins.mpsat.MpsatMode;
import org.workcraft.plugins.mpsat.MpsatResultParser;
import org.workcraft.plugins.mpsat.MpsatSettings;
import org.workcraft.plugins.mpsat.MpsatUtilitySettings;
import org.workcraft.plugins.pcomp.tasks.PcompTask;
import org.workcraft.plugins.pcomp.tasks.PcompTask.ConversionMode;
import org.workcraft.plugins.shared.tasks.ExternalProcessResult;
import org.workcraft.plugins.stg.STG;
import org.workcraft.plugins.stg.SignalTransition.Type;
import org.workcraft.serialisation.Format;
import org.workcraft.tasks.ProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.tasks.SubtaskMonitor;
import org.workcraft.util.Export;
import org.workcraft.util.Export.ExportTask;
import org.workcraft.util.FileUtils;
import org.workcraft.workspace.WorkspaceEntry;


public class MpsatConformationTask extends MpsatChainTask {
	private final MpsatSettings toolchainPreparationSettings = new MpsatSettings("Toolchain preparation of data",
			MpsatMode.UNDEFINED, 0, null, 0, null);

	private final MpsatSettings toolchainCompletionSettings = new MpsatSettings("Toolchain completion",
			MpsatMode.UNDEFINED, 0, null, 0, null);

	private final WorkspaceEntry we;
	private File envFile;

	public MpsatConformationTask(WorkspaceEntry we, File envFile) {
		super (we, null);
		this.we = we;
		this.envFile = envFile;
	}

	@Override
	public Result<? extends MpsatChainResult> run(ProgressMonitor<? super MpsatChainResult> monitor) {
		Framework framework = Framework.getInstance();
		File workingDirectory = null;
		try {
			// Common variables
			monitor.progressUpdate(0.10);
			String title = we.getTitle();
			String prefix = "workcraft-" + title + "-"; // Prefix must be at least 3 symbols long.
			workingDirectory = FileUtils.createTempDirectory(prefix);

			STG devStg = (STG)we.getModelEntry().getVisualModel().getMathModel();
			Exporter devStgExporter = Export.chooseBestExporter(framework.getPluginManager(), devStg, Format.STG);
			if (devStgExporter == null) {
				throw new RuntimeException ("Exporter not available: model class " + devStg.getClass().getName() + " to format STG.");
			}
			SubtaskMonitor<Object> subtaskMonitor = new SubtaskMonitor<Object>(monitor);
			monitor.progressUpdate(0.20);

			// Generating .g for the model
			File devStgFile =  new File(workingDirectory, "dev.g");
			ExportTask devExportTask = new ExportTask(devStgExporter, devStg, devStgFile.getCanonicalPath());
			Result<? extends Object> devExportResult = framework.getTaskManager().execute(
					devExportTask, "Exporting circuit .g", subtaskMonitor);

			if (devExportResult.getOutcome() != Outcome.FINISHED) {
				if (devExportResult.getOutcome() == Outcome.CANCELLED) {
					return new Result<MpsatChainResult>(Outcome.CANCELLED);
				}
				return new Result<MpsatChainResult>(Outcome.FAILED,
						new MpsatChainResult(devExportResult, null, null, null, toolchainPreparationSettings));
			}
			monitor.progressUpdate(0.30);

			// Generating .g for the environment
			Result<? extends ExternalProcessResult>  pcompResult = null;
			File envStgFile = null;
			if (envFile.getName().endsWith(".g")) {
				envStgFile = envFile;
			} else {
				STG envStg = (STG)framework.loadFile(envFile).getMathModel();
				Exporter envStgExporter = Export.chooseBestExporter(framework.getPluginManager(), envStg, Format.STG);
				envStgFile = new File(workingDirectory, "env.g");
				ExportTask envExportTask = new ExportTask(envStgExporter, envStg, envStgFile.getCanonicalPath());
				Result<? extends Object> envExportResult = framework.getTaskManager().execute(
						envExportTask, "Exporting environment .g", subtaskMonitor);

				if (envExportResult.getOutcome() != Outcome.FINISHED) {
					if (envExportResult.getOutcome() == Outcome.CANCELLED) {
						return new Result<MpsatChainResult>(Outcome.CANCELLED);
					}
					return new Result<MpsatChainResult>(Outcome.FAILED,
							new MpsatChainResult(envExportResult, null, null, null, toolchainPreparationSettings));
				}
			}
			monitor.progressUpdate(0.40);

			// Generating .g for the whole system (model and environment)
			File stgFile = new File(workingDirectory, "system.g");
			PcompTask pcompTask = new PcompTask(new File[]{devStgFile, envStgFile}, ConversionMode.OUTPUT, true, false, workingDirectory);
			pcompResult = framework.getTaskManager().execute(
					pcompTask, "Running pcomp", subtaskMonitor);

			if (pcompResult.getOutcome() != Outcome.FINISHED) {
				if (pcompResult.getOutcome() == Outcome.CANCELLED) {
					return new Result<MpsatChainResult>(Outcome.CANCELLED);
				}
				return new Result<MpsatChainResult>(Outcome.FAILED,
						new MpsatChainResult(devExportResult, pcompResult, null, null, toolchainPreparationSettings));
			}
			FileUtils.writeAllText(stgFile, new String(pcompResult.getReturnValue().getOutput()));
			WorkspaceEntry stgWorkspaceEntry = framework.getWorkspace().open(stgFile, true);
			STG stg = (STG)stgWorkspaceEntry.getModelEntry().getMathModel();
			framework.getWorkspace().close(stgWorkspaceEntry);
			monitor.progressUpdate(0.50);

			// Generate unfolding
			File unfoldingFile = new File(workingDirectory, "system" + MpsatUtilitySettings.getUnfoldingExtension(true));
			PunfTask punfTask = new PunfTask(stgFile.getCanonicalPath(), unfoldingFile.getCanonicalPath(), true);
			Result<? extends ExternalProcessResult> punfResult = framework.getTaskManager().execute(
					punfTask, "Unfolding .g", subtaskMonitor);

			if (punfResult.getOutcome() != Outcome.FINISHED) {
				if (punfResult.getOutcome() == Outcome.CANCELLED) {
					return new Result<MpsatChainResult>(Outcome.CANCELLED);
				}
				return new Result<MpsatChainResult>(Outcome.FAILED,
						new MpsatChainResult(devExportResult, pcompResult, punfResult, null, toolchainPreparationSettings));
			}
			monitor.progressUpdate(0.60);

			// Check for interface conformation
			Set<String> devOutputNames = devStg.getSignalFlatNames(Type.OUTPUT);
			Set<String> devPlaceNames = parsePlaceNames(pcompResult.getReturnValue().getOutputFile("places.list"), 0);
//			String reachConformation = MpsatSettings.genReachConformation(devOutputNames, devPlaceNames);
			String reachConformation = MpsatSettings.genReachConformationDetail(stg, devOutputNames, devPlaceNames);
			if (MpsatUtilitySettings.getDebugReach()) {
				System.out.println("\nReach expression for the interface conformation property:");
				System.out.println(reachConformation);
			}
			MpsatSettings conformationSettings = new MpsatSettings("Interface conformation",
					MpsatMode.STG_REACHABILITY, 0, MpsatUtilitySettings.getSolutionMode(),
					MpsatUtilitySettings.getSolutionCount(), reachConformation);

			MpsatTask mpsatConformationTask = new MpsatTask(conformationSettings.getMpsatArguments(),
					unfoldingFile.getCanonicalPath(), workingDirectory, true);
			Result<? extends ExternalProcessResult>  mpsatConformationResult = framework.getTaskManager().execute(
					mpsatConformationTask, "Running conformation check [MPSat]", subtaskMonitor);

			if (mpsatConformationResult.getOutcome() != Outcome.FINISHED) {
				if (mpsatConformationResult.getOutcome() == Outcome.CANCELLED) {
					return new Result<MpsatChainResult>(Outcome.CANCELLED);
				}
				return new Result<MpsatChainResult>(Outcome.FAILED,
						new MpsatChainResult(devExportResult, pcompResult, punfResult, mpsatConformationResult, conformationSettings));
			}
			monitor.progressUpdate(0.80);

			MpsatResultParser mpsatConformationParser = new MpsatResultParser(mpsatConformationResult.getReturnValue());
			if (!mpsatConformationParser.getSolutions().isEmpty()) {
				return new Result<MpsatChainResult>(Outcome.FINISHED,
						new MpsatChainResult(devExportResult, pcompResult, punfResult, mpsatConformationResult, conformationSettings,
								"This model does not conform to the environment after the following trace:"));
			}
			monitor.progressUpdate(1.0);

			// Success
			unfoldingFile.delete();
			String message = "The model conforms to its environment (" + envFile.getName() + ").";
			return new Result<MpsatChainResult>(Outcome.FINISHED,
					new MpsatChainResult(devExportResult, pcompResult, punfResult, null, toolchainCompletionSettings, message));

		} catch (Throwable e) {
			return new Result<MpsatChainResult>(e);
		} finally {
			FileUtils.deleteFile(workingDirectory, MpsatUtilitySettings.getDebugTemporaryFiles());
		}
	}

	private HashSet<String> parsePlaceNames(byte[] bufferedInput, int lineIndex) {
		HashSet<String> result = new HashSet<String>();
		InputStream is = new ByteArrayInputStream(bufferedInput);
	    BufferedReader br = new BufferedReader(new InputStreamReader(is));
		try {
			String line = null;
			while ((lineIndex >= 0) && ((line = br.readLine()) != null)) {
				lineIndex--;
			}
			if (line != null) {
				for (String name: line.trim().split("\\s")) {
					result.add(name);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}


}
