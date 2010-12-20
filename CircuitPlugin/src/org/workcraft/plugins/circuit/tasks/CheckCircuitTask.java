package org.workcraft.plugins.circuit.tasks;

import java.io.File;

import org.workcraft.Framework;
import org.workcraft.interop.Exporter;
import org.workcraft.plugins.circuit.CircuitSettings;
import org.workcraft.plugins.circuit.VisualCircuit;
import org.workcraft.plugins.circuit.stg.CircuitPetriNetGenerator;
import org.workcraft.plugins.mpsat.MpsatMode;
import org.workcraft.plugins.mpsat.MpsatResultParser;
import org.workcraft.plugins.mpsat.MpsatSettings;
import org.workcraft.plugins.mpsat.MpsatSettings.SolutionMode;
import org.workcraft.plugins.mpsat.tasks.MpsatChainResult;
import org.workcraft.plugins.mpsat.tasks.MpsatChainTask;
import org.workcraft.plugins.mpsat.tasks.MpsatTask;
import org.workcraft.plugins.mpsat.tasks.PunfTask;
import org.workcraft.plugins.shared.tasks.ExternalProcessResult;
import org.workcraft.plugins.stg.STGModel;
import org.workcraft.serialisation.Format;
import org.workcraft.tasks.ProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.SubtaskMonitor;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.util.Export;
import org.workcraft.util.Export.ExportTask;
import org.workcraft.workspace.WorkspaceEntry;


public class CheckCircuitTask extends MpsatChainTask {
	private final MpsatSettings deadlockSettings;
	private final MpsatSettings semimodSettings;
//	private final MpsatSettings settings;
	private final WorkspaceEntry we;
	private final Framework framework;
	private STGModel model;

	private String message="";


	// setup for searching hazards in circuits
	private final String nonPersReach =
		"card DUMMY != 0 ? fail \"This property can be checked only on STGs without dummies\" :\n"+
		"	exists t1 in tran EVENTS s.t. sig t1 in LOCAL {\n"+
		"	  @t1 &\n"+
		"	  exists t2 in tran EVENTS s.t. sig t2 != sig t1 & card (pre t1 * (pre t2 \\ post t2)) != 0 {\n"+
		"	    @t2 &\n"+
		"	    forall t3 in tran EVENTS * (tran sig t1 \\ {t1}) s.t. card (pre t3 * (pre t2 \\ post t2)) = 0 {\n"+
		"	       exists p in pre t3 \\ post t2 { ~$p }\n"+
		"	    }\n"+
		"	  }\n"+
		"	}\n";


	public CheckCircuitTask(WorkspaceEntry we, Framework framework) {
		super (we, null, framework);
		this.we = we;
		this.framework = framework;
		this.model = null;

		this.deadlockSettings = new MpsatSettings(MpsatMode.DEADLOCK, 0, MpsatSettings.SOLVER_MINISAT,
				CircuitSettings.getCheckMode(), (CircuitSettings.getCheckMode()==SolutionMode.ALL)?10:1, null);
		this.semimodSettings = new MpsatSettings(MpsatMode.STG_REACHABILITY, 0, MpsatSettings.SOLVER_MINISAT,
				CircuitSettings.getCheckMode(),(CircuitSettings.getCheckMode()==SolutionMode.ALL)?10:1, nonPersReach);
	}


