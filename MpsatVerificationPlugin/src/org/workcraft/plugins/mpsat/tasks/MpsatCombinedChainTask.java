package org.workcraft.plugins.mpsat.tasks;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.workcraft.Framework;
import org.workcraft.interop.Exporter;
import org.workcraft.plugins.mpsat.MpsatSettings;
import org.workcraft.plugins.petri.PetriNetModel;
import org.workcraft.plugins.punf.PunfUtilitySettings;
import org.workcraft.plugins.punf.tasks.PunfTask;
import org.workcraft.plugins.shared.CommonDebugSettings;
import org.workcraft.plugins.shared.tasks.ExternalProcessResult;
import org.workcraft.serialisation.Format;
import org.workcraft.tasks.ProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.tasks.SubtaskMonitor;
import org.workcraft.tasks.Task;
import org.workcraft.util.Export;
import org.workcraft.util.Export.ExportTask;
import org.workcraft.util.FileUtils;
import org.workcraft.util.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class MpsatCombinedChainTask implements Task<MpsatCombinedChainResult> {
	private final WorkspaceEntry we;
	private final List<MpsatSettings> settingsList;

	public MpsatCombinedChainTask(WorkspaceEntry we, List<MpsatSettings> settingsList) {
		this.we = we;
		this.settingsList = settingsList;
	}

	@Override
	public Result<? extends MpsatCombinedChainResult> run(ProgressMonitor<? super MpsatCombinedChainResult> monitor) {
		Framework framework = Framework.getInstance();
		File directory = null;
		try {
			String title = we.getTitle();
			String prefix = "workcraft-" + title + "-"; // Prefix must be at least 3 symbols long.
			directory = FileUtils.createTempDirectory(prefix);

			PetriNetModel model = WorkspaceUtils.getAs(we, PetriNetModel.class);
			Exporter exporter = Export.chooseBestExporter(framework.getPluginManager(), model, Format.STG);
			if (exporter == null) {
				throw new RuntimeException ("Exporter not available: model class " + model.getClass().getName() + " to format STG.");
			}
			SubtaskMonitor<Object> subtaskMonitor = new SubtaskMonitor<Object>(monitor);

			// Generate .g for the model
			File netFile = new File(directory, "net" + exporter.getExtenstion());
			ExportTask exportTask = new ExportTask(exporter, model, netFile.getCanonicalPath());
			Result<? extends Object> exportResult = framework.getTaskManager().execute(
					exportTask, "Exporting .g", subtaskMonitor);

			if (exportResult.getOutcome() != Outcome.FINISHED) {
				if (exportResult.getOutcome() == Outcome.CANCELLED) {
					return new Result<MpsatCombinedChainResult>(Outcome.CANCELLED);
				}
				return new Result<MpsatCombinedChainResult>(Outcome.FAILED,
						new MpsatCombinedChainResult(exportResult, null, null, null, settingsList));
			}
			monitor.progressUpdate(0.33);

			// Generate unfolding
			boolean tryPnml = true;
			for (MpsatSettings settings: settingsList) {
				tryPnml &= settings.getMode().canPnml();
			}
			File unfoldingFile = new File(directory, "unfolding" + PunfUtilitySettings.getUnfoldingExtension(tryPnml));
			PunfTask punfTask = new PunfTask(netFile.getCanonicalPath(), unfoldingFile.getCanonicalPath(), tryPnml);
			Result<? extends ExternalProcessResult> punfResult = framework.getTaskManager().execute(punfTask, "Unfolding .g", subtaskMonitor);

			if (punfResult.getOutcome() != Outcome.FINISHED) {
				if (punfResult.getOutcome() == Outcome.CANCELLED) {
					return new Result<MpsatCombinedChainResult>(Outcome.CANCELLED);
				}
				return new Result<MpsatCombinedChainResult>(Outcome.FAILED,
						new MpsatCombinedChainResult(exportResult, null, punfResult, null, settingsList));
			}
			monitor.progressUpdate(0.66);

			// Run MPSat on the generated unfolding
			ArrayList<Result<? extends ExternalProcessResult>> mpsatResultList = new ArrayList<>(settingsList.size());
			for (MpsatSettings settings: settingsList) {
				MpsatTask mpsatTask = new MpsatTask(settings.getMpsatArguments(directory),
						unfoldingFile.getCanonicalPath(), directory, tryPnml);
				Result<? extends ExternalProcessResult> mpsatResult = framework.getTaskManager().execute(
						mpsatTask, "Running verification [MPSat]", subtaskMonitor);
				mpsatResultList.add(mpsatResult);
				if (mpsatResult.getOutcome() != Outcome.FINISHED) {
					if (mpsatResult.getOutcome() == Outcome.CANCELLED) {
						return new Result<MpsatCombinedChainResult>(Outcome.CANCELLED);
					}
					return new Result<MpsatCombinedChainResult>(Outcome.FAILED,
							new MpsatCombinedChainResult(exportResult, null, punfResult, mpsatResultList, settingsList));
				}
			}
			monitor.progressUpdate(1.0);

			return new Result<MpsatCombinedChainResult>(Outcome.FINISHED,
					new MpsatCombinedChainResult(exportResult, null, punfResult, mpsatResultList, settingsList));
		} catch (Throwable e) {
			return new Result<MpsatCombinedChainResult>(e);
		}
		// Clean up
		finally {
			FileUtils.deleteFile(directory, CommonDebugSettings.getKeepTemporaryFiles());
		}
	}

	public List<MpsatSettings> getSettingsList() {
		return settingsList;
	}

	public WorkspaceEntry getWorkspaceEntry() {
		return we;
	}

}
