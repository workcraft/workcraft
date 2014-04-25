package org.workcraft.plugins.circuit.tasks;

import java.io.File;

import org.workcraft.Framework;
import org.workcraft.interop.Exporter;
import org.workcraft.plugins.circuit.VisualCircuit;
import org.workcraft.plugins.circuit.tools.STGGenerator;
import org.workcraft.plugins.mpsat.MpsatMode;
import org.workcraft.plugins.mpsat.MpsatResultParser;
import org.workcraft.plugins.mpsat.MpsatSettings;
import org.workcraft.plugins.mpsat.MpsatUtilitySettings;
import org.workcraft.plugins.mpsat.tasks.MpsatChainResult;
import org.workcraft.plugins.mpsat.tasks.MpsatChainTask;
import org.workcraft.plugins.mpsat.tasks.MpsatTask;
import org.workcraft.plugins.mpsat.tasks.PunfTask;
import org.workcraft.plugins.pcomp.PCompOutputMode;
import org.workcraft.plugins.pcomp.tasks.PcompTask;
import org.workcraft.plugins.shared.tasks.ExternalProcessResult;
import org.workcraft.plugins.stg.STGModel;
import org.workcraft.serialisation.Format;
import org.workcraft.tasks.ProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.tasks.SubtaskMonitor;
import org.workcraft.util.Export;
import org.workcraft.util.Export.ExportTask;
import org.workcraft.util.FileUtils;
import org.workcraft.workspace.WorkspaceEntry;


public class CheckConformanceTask extends MpsatChainTask {
	private final MpsatSettings toolchainPreparationSettings = new MpsatSettings("Toolchain preparation of data",
			MpsatMode.UNDEFINED, 0, null, 0, null);

	private final MpsatSettings toolchainCompletionSettings = new MpsatSettings("Toolchain completion",
			MpsatMode.UNDEFINED, 0, null, 0, null);

	private final MpsatSettings conformanceSettings = new MpsatSettings("Conformance with environment",
			MpsatMode.UNDEFINED, 0, null, 0, null);

	private final MpsatSettings deadlockSettings = new MpsatSettings("Deadlock freedom",
			MpsatMode.DEADLOCK, 0, MpsatUtilitySettings.getSolutionMode(),
			MpsatUtilitySettings.getSolutionCount(), null);

	private final MpsatSettings hazardSettings = new MpsatSettings("Output persistence",
			MpsatMode.STG_REACHABILITY, 0, MpsatUtilitySettings.getSolutionMode(),
			MpsatUtilitySettings.getSolutionCount(), MpsatSettings.reachSemimodularity);

	private final WorkspaceEntry we;
	private final Framework framework;

	public CheckConformanceTask(WorkspaceEntry we, Framework framework) {
		super (we, null, framework);
		this.we = we;
		this.framework = framework;
		}

