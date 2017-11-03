package org.workcraft.plugins.mpsat.tasks;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.workcraft.Framework;
import org.workcraft.interop.Exporter;
import org.workcraft.plugins.mpsat.MpsatParameters;
import org.workcraft.plugins.petri.PetriNetModel;
import org.workcraft.plugins.punf.PunfSettings;
import org.workcraft.plugins.punf.tasks.PunfTask;
import org.workcraft.plugins.shared.tasks.ExternalProcessResult;
import org.workcraft.plugins.stg.interop.StgFormat;
import org.workcraft.tasks.ProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.tasks.SubtaskMonitor;
import org.workcraft.tasks.Task;
import org.workcraft.util.Export;
import org.workcraft.util.Export.ExportTask;
import org.workcraft.util.FileUtils;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

public class MpsatCombinedChainTask implements Task<MpsatCombinedChainResult> {
    private final WorkspaceEntry we;
    private final List<MpsatParameters> settingsList;

    public MpsatCombinedChainTask(WorkspaceEntry we, List<MpsatParameters> settingsList) {
        this.we = we;
        this.settingsList = settingsList;
    }

    @Override
    public Result<? extends MpsatCombinedChainResult> run(ProgressMonitor<? super MpsatCombinedChainResult> monitor) {
        Framework framework = Framework.getInstance();
        String prefix = FileUtils.getTempPrefix(we.getTitle());
        File directory = FileUtils.createTempDirectory(prefix);
        try {
            PetriNetModel model = WorkspaceUtils.getAs(we, PetriNetModel.class);
            StgFormat stgFormat = StgFormat.getInstance();
            Exporter exporter = Export.chooseBestExporter(framework.getPluginManager(), model, stgFormat);
            if (exporter == null) {
                throw new RuntimeException("Exporter not available: model class " + model.getClass().getName() + " to format STG.");
            }
            SubtaskMonitor<Object> subtaskMonitor = new SubtaskMonitor<>(monitor);

            // Generate .g for the model
            File netFile = new File(directory, "net" + stgFormat.getExtension());
            ExportTask exportTask = new ExportTask(exporter, model, netFile.getAbsolutePath());
            Result<? extends Object> exportResult = framework.getTaskManager().execute(
                    exportTask, "Exporting .g", subtaskMonitor);

            if (exportResult.getOutcome() != Outcome.SUCCESS) {
                if (exportResult.getOutcome() == Outcome.CANCEL) {
                    return new Result<MpsatCombinedChainResult>(Outcome.CANCEL);
                }
                return new Result<MpsatCombinedChainResult>(Outcome.FAILURE,
                        new MpsatCombinedChainResult(exportResult, null, null, null, settingsList));
            }
            monitor.progressUpdate(0.33);

            // Generate unfolding
            boolean tryPnml = true;
            for (MpsatParameters settings: settingsList) {
                tryPnml &= settings.getMode().canPnml();
            }
            File unfoldingFile = new File(directory, "unfolding" + PunfSettings.getUnfoldingExtension(tryPnml));
            PunfTask punfTask = new PunfTask(netFile.getAbsolutePath(), unfoldingFile.getAbsolutePath());
            Result<? extends ExternalProcessResult> punfResult = framework.getTaskManager().execute(punfTask, "Unfolding .g", subtaskMonitor);

            if (punfResult.getOutcome() != Outcome.SUCCESS) {
                if (punfResult.getOutcome() == Outcome.CANCEL) {
                    return new Result<MpsatCombinedChainResult>(Outcome.CANCEL);
                }
                return new Result<MpsatCombinedChainResult>(Outcome.FAILURE,
                        new MpsatCombinedChainResult(exportResult, null, punfResult, null, settingsList));
            }
            monitor.progressUpdate(0.66);

            // Run MPSat on the generated unfolding
            ArrayList<Result<? extends ExternalProcessResult>> mpsatResultList = new ArrayList<>(settingsList.size());
            for (MpsatParameters settings: settingsList) {
                MpsatTask mpsatTask = new MpsatTask(settings.getMpsatArguments(directory),
                        unfoldingFile, directory, tryPnml, netFile);
                Result<? extends ExternalProcessResult> mpsatResult = framework.getTaskManager().execute(
                        mpsatTask, "Running verification [MPSat]", subtaskMonitor);
                mpsatResultList.add(mpsatResult);
                if (mpsatResult.getOutcome() != Outcome.SUCCESS) {
                    if (mpsatResult.getOutcome() == Outcome.CANCEL) {
                        return new Result<MpsatCombinedChainResult>(Outcome.CANCEL);
                    }
                    return new Result<MpsatCombinedChainResult>(Outcome.FAILURE,
                            new MpsatCombinedChainResult(exportResult, null, punfResult, mpsatResultList, settingsList));
                }
            }
            monitor.progressUpdate(1.0);

            return new Result<MpsatCombinedChainResult>(Outcome.SUCCESS,
                    new MpsatCombinedChainResult(exportResult, null, punfResult, mpsatResultList, settingsList));
        } catch (Throwable e) {
            return new Result<MpsatCombinedChainResult>(e);
        } finally {
            FileUtils.deleteOnExitRecursively(directory);
        }
    }

    public List<MpsatParameters> getSettingsList() {
        return settingsList;
    }

    public WorkspaceEntry getWorkspaceEntry() {
        return we;
    }

}
