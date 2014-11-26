package org.workcraft.plugins.mpsat.tasks;

import java.io.File;

import org.workcraft.Framework;
import org.workcraft.interop.Exporter;
import org.workcraft.plugins.mpsat.MpsatMode;
import org.workcraft.plugins.mpsat.MpsatResultParser;
import org.workcraft.plugins.mpsat.MpsatSettings;
import org.workcraft.plugins.mpsat.MpsatUtilitySettings;
import org.workcraft.plugins.pcomp.PCompOutputMode;
import org.workcraft.plugins.pcomp.tasks.PcompTask;
import org.workcraft.plugins.shared.tasks.ExternalProcessResult;
import org.workcraft.plugins.stg.STG;
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
	private final Framework framework;
	private File environmentFile;

	public MpsatConformationTask(WorkspaceEntry we, Framework framework, File environmentFile) {
		super (we, null, framework);
		this.we = we;
		this.framework = framework;
		this.environmentFile = environmentFile;
	}

	@Override
	public Result<? extends MpsatChainResult> run(ProgressMonitor<? super MpsatChainResult> monitor) {
		try {
			// Common variables
			monitor.progressUpdate(0.10);
			String title = we.getWorkspacePath().getNode();
			if (title.endsWith(".work")) {
				title = title.substring(0, title.length() - 5);
			}
			STG modelStg = (STG)we.getModelEntry().getVisualModel().getMathModel();
			Exporter stgExporter = Export.chooseBestExporter(framework.getPluginManager(), modelStg, Format.STG);
			if (stgExporter == null) {
				throw new RuntimeException ("Exporter not available: model class " + modelStg.getClass().getName() + " to format STG.");
			}
			SubtaskMonitor<Object> subtaskMonitor = new SubtaskMonitor<Object>(monitor);
			monitor.progressUpdate(0.20);

			// Generating .g for the model
			String modelFilePrefix = title + "-model-";
			File modelStgFile = File.createTempFile(modelFilePrefix.replaceAll("\\s",""), stgExporter.getExtenstion());
			ExportTask modelExportTask = new ExportTask(stgExporter, modelStg, modelStgFile.getCanonicalPath());
			Result<? extends Object> modelExportResult = framework.getTaskManager().execute(
					modelExportTask, "Exporting model .g", subtaskMonitor);

			if (modelExportResult.getOutcome() != Outcome.FINISHED) {
				modelStgFile.delete();
				if (modelExportResult.getOutcome() == Outcome.CANCELLED) {
					return new Result<MpsatChainResult>(Outcome.CANCELLED);
				}
				return new Result<MpsatChainResult>(Outcome.FAILED,
						new MpsatChainResult(modelExportResult, null, null, null, toolchainPreparationSettings));
			}
			monitor.progressUpdate(0.30);

			// Generating .g for the environment
			Result<? extends ExternalProcessResult>  pcompResult = null;
			// Compose model with its environment
			File environmentStgFile;
			if (environmentFile.getName().endsWith(".g")) {
				environmentStgFile = environmentFile;
			} else {
				STG environementStg = (STG)framework.loadFile(environmentFile).getMathModel();
				String environmentFilePrefix = title + "-environment-";
				environmentStgFile = File.createTempFile(environmentFilePrefix.replaceAll("\\s",""), stgExporter.getExtenstion());
				ExportTask environmentExportTask = new ExportTask(stgExporter, environementStg, environmentStgFile.getCanonicalPath());
				Result<? extends Object> environmentExportResult = framework.getTaskManager().execute(
						environmentExportTask, "Exporting environment .g", subtaskMonitor);

				if (environmentExportResult.getOutcome() != Outcome.FINISHED) {
					environmentStgFile.delete();
					if (environmentExportResult.getOutcome() == Outcome.CANCELLED) {
						return new Result<MpsatChainResult>(Outcome.CANCELLED);
					}
					return new Result<MpsatChainResult>(Outcome.FAILED,
							new MpsatChainResult(environmentExportResult, null, null, null, toolchainPreparationSettings));
				}
			}
			monitor.progressUpdate(0.40);

			// Generating .g for the whole system (model and environment)
			String systemFilePrefix = title + "-system-";
			File systemFile = File.createTempFile(systemFilePrefix.replaceAll("\\s",""), stgExporter.getExtenstion());
			PcompTask pcompTask = new PcompTask(new File[]{modelStgFile, environmentStgFile}, PCompOutputMode.OUTPUT, true, false);
			pcompResult = framework.getTaskManager().execute(
					pcompTask, "Running pcomp", subtaskMonitor);

			if (pcompResult.getOutcome() != Outcome.FINISHED) {
				modelStgFile.delete();
				if (pcompResult.getOutcome() == Outcome.CANCELLED) {
					return new Result<MpsatChainResult>(Outcome.CANCELLED);
				}
				return new Result<MpsatChainResult>(Outcome.FAILED,
						new MpsatChainResult(modelExportResult, pcompResult, null, null, toolchainPreparationSettings));
			}
			FileUtils.writeAllText(systemFile, new String(pcompResult.getReturnValue().getOutput()));
			WorkspaceEntry systemWorkspaceEntry = framework.getWorkspace().open(systemFile, true);
			STG systemStg = (STG)systemWorkspaceEntry.getModelEntry().getMathModel();

			monitor.progressUpdate(0.50);

			// Generate unfolding
			String unfoldingFilePrefix = title + "-unfolding-";
			File unfoldingFile = File.createTempFile(unfoldingFilePrefix.replaceAll("\\s",""), MpsatUtilitySettings.getUnfoldingExtension());
			PunfTask punfTask = new PunfTask(systemFile.getCanonicalPath(), unfoldingFile.getCanonicalPath());
			Result<? extends ExternalProcessResult> punfResult = framework.getTaskManager().execute(
					punfTask, "Unfolding .g", subtaskMonitor);

			systemFile.delete();
			if (punfResult.getOutcome() != Outcome.FINISHED) {
				unfoldingFile.delete();
				if (punfResult.getOutcome() == Outcome.CANCELLED) {
					return new Result<MpsatChainResult>(Outcome.CANCELLED);
				}
				return new Result<MpsatChainResult>(Outcome.FAILED,
						new MpsatChainResult(modelExportResult, pcompResult, punfResult, null, toolchainPreparationSettings));
			}
			monitor.progressUpdate(0.60);

			// Check for interface conformation
			String reachConformation = MpsatSettings.genReachConformation(systemStg, modelStg);
			if (MpsatUtilitySettings.getDebugReach()) {
				System.out.println("\nReach expression for the interface conformation property:");
				System.out.println(reachConformation);
			}
			MpsatSettings conformationSettings = new MpsatSettings("Interface conformation",
					MpsatMode.STG_REACHABILITY, 0, MpsatUtilitySettings.getSolutionMode(),
					MpsatUtilitySettings.getSolutionCount(), reachConformation);

			MpsatTask mpsatConformationTask = new MpsatTask(conformationSettings.getMpsatArguments(), unfoldingFile.getCanonicalPath());
			Result<? extends ExternalProcessResult>  mpsatConformationResult = framework.getTaskManager().execute(
					mpsatConformationTask, "Running conformation check [MPSat]", subtaskMonitor);

			if (mpsatConformationResult.getOutcome() != Outcome.FINISHED) {
				if (mpsatConformationResult.getOutcome() == Outcome.CANCELLED) {
					return new Result<MpsatChainResult>(Outcome.CANCELLED);
				}
				return new Result<MpsatChainResult>(Outcome.FAILED,
						new MpsatChainResult(modelExportResult, pcompResult, punfResult, mpsatConformationResult, conformationSettings));
			}
			monitor.progressUpdate(0.80);

			MpsatResultParser mpsatConformationParser = new MpsatResultParser(mpsatConformationResult.getReturnValue());
			if (!mpsatConformationParser.getSolutions().isEmpty()) {
				unfoldingFile.delete();
				return new Result<MpsatChainResult>(Outcome.FINISHED,
						new MpsatChainResult(modelExportResult, pcompResult, punfResult, mpsatConformationResult, conformationSettings,
								"Model does not conform to the environment after the following trace:"));
			}

			monitor.progressUpdate(1.0);

			// Success
			unfoldingFile.delete();
			String message = "The model conforms to its environment (" + environmentFile.getName() + ").";
			return new Result<MpsatChainResult>(Outcome.FINISHED,
					new MpsatChainResult(modelExportResult, pcompResult, punfResult, null, toolchainCompletionSettings, message));

		} catch (Throwable e) {
			return new Result<MpsatChainResult>(e);
		}
	}

}
