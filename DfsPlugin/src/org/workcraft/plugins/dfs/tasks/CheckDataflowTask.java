package org.workcraft.plugins.dfs.tasks;

import java.io.File;

import org.workcraft.Framework;
import org.workcraft.interop.Exporter;
import org.workcraft.plugins.dfs.VisualDfs;
import org.workcraft.plugins.dfs.stg.StgGenerator;
import org.workcraft.plugins.mpsat.MpsatMode;
import org.workcraft.plugins.mpsat.MpsatResultParser;
import org.workcraft.plugins.mpsat.MpsatSettings;
import org.workcraft.plugins.mpsat.MpsatUtilitySettings;
import org.workcraft.plugins.mpsat.tasks.MpsatChainResult;
import org.workcraft.plugins.mpsat.tasks.MpsatChainTask;
import org.workcraft.plugins.mpsat.tasks.MpsatTask;
import org.workcraft.plugins.mpsat.tasks.PunfTask;
import org.workcraft.plugins.shared.tasks.ExternalProcessResult;
import org.workcraft.plugins.stg.STGModel;
import org.workcraft.serialisation.Format;
import org.workcraft.tasks.ProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.tasks.SubtaskMonitor;
import org.workcraft.util.Export;
import org.workcraft.util.Export.ExportTask;
import org.workcraft.workspace.WorkspaceEntry;


public class CheckDataflowTask extends MpsatChainTask {
	private final MpsatSettings deadlockSettings;
	private final MpsatSettings hazardSettings;
	private final WorkspaceEntry we;

	public CheckDataflowTask(WorkspaceEntry we) {
		super (we, null);
		this.we = we;

		this.deadlockSettings = new MpsatSettings("Deadlock", MpsatMode.DEADLOCK, 0,
				MpsatUtilitySettings.getSolutionMode(), MpsatUtilitySettings.getSolutionCount());

		this.hazardSettings = new MpsatSettings("Output persistency", MpsatMode.STG_REACHABILITY, 0,
				MpsatUtilitySettings.getSolutionMode(), MpsatUtilitySettings.getSolutionCount(),
				MpsatSettings.reachSemimodularity, true);
	}

	@Override
	public Result<? extends MpsatChainResult> run(ProgressMonitor<? super MpsatChainResult> monitor) {
		final Framework framework = Framework.getInstance();
		try {
			StgGenerator generator = new StgGenerator((VisualDfs)we.getModelEntry().getVisualModel());
			STGModel model = (STGModel)generator.getStgModel().getMathModel();
			Exporter exporter = Export.chooseBestExporter(framework.getPluginManager(), model, Format.STG);
			if (exporter == null) {
				throw new RuntimeException ("Exporter not available: model class " + model.getClass().getName() + " to format STG.");
			}
			monitor.progressUpdate(0.10);

			File netFile = File.createTempFile("net", exporter.getExtenstion());
			ExportTask exportTask = new ExportTask(exporter, model, netFile.getCanonicalPath());
			SubtaskMonitor<Object> mon = new SubtaskMonitor<Object>(monitor);
			Result<? extends Object> exportResult = framework.getTaskManager().execute(
					exportTask, "Exporting .g", mon);

			if (exportResult.getOutcome() != Outcome.FINISHED) {
				netFile.delete();
				if (exportResult.getOutcome() == Outcome.CANCELLED) {
					return new Result<MpsatChainResult>(Outcome.CANCELLED);
				}
				return new Result<MpsatChainResult>(Outcome.FAILED,
						new MpsatChainResult(exportResult, null, null, null, deadlockSettings));
			}
			monitor.progressUpdate(0.20);

			File unfoldingFile = File.createTempFile("unfolding", MpsatUtilitySettings.getUnfoldingExtension(true));
			PunfTask punfTask = new PunfTask(netFile.getCanonicalPath(), unfoldingFile.getCanonicalPath(), true);
			Result<? extends ExternalProcessResult> punfResult = framework.getTaskManager().execute(
					punfTask, "Unfolding .g", mon);

			netFile.delete();
			if (punfResult.getOutcome() != Outcome.FINISHED) {
				unfoldingFile.delete();
				if (punfResult.getOutcome() == Outcome.CANCELLED) {
					return new Result<MpsatChainResult>(Outcome.CANCELLED);
				}
				return new Result<MpsatChainResult>(Outcome.FAILED,
						new MpsatChainResult(exportResult, null, punfResult, null, deadlockSettings));
			}
			monitor.progressUpdate(0.40);

			MpsatTask mpsatTask = new MpsatTask(deadlockSettings.getMpsatArguments(), unfoldingFile.getCanonicalPath(), null, true);
			Result<? extends ExternalProcessResult> mpsatResult = framework.getTaskManager().execute(
					mpsatTask, "Running deadlock checking [MPSat]", mon);

			if (mpsatResult.getOutcome() != Outcome.FINISHED) {
				if (mpsatResult.getOutcome() == Outcome.CANCELLED) {
					return new Result<MpsatChainResult>(Outcome.CANCELLED);
				}
				return new Result<MpsatChainResult>(Outcome.FAILED,
						new MpsatChainResult(exportResult, null, punfResult, mpsatResult, deadlockSettings));
			}
			monitor.progressUpdate(0.60);

			MpsatResultParser mdp = new MpsatResultParser(mpsatResult.getReturnValue());
			if (!mdp.getSolutions().isEmpty()) {
				unfoldingFile.delete();
				return new Result<MpsatChainResult>(Outcome.FINISHED,
						new MpsatChainResult(exportResult, null, punfResult, mpsatResult, deadlockSettings, "Dataflow has a deadlock"));
			}
			monitor.progressUpdate(0.70);

			mpsatTask = new MpsatTask(hazardSettings.getMpsatArguments(), unfoldingFile.getCanonicalPath(), null, true);
			mpsatResult = framework.getTaskManager().execute(mpsatTask, "Running semimodularity checking [MPSat]", mon);
			if (mpsatResult.getOutcome() != Outcome.FINISHED) {
				if (mpsatResult.getOutcome() == Outcome.CANCELLED) {
					return new Result<MpsatChainResult>(Outcome.CANCELLED);
				}
				return new Result<MpsatChainResult>(Outcome.FAILED,
						new MpsatChainResult(exportResult, null, punfResult, mpsatResult, hazardSettings));
			}
			monitor.progressUpdate(0.90);

			mdp = new MpsatResultParser(mpsatResult.getReturnValue());
			if (!mdp.getSolutions().isEmpty()) {
				unfoldingFile.delete();
				return new Result<MpsatChainResult>(Outcome.FINISHED,
						new MpsatChainResult(exportResult, null, punfResult, mpsatResult, hazardSettings, "Dataflow has hazard(s)"));
			}
			monitor.progressUpdate(1.0);

			unfoldingFile.delete();
			return new Result<MpsatChainResult>(Outcome.FINISHED,
					new MpsatChainResult(exportResult, null, punfResult, mpsatResult, hazardSettings, "Dataflow is deadlock-free and hazard-free"));

		} catch (Throwable e) {
			return new Result<MpsatChainResult>(e);
		}
	}

}