	@Override
	public Result<? extends MpsatChainResult> run(ProgressMonitor<? super MpsatChainResult> monitor) {
		try {

			monitor.progressUpdate(0.05);
			VisualCircuit circuit = (VisualCircuit) we.getModelEntry().getVisualModel();
			model = (STGModel) CircuitPetriNetGenerator.generate(circuit).getMathModel();
			monitor.progressUpdate(0.10);

			Exporter exporter = Export.chooseBestExporter(framework.getPluginManager(), model, Format.STG);

			if (exporter == null)
				throw new RuntimeException ("Exporter not available: model class " + model.getClass().getName() + " to format STG.");

			File netFile = File.createTempFile("net", exporter.getExtenstion());

			ExportTask exportTask;

			exportTask = new ExportTask(exporter, model, netFile.getCanonicalPath());

			SubtaskMonitor<Object> mon = new SubtaskMonitor<Object>(monitor);

			Result<? extends Object> exportResult = framework.getTaskManager().execute(exportTask, "Exporting .g", mon);

			if (exportResult.getOutcome() != Outcome.FINISHED) {
				netFile.delete();
				if (exportResult.getOutcome() == Outcome.CANCELLED)
					return new Result<MpsatChainResult>(Outcome.CANCELLED);

				return new Result<MpsatChainResult>(Outcome.FAILED, new MpsatChainResult(exportResult, null, null, deadlockSettings));
			}

			monitor.progressUpdate(0.25);

			File mciFile = File.createTempFile("unfolding", ".mci");

			PunfTask punfTask = new PunfTask(netFile.getCanonicalPath(), mciFile.getCanonicalPath());
			Result<? extends ExternalProcessResult> punfResult = framework.getTaskManager().execute(punfTask, "Unfolding .g", mon);
			netFile.delete();

			if (punfResult.getOutcome() != Outcome.FINISHED) {
				mciFile.delete();
				if (punfResult.getOutcome() == Outcome.CANCELLED)
					return new Result<MpsatChainResult>(Outcome.CANCELLED);
				return new Result<MpsatChainResult>(Outcome.FAILED, new MpsatChainResult(exportResult, punfResult, null, deadlockSettings));
			}

			monitor.progressUpdate(0.50);

			MpsatTask mpsatTask = new MpsatTask(deadlockSettings.getMpsatArguments(), mciFile.getCanonicalPath());
			Result<? extends ExternalProcessResult> mpsatResult = framework.getTaskManager().execute(mpsatTask, "Running deadlock checking (mpsat)", mon);

			if (mpsatResult.getOutcome() != Outcome.FINISHED) {
				if (mpsatResult.getOutcome() == Outcome.CANCELLED)
					return new Result<MpsatChainResult>(Outcome.CANCELLED);

				return new Result<MpsatChainResult>(Outcome.FAILED, new MpsatChainResult(exportResult, punfResult, mpsatResult, deadlockSettings ));
			}

			monitor.progressUpdate(0.75);

			MpsatResultParser mdp = new MpsatResultParser(mpsatResult.getReturnValue());

			if (!mdp.getSolutions().isEmpty()) {
				mciFile.delete();
				return new Result<MpsatChainResult>(Outcome.FINISHED, new MpsatChainResult(exportResult, punfResult, mpsatResult, deadlockSettings, "Circuit has a deadlock"));
			}

			mpsatTask = new MpsatTask(semimodSettings.getMpsatArguments(), mciFile.getCanonicalPath());
			mpsatResult = framework.getTaskManager().execute(mpsatTask, "Running semimodularity checking (mpsat)", mon);

			if (mpsatResult.getOutcome() != Outcome.FINISHED) {
				if (mpsatResult.getOutcome() == Outcome.CANCELLED)
					return new Result<MpsatChainResult>(Outcome.CANCELLED);

				return new Result<MpsatChainResult>(Outcome.FAILED, new MpsatChainResult(exportResult, punfResult, mpsatResult, semimodSettings));
			}

			monitor.progressUpdate(1.0);

			mdp = new MpsatResultParser(mpsatResult.getReturnValue());

			if (!mdp.getSolutions().isEmpty()) {
				mciFile.delete();
				return new Result<MpsatChainResult>(Outcome.FINISHED, new MpsatChainResult(exportResult, punfResult, mpsatResult, semimodSettings, "Circuit has hazard(s)"));
			}

			mciFile.delete();
			return new Result<MpsatChainResult>(Outcome.FINISHED, new MpsatChainResult(exportResult, punfResult, mpsatResult, semimodSettings, "Circuit is deadlock-free with no hazards"));

		} catch (Throwable e) {
			return new Result<MpsatChainResult>(e);
		}
	}

	public String getMessage() {
		return message;
	}

}
