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
	private final MpsatSettings deadlockSettings;
	private final MpsatSettings hazardSettings;
	private final WorkspaceEntry we;
	private final Framework framework;

	public CheckConformanceTask(WorkspaceEntry we, Framework framework) {
		super (we, null, framework);
		this.we = we;
		this.framework = framework;

		this.deadlockSettings = new MpsatSettings("Deadlock freedom", MpsatMode.DEADLOCK, 0,
				MpsatUtilitySettings.getSolutionMode(), MpsatUtilitySettings.getSolutionCount(), null);

		this.hazardSettings = new MpsatSettings("Output persistence", MpsatMode.STG_REACHABILITY, 0,
				MpsatUtilitySettings.getSolutionMode(), MpsatUtilitySettings.getSolutionCount(),
				MpsatSettings.reachSemimodularity);
	}

	@Override
	public Result<? extends MpsatChainResult> run(ProgressMonitor<? super MpsatChainResult> monitor) {
		try {
			monitor.progressUpdate(0.05);
			VisualCircuit circuit = (VisualCircuit) we.getModelEntry().getVisualModel();
			STGModel circuitStg = (STGModel) STGGenerator.generate(circuit).getMathModel();
			monitor.progressUpdate(0.10);

			Exporter exporter = Export.chooseBestExporter(framework.getPluginManager(), circuitStg, Format.STG);
			if (exporter == null) {
				throw new RuntimeException ("Exporter not available: model class " + circuitStg.getClass().getName() + " to format STG.");
			}

			SubtaskMonitor<Object> mon = new SubtaskMonitor<Object>(monitor);

			// Generating .g for the circuit
			File circuitStgFile = File.createTempFile("circuit", exporter.getExtenstion());
			ExportTask circuitExportTask = new ExportTask(exporter, circuitStg, circuitStgFile.getCanonicalPath());
			Result<? extends Object> circuitExportResult = framework.getTaskManager().execute(circuitExportTask, "Exporting circuit .g", mon);
			if (circuitExportResult.getOutcome() != Outcome.FINISHED) {
				circuitStgFile.delete();
				if (circuitExportResult.getOutcome() == Outcome.CANCELLED) {
					return new Result<MpsatChainResult>(Outcome.CANCELLED);
				}
				return new Result<MpsatChainResult>(Outcome.FAILED,
						new MpsatChainResult(circuitExportResult, null, null, deadlockSettings));
			}
			monitor.progressUpdate(0.15);

			// Generating .g for the environment
			File stgFile;
			Result<? extends Object> exportResult = circuitExportResult;
			File environmentFile = circuit.getEnvironmentFile();
			if ( !environmentFile.exists() ) {
				 stgFile = circuitStgFile;
			} else {
				File environmentStgFile;
				if (environmentFile.getName().endsWith(".g")) {
					environmentStgFile = environmentFile;
				} else {
					STGModel environementStg = (STGModel)framework.loadFile(environmentFile).getMathModel();
					environmentStgFile = File.createTempFile("environment", exporter.getExtenstion());
					ExportTask environmentExportTask = new ExportTask(exporter, environementStg, environmentStgFile.getCanonicalPath());
					Result<? extends Object> environmentExportResult = framework.getTaskManager().execute(environmentExportTask, "Exporting environment .g", mon);
					if (environmentExportResult.getOutcome() != Outcome.FINISHED) {
						environmentStgFile.delete();
						if (environmentExportResult.getOutcome() == Outcome.CANCELLED) {
							return new Result<MpsatChainResult>(Outcome.CANCELLED);
						}
						return new Result<MpsatChainResult>(Outcome.FAILED,
								new MpsatChainResult(environmentExportResult, null, null, deadlockSettings));
					}
				}
				//framework.getMainWindow().createEditorWindow(framework.getWorkspace().open(environmentStgFile, true));
				monitor.progressUpdate(0.20);

				// Generating .g for the whole system (circuit and environment)
				stgFile = File.createTempFile("system", exporter.getExtenstion());
				PcompTask pcompTask = new PcompTask(new File[]{circuitStgFile, environmentStgFile}, PCompOutputMode.OUTPUT, false);
				Result<? extends ExternalProcessResult>  pcompResult = framework.getTaskManager().execute(pcompTask, "Running pcomp", mon);
				if (pcompResult.getOutcome() != Outcome.FINISHED) {
					circuitStgFile.delete();
					if (pcompResult.getOutcome() == Outcome.CANCELLED) {
						return new Result<MpsatChainResult>(Outcome.CANCELLED);
					}
					return new Result<MpsatChainResult>(Outcome.FAILED,
							new MpsatChainResult(pcompResult, null, null, deadlockSettings));
				}
				FileUtils.writeAllText(stgFile, new String(pcompResult.getReturnValue().getOutput()));
				//framework.getMainWindow().createEditorWindow(framework.getWorkspace().open(stgFile, true));
				exportResult = pcompResult;
			}
			monitor.progressUpdate(0.30);

			// Generate unfolding
			File mciFile = File.createTempFile("unfolding", ".mci");
			PunfTask punfTask = new PunfTask(stgFile.getCanonicalPath(), mciFile.getCanonicalPath());
			Result<? extends ExternalProcessResult> punfResult = framework.getTaskManager().execute(punfTask, "Unfolding .g", mon);
			stgFile.delete();
			if (punfResult.getOutcome() != Outcome.FINISHED) {
				mciFile.delete();
				if (punfResult.getOutcome() == Outcome.CANCELLED) {
					return new Result<MpsatChainResult>(Outcome.CANCELLED);
				}
				return new Result<MpsatChainResult>(Outcome.FAILED,
						new MpsatChainResult(exportResult, punfResult, null, deadlockSettings));
			}
			monitor.progressUpdate(0.40);

			// Check for deadlock
			MpsatTask mpsatTask = new MpsatTask(deadlockSettings.getMpsatArguments(), mciFile.getCanonicalPath());
			Result<? extends ExternalProcessResult> mpsatResult = framework.getTaskManager().execute(mpsatTask, "Running deadlock checking [MPSat]", mon);
			if (mpsatResult.getOutcome() != Outcome.FINISHED) {
				if (mpsatResult.getOutcome() == Outcome.CANCELLED) {
					return new Result<MpsatChainResult>(Outcome.CANCELLED);
				}
				return new Result<MpsatChainResult>(Outcome.FAILED,
						new MpsatChainResult(exportResult, punfResult, mpsatResult, deadlockSettings));
			}
			monitor.progressUpdate(0.60);

			MpsatResultParser mdp = new MpsatResultParser(mpsatResult.getReturnValue());
			if (!mdp.getSolutions().isEmpty()) {
				mciFile.delete();
				return new Result<MpsatChainResult>(Outcome.FINISHED,
						new MpsatChainResult(exportResult, punfResult, mpsatResult, deadlockSettings, "Circuit has a deadlock"));
			}
			monitor.progressUpdate(0.70);

			// Check for hazards
			mpsatTask = new MpsatTask(hazardSettings.getMpsatArguments(), mciFile.getCanonicalPath());
			mpsatResult = framework.getTaskManager().execute(mpsatTask, "Running semimodularity checking [MPSat]", mon);
			if (mpsatResult.getOutcome() != Outcome.FINISHED) {
				if (mpsatResult.getOutcome() == Outcome.CANCELLED)
					return new Result<MpsatChainResult>(Outcome.CANCELLED);

				return new Result<MpsatChainResult>(Outcome.FAILED,
						new MpsatChainResult(exportResult, punfResult, mpsatResult, hazardSettings));
			}
			monitor.progressUpdate(0.90);

			mdp = new MpsatResultParser(mpsatResult.getReturnValue());
			if (!mdp.getSolutions().isEmpty()) {
				mciFile.delete();
				return new Result<MpsatChainResult>(Outcome.FINISHED,
						new MpsatChainResult(exportResult, punfResult, mpsatResult, hazardSettings, "Circuit has hazard(s)"));
			}
			monitor.progressUpdate(1.0);

			// Success
			mciFile.delete();
			return new Result<MpsatChainResult>(Outcome.FINISHED,
					new MpsatChainResult(exportResult, punfResult, mpsatResult, hazardSettings, "Circuit is deadlock-free and hazard-free"));

		} catch (Throwable e) {
			return new Result<MpsatChainResult>(e);
		}
	}

}