	@Override
	public Result<? extends MpsatChainResult> run(ProgressMonitor<? super MpsatChainResult> monitor) {
		try {
			// Common variables
			monitor.progressUpdate(0.05);
			VisualCircuit visualCircuit = (VisualCircuit)we.getModelEntry().getVisualModel();
			STGModel circuitStg = (STGModel)STGGenerator.generate(visualCircuit).getMathModel();
			Exporter stgExporter = Export.chooseBestExporter(framework.getPluginManager(), circuitStg, Format.STG);
			if (stgExporter == null) {
				throw new RuntimeException ("Exporter not available: model class " + circuitStg.getClass().getName() + " to format STG.");
			}
			SubtaskMonitor<Object> subtaskMonitor = new SubtaskMonitor<Object>(monitor);
			monitor.progressUpdate(0.10);

			// Generating .g for the circuit
			File circuitStgFile = File.createTempFile("circuit", stgExporter.getExtenstion());
			ExportTask circuitExportTask = new ExportTask(stgExporter, circuitStg, circuitStgFile.getCanonicalPath());
			Result<? extends Object> circuitExportResult = framework.getTaskManager().execute(
					circuitExportTask, "Exporting circuit .g", subtaskMonitor);

			if (circuitExportResult.getOutcome() != Outcome.FINISHED) {
				circuitStgFile.delete();
				if (circuitExportResult.getOutcome() == Outcome.CANCELLED) {
					return new Result<MpsatChainResult>(Outcome.CANCELLED);
				}
				return new Result<MpsatChainResult>(Outcome.FAILED,
						new MpsatChainResult(circuitExportResult, null, null, null, toolchainPreparationSettings));
			}
			monitor.progressUpdate(0.15);

			// Generating .g for the environment
			File stgFile;
			Result<? extends ExternalProcessResult>  pcompResult = null;
			File environmentFile = visualCircuit.getEnvironmentFile();
			if ( !environmentFile.exists() ) {
				 // No environment to compose with
				 stgFile = circuitStgFile;
			} else {
				// Compose circuit with its environment
				File environmentStgFile;
				if (environmentFile.getName().endsWith(".g")) {
					environmentStgFile = environmentFile;
				} else {
					STGModel environementStg = (STGModel)framework.loadFile(environmentFile).getMathModel();
					environmentStgFile = File.createTempFile("environment", stgExporter.getExtenstion());
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
				//framework.getMainWindow().createEditorWindow(framework.getWorkspace().open(environmentStgFile, true));
				monitor.progressUpdate(0.20);

				// Generating .g for the whole system (circuit and environment)
				stgFile = File.createTempFile("system", stgExporter.getExtenstion());
				PcompTask pcompTask = new PcompTask(new File[]{circuitStgFile, environmentStgFile}, PCompOutputMode.OUTPUT, false);
				pcompResult = framework.getTaskManager().execute(
						pcompTask, "Running pcomp", subtaskMonitor);

				if (pcompResult.getOutcome() != Outcome.FINISHED) {
					circuitStgFile.delete();
					if (pcompResult.getOutcome() == Outcome.CANCELLED) {
						return new Result<MpsatChainResult>(Outcome.CANCELLED);
					}
					return new Result<MpsatChainResult>(Outcome.FAILED,
							new MpsatChainResult(circuitExportResult, pcompResult, null, null, toolchainPreparationSettings));
				}
				FileUtils.writeAllText(stgFile, new String(pcompResult.getReturnValue().getOutput()));
				framework.getMainWindow().createEditorWindow(framework.getWorkspace().open(stgFile, true));
			}
			monitor.progressUpdate(0.30);

			// Generate unfolding
			File mciFile = File.createTempFile("unfolding", ".mci");
			PunfTask punfTask = new PunfTask(stgFile.getCanonicalPath(), mciFile.getCanonicalPath());
			Result<? extends ExternalProcessResult> punfResult = framework.getTaskManager().execute(
					punfTask, "Unfolding .g", subtaskMonitor);

			stgFile.delete();
			if (punfResult.getOutcome() != Outcome.FINISHED) {
				mciFile.delete();
				if (punfResult.getOutcome() == Outcome.CANCELLED) {
					return new Result<MpsatChainResult>(Outcome.CANCELLED);
				}
				return new Result<MpsatChainResult>(Outcome.FAILED,
						new MpsatChainResult(circuitExportResult, pcompResult, punfResult, null, toolchainPreparationSettings));
			}
			monitor.progressUpdate(0.40);

			// Check for deadlock
			MpsatTask mpsatDeadlockTask = new MpsatTask(deadlockSettings.getMpsatArguments(), mciFile.getCanonicalPath());
			Result<? extends ExternalProcessResult> mpsatDeadlockResult = framework.getTaskManager().execute(
					mpsatDeadlockTask, "Running deadlock checking [MPSat]", subtaskMonitor);

			if (mpsatDeadlockResult.getOutcome() != Outcome.FINISHED) {
				if (mpsatDeadlockResult.getOutcome() == Outcome.CANCELLED) {
					return new Result<MpsatChainResult>(Outcome.CANCELLED);
				}
				return new Result<MpsatChainResult>(Outcome.FAILED,
						new MpsatChainResult(circuitExportResult, pcompResult, punfResult, mpsatDeadlockResult, deadlockSettings));
			}
			monitor.progressUpdate(0.60);

			MpsatResultParser mpsatDeadlockParser = new MpsatResultParser(mpsatDeadlockResult.getReturnValue());
			if (!mpsatDeadlockParser.getSolutions().isEmpty()) {
				mciFile.delete();
				return new Result<MpsatChainResult>(Outcome.FINISHED,
						new MpsatChainResult(circuitExportResult, pcompResult, punfResult, mpsatDeadlockResult, deadlockSettings, "Circuit has a deadlock"));
			}
			monitor.progressUpdate(0.70);

			// Check for hazards
			MpsatTask mpsatHazardTask = new MpsatTask(hazardSettings.getMpsatArguments(), mciFile.getCanonicalPath());
			Result<? extends ExternalProcessResult>  mpsatHazardResult = framework.getTaskManager().execute(
					mpsatHazardTask, "Running semimodularity checking [MPSat]", subtaskMonitor);

			if (mpsatHazardResult.getOutcome() != Outcome.FINISHED) {
				if (mpsatHazardResult.getOutcome() == Outcome.CANCELLED) {
					return new Result<MpsatChainResult>(Outcome.CANCELLED);
				}
				return new Result<MpsatChainResult>(Outcome.FAILED,
						new MpsatChainResult(circuitExportResult, pcompResult, punfResult, mpsatHazardResult, hazardSettings));
			}
			monitor.progressUpdate(0.90);

			MpsatResultParser mpsatHazardParser = new MpsatResultParser(mpsatHazardResult.getReturnValue());
			if (!mpsatHazardParser.getSolutions().isEmpty()) {
				mciFile.delete();
				return new Result<MpsatChainResult>(Outcome.FINISHED,
						new MpsatChainResult(circuitExportResult, pcompResult, punfResult, mpsatHazardResult, hazardSettings, "Circuit has hazard(s)"));
			}
			monitor.progressUpdate(1.0);

			// Success
			mciFile.delete();
			String message = "Circuit is deadlock-free and hazard-free";
			if (environmentFile.exists()) {
				message += " under the given environment (" + environmentFile.getName() + ")";
			}
			return new Result<MpsatChainResult>(Outcome.FINISHED,
					new MpsatChainResult(circuitExportResult, pcompResult, punfResult, null, toolchainCompletionSettings, message));

		} catch (Throwable e) {
			return new Result<MpsatChainResult>(e);
		}
	}

}
