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


public class CheckCircuitTask extends MpsatChainTask {
	private final MpsatSettings toolchainPreparationSettings = new MpsatSettings("Toolchain preparation of data",
			MpsatMode.UNDEFINED, 0, null, 0, null);

	private final MpsatSettings toolchainCompletionSettings = new MpsatSettings("Toolchain completion",
			MpsatMode.UNDEFINED, 0, null, 0, null);

	private final MpsatSettings deadlockSettings = new MpsatSettings("Deadlock freedom",
			MpsatMode.DEADLOCK, 0, MpsatUtilitySettings.getSolutionMode(),
			MpsatUtilitySettings.getSolutionCount(), null);

	private final MpsatSettings hazardSettings = new MpsatSettings("Output persistence",
			MpsatMode.STG_REACHABILITY, 0, MpsatUtilitySettings.getSolutionMode(),
			MpsatUtilitySettings.getSolutionCount(), MpsatSettings.reachSemimodularity);

	private final WorkspaceEntry we;
	private final Framework framework;
	private final boolean checkConformation;
	private final boolean checkDeadlock;
	private final boolean checkHazard;

	public CheckCircuitTask(WorkspaceEntry we, Framework framework,
			boolean checkConformation, boolean checkDeadlock, boolean checkHazard) {
		super (we, null, framework);
		this.we = we;
		this.framework = framework;
		this.checkConformation = checkConformation;
		this.checkDeadlock = checkDeadlock;
		this.checkHazard = checkHazard;
	}

	@Override
	public Result<? extends MpsatChainResult> run(ProgressMonitor<? super MpsatChainResult> monitor) {
		try {
			// Common variables
			monitor.progressUpdate(0.05);
			String title = we.getWorkspacePath().getNode();
			if (title.endsWith(".work")) {
				title = title.substring(0, title.length() - 5);
			}
			VisualCircuit visualCircuit = (VisualCircuit)we.getModelEntry().getVisualModel();
			STG circuitStg = (STG)STGGenerator.generate(visualCircuit).getMathModel();
			Exporter stgExporter = Export.chooseBestExporter(framework.getPluginManager(), circuitStg, Format.STG);
			if (stgExporter == null) {
				throw new RuntimeException ("Exporter not available: model class " + circuitStg.getClass().getName() + " to format STG.");
			}
			SubtaskMonitor<Object> subtaskMonitor = new SubtaskMonitor<Object>(monitor);
			monitor.progressUpdate(0.10);

			// Generating .g for the circuit
			File circuitStgFile = File.createTempFile(title + "-circuit-", stgExporter.getExtenstion());
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
			monitor.progressUpdate(0.20);

			// Generating .g for the environment
			File stgFile;
			STG stg;
			Result<? extends ExternalProcessResult>  pcompResult = null;
			File environmentFile = visualCircuit.getEnvironmentFile();
			boolean hasEnvironment = ((environmentFile != null) && environmentFile.exists());
			if ( !hasEnvironment ) {
				 // No environment to compose with
				 stgFile = circuitStgFile;
				 stg = circuitStg;
			} else {
				// Compose circuit with its environment
				File environmentStgFile;
				if (environmentFile.getName().endsWith(".g")) {
					environmentStgFile = environmentFile;
				} else {
					STG environementStg = (STG)framework.loadFile(environmentFile).getMathModel();
					environmentStgFile = File.createTempFile(title + "-environment-", stgExporter.getExtenstion());
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
				monitor.progressUpdate(0.25);

				// Generating .g for the whole system (circuit and environment)
				stgFile = File.createTempFile(title + "-system-", stgExporter.getExtenstion());
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
				WorkspaceEntry stgWorkspaceEntry = framework.getWorkspace().open(stgFile, true);
				stg = (STG)stgWorkspaceEntry.getModelEntry().getMathModel();
			}
			monitor.progressUpdate(0.30);

			// Generate unfolding
			File mciFile = File.createTempFile(title+"-unfolding-", ".mci");
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

			// Check for interface conformation (only if the environment is specified)
			if (hasEnvironment && checkConformation) {
				String reachConformation = MpsatSettings.genReachConformation(stg, circuitStg);
				if (MpsatUtilitySettings.getDebugReach()) {
					System.out.println("\nReach expression for the interface conformation property:");
					System.out.println(reachConformation);
				}
				MpsatSettings conformationSettings = new MpsatSettings("Interface conformation",
						MpsatMode.STG_REACHABILITY, 0, MpsatUtilitySettings.getSolutionMode(),
						MpsatUtilitySettings.getSolutionCount(), reachConformation);

				MpsatTask mpsatConformationTask = new MpsatTask(conformationSettings.getMpsatArguments(), mciFile.getCanonicalPath());
				Result<? extends ExternalProcessResult>  mpsatConformationResult = framework.getTaskManager().execute(
						mpsatConformationTask, "Running conformation check [MPSat]", subtaskMonitor);

				if (mpsatConformationResult.getOutcome() != Outcome.FINISHED) {
					if (mpsatConformationResult.getOutcome() == Outcome.CANCELLED) {
						return new Result<MpsatChainResult>(Outcome.CANCELLED);
					}
					return new Result<MpsatChainResult>(Outcome.FAILED,
							new MpsatChainResult(circuitExportResult, pcompResult, punfResult, mpsatConformationResult, conformationSettings));
				}
				monitor.progressUpdate(0.50);

				MpsatResultParser mpsatConformationParser = new MpsatResultParser(mpsatConformationResult.getReturnValue());
				if (!mpsatConformationParser.getSolutions().isEmpty()) {
					mciFile.delete();
					return new Result<MpsatChainResult>(Outcome.FINISHED,
							new MpsatChainResult(circuitExportResult, pcompResult, punfResult, mpsatConformationResult, conformationSettings,
									"Circuit does not conform to the environment after the following trace:"));
				}
			}
			monitor.progressUpdate(0.60);

			// Check for deadlock
			if (checkDeadlock) {
				MpsatTask mpsatDeadlockTask = new MpsatTask(deadlockSettings.getMpsatArguments(), mciFile.getCanonicalPath());
				Result<? extends ExternalProcessResult> mpsatDeadlockResult = framework.getTaskManager().execute(
						mpsatDeadlockTask, "Running deadlock check [MPSat]", subtaskMonitor);

				if (mpsatDeadlockResult.getOutcome() != Outcome.FINISHED) {
					if (mpsatDeadlockResult.getOutcome() == Outcome.CANCELLED) {
						return new Result<MpsatChainResult>(Outcome.CANCELLED);
					}
					return new Result<MpsatChainResult>(Outcome.FAILED,
							new MpsatChainResult(circuitExportResult, pcompResult, punfResult, mpsatDeadlockResult, deadlockSettings));
				}
				monitor.progressUpdate(0.70);

				MpsatResultParser mpsatDeadlockParser = new MpsatResultParser(mpsatDeadlockResult.getReturnValue());
				if (!mpsatDeadlockParser.getSolutions().isEmpty()) {
					mciFile.delete();
					return new Result<MpsatChainResult>(Outcome.FINISHED,
							new MpsatChainResult(circuitExportResult, pcompResult, punfResult, mpsatDeadlockResult, deadlockSettings,
									"Circuit has a deadlock after the following trace:"));
				}
			}
			monitor.progressUpdate(0.80);

			// Check for hazards
			if (checkHazard) {
				MpsatTask mpsatHazardTask = new MpsatTask(hazardSettings.getMpsatArguments(), mciFile.getCanonicalPath());
				if (MpsatUtilitySettings.getDebugReach()) {
					System.out.println("\nReach expression for the hazard property:");
					System.out.println(hazardSettings.getReach());
				}
				Result<? extends ExternalProcessResult>  mpsatHazardResult = framework.getTaskManager().execute(
						mpsatHazardTask, "Running hazard check [MPSat]", subtaskMonitor);

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
							new MpsatChainResult(circuitExportResult, pcompResult, punfResult, mpsatHazardResult, hazardSettings,
									"Circuit has a hazard  after the following trace:"));
				}
			}
			monitor.progressUpdate(1.0);

			// Success
			mciFile.delete();
			String message = "";
			if (hasEnvironment) {
				message = "Under the given environment (" + environmentFile.getName() + ")";
			} else {
				message = "Without environment restrictions";
			}
			message +=  " the circuit is:\n";
			if (checkConformation) {
				message += "  * conformant\n";
			}
			if (checkDeadlock) {
				message += "  * deadlock-free\n";
			}
			if (checkHazard) {
				message += "  * hazard-free\n";
			}
			return new Result<MpsatChainResult>(Outcome.FINISHED,
					new MpsatChainResult(circuitExportResult, pcompResult, punfResult, null, toolchainCompletionSettings, message));

		} catch (Throwable e) {
			return new Result<MpsatChainResult>(e);
		}
	}

}
