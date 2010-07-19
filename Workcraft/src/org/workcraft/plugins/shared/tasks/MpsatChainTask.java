package org.workcraft.plugins.shared.tasks;

import java.io.File;

import org.workcraft.Framework;
import org.workcraft.dom.Model;
import org.workcraft.interop.Exporter;
import org.workcraft.plugins.shared.MpsatSettings;
import org.workcraft.serialisation.Format;
import org.workcraft.tasks.ProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.SubtaskMonitor;
import org.workcraft.tasks.Task;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.util.Export;
import org.workcraft.util.Export.ExportTask;

public class MpsatChainTask implements Task<MpsatChainResult> {
	private Model model;
	private MpsatSettings settings;
	private Framework framework;


	public MpsatChainTask(Model model, MpsatSettings settings, Framework framework) {
		this.model = model;
		this.settings = settings;
		this.framework = framework;
	}

	@Override
	public Result<? extends MpsatChainResult> run(ProgressMonitor<? super MpsatChainResult> monitor) {
		try {
			Exporter exporter = Export.chooseBestExporter(framework.getPluginManager(), model, Format.STG);

			if (exporter == null)
				throw new RuntimeException ("Exporter not available: model class " + model.getClass().getName() + " to format STG.");

			File netFile = File.createTempFile("net", exporter.getExtenstion());
			ExportTask exportTask = new ExportTask(exporter, model, netFile.getCanonicalPath());

			SubtaskMonitor<Object> mon = new SubtaskMonitor<Object>(monitor);

			Result<? extends Object> exportResult = framework.getTaskManager().execute(exportTask, "Verification: exporting net", mon);

			if (exportResult.getOutcome() != Outcome.FINISHED) {
				netFile.delete();
				if (exportResult.getOutcome() == Outcome.CANCELLED)
					return new Result<MpsatChainResult>(Outcome.CANCELLED);
				return new Result<MpsatChainResult>(Outcome.FAILED, new MpsatChainResult(exportResult, null, null, settings));
			}

			monitor.progressUpdate(0.33);

			File mciFile = File.createTempFile("unfolding", ".mci");

			PunfTask punfTask = new PunfTask(netFile.getCanonicalPath(), mciFile.getCanonicalPath());
			Result<? extends ExternalProcessResult> punfResult = framework.getTaskManager().execute(punfTask, "Verification: unfolding net", mon);
			netFile.delete();

			if (punfResult.getOutcome() != Outcome.FINISHED) {
				mciFile.delete();
				if (punfResult.getOutcome() == Outcome.CANCELLED)
					return new Result<MpsatChainResult>(Outcome.CANCELLED);
				return new Result<MpsatChainResult>(Outcome.FAILED, new MpsatChainResult(exportResult, punfResult, null, settings));
			}

			monitor.progressUpdate(0.66);

			MpsatTask mpsatTask = new MpsatTask(settings.getMpsatArguments(), mciFile.getCanonicalPath());
			Result<? extends ExternalProcessResult> mpsatResult = framework.getTaskManager().execute(mpsatTask, "Verification: model-checking", mon);
			mciFile.delete();

			if (mpsatResult.getOutcome() != Outcome.FINISHED) {
				if (mpsatResult.getOutcome() == Outcome.CANCELLED)
					return new Result<MpsatChainResult>(Outcome.CANCELLED);
				return new Result<MpsatChainResult>(Outcome.FAILED, new MpsatChainResult(exportResult, punfResult, mpsatResult, settings ));
			}

			monitor.progressUpdate(1.0);

			return new Result<MpsatChainResult>(Outcome.FINISHED, new MpsatChainResult(exportResult, punfResult, mpsatResult, settings));
		} catch (Throwable e) {
			return new Result<MpsatChainResult>(e);
		}
	}
}