package org.workcraft.plugins.mpsat.tasks;

import java.io.File;

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

public class MpsatChainTask implements Task<MpsatChainResult> {
    private final WorkspaceEntry we;
    private final MpsatParameters settings;

    public MpsatChainTask(WorkspaceEntry we, MpsatParameters settings) {
        this.we = we;
        this.settings = settings;
    }

    @Override
    public Result<? extends MpsatChainResult> run(ProgressMonitor<? super MpsatChainResult> monitor) {
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
                    return new Result<MpsatChainResult>(Outcome.CANCEL);
                }
                return new Result<MpsatChainResult>(Outcome.FAILURE,
                        new MpsatChainResult(exportResult, null, null, null, settings));
            }
            monitor.progressUpdate(0.33);

            // Generate unfolding
            boolean tryPnml = settings.getMode().canPnml();
            File unfoldingFile = new File(directory, "unfolding" + PunfSettings.getUnfoldingExtension(tryPnml));
            PunfTask punfTask = new PunfTask(netFile.getAbsolutePath(), unfoldingFile.getAbsolutePath(), tryPnml);
            Result<? extends ExternalProcessResult> punfResult = framework.getTaskManager().execute(punfTask, "Unfolding .g", subtaskMonitor);

            if (punfResult.getOutcome() != Outcome.SUCCESS) {
                if (punfResult.getOutcome() == Outcome.CANCEL) {
                    return new Result<MpsatChainResult>(Outcome.CANCEL);
                }
                return new Result<MpsatChainResult>(Outcome.FAILURE,
                        new MpsatChainResult(exportResult, null, punfResult, null, settings));
            }
            monitor.progressUpdate(0.66);

            // Run MPSat on the generated unfolding
            MpsatTask mpsatTask = new MpsatTask(settings.getMpsatArguments(directory),
                    unfoldingFile, directory, tryPnml, netFile);
            Result<? extends ExternalProcessResult> mpsatResult = framework.getTaskManager().execute(
                    mpsatTask, "Running verification [MPSat]", subtaskMonitor);

            if (mpsatResult.getOutcome() != Outcome.SUCCESS) {
                if (mpsatResult.getOutcome() == Outcome.CANCEL) {
                    return new Result<MpsatChainResult>(Outcome.CANCEL);
                }
                return new Result<MpsatChainResult>(Outcome.FAILURE,
                        new MpsatChainResult(exportResult, null, punfResult, mpsatResult, settings));
            }
            monitor.progressUpdate(1.0);

            return new Result<MpsatChainResult>(Outcome.SUCCESS,
                    new MpsatChainResult(exportResult, null, punfResult, mpsatResult, settings));
        } catch (Throwable e) {
            return new Result<MpsatChainResult>(e);
        } finally {
            FileUtils.deleteOnExitRecursively(directory);
        }
    }

    public MpsatParameters getSettings() {
        return settings;
    }

    public WorkspaceEntry getWorkspaceEntry() {
        return we;
    }

}
