package org.workcraft.plugins.dfs.tasks;

import java.io.File;

import org.workcraft.Framework;
import org.workcraft.interop.Exporter;
import org.workcraft.plugins.dfs.VisualDfs;
import org.workcraft.plugins.dfs.stg.DfsToStgConverter;
import org.workcraft.plugins.mpsat.MpsatResultParser;
import org.workcraft.plugins.mpsat.MpsatParameters;
import org.workcraft.plugins.mpsat.tasks.MpsatChainResult;
import org.workcraft.plugins.mpsat.tasks.MpsatChainTask;
import org.workcraft.plugins.mpsat.tasks.MpsatTask;
import org.workcraft.plugins.punf.PunfSettings;
import org.workcraft.plugins.punf.tasks.PunfTask;
import org.workcraft.plugins.shared.tasks.ExternalProcessResult;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.serialisation.Format;
import org.workcraft.tasks.ProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.tasks.SubtaskMonitor;
import org.workcraft.util.Export;
import org.workcraft.util.Export.ExportTask;
import org.workcraft.util.FileUtils;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

public class CheckDataflowTask extends MpsatChainTask {
    private final MpsatParameters toolchainPreparationSettings = MpsatParameters.getToolchainPreparationSettings();
    private final MpsatParameters toolchainCompletionSettings = MpsatParameters.getToolchainCompletionSettings();
    private final MpsatParameters deadlockSettings = MpsatParameters.getDeadlockSettings();
    private final MpsatParameters persistencySettings = MpsatParameters.getOutputPersistencySettings();
    private final WorkspaceEntry we;

    public CheckDataflowTask(WorkspaceEntry we) {
        super(we, null);
        this.we = we;
    }

    @Override
    public Result<? extends MpsatChainResult> run(ProgressMonitor<? super MpsatChainResult> monitor) {
        final Framework framework = Framework.getInstance();
        String prefix = FileUtils.getTempPrefix(we.getTitle());
        File directory = FileUtils.createTempDirectory(prefix);
        try {
            VisualDfs dfs = WorkspaceUtils.getAs(we, VisualDfs.class);
            DfsToStgConverter converter = new DfsToStgConverter(dfs);
            StgModel model = (StgModel) converter.getStgModel().getMathModel();
            Exporter exporter = Export.chooseBestExporter(framework.getPluginManager(), model, Format.STG);
            if (exporter == null) {
                throw new RuntimeException("Exporter not available: model class " + model.getClass().getName() + " to format STG.");
            }
            monitor.progressUpdate(0.10);

            File netFile = new File(directory, "net" + exporter.getExtenstion());
            ExportTask exportTask = new ExportTask(exporter, model, netFile.getAbsolutePath());
            SubtaskMonitor<Object> mon = new SubtaskMonitor<>(monitor);
            Result<? extends Object> exportResult = framework.getTaskManager().execute(
                    exportTask, "Exporting .g", mon);

            if (exportResult.getOutcome() != Outcome.FINISHED) {
                if (exportResult.getOutcome() == Outcome.CANCELLED) {
                    return new Result<MpsatChainResult>(Outcome.CANCELLED);
                }
                return new Result<MpsatChainResult>(Outcome.FAILED,
                        new MpsatChainResult(exportResult, null, null, null, toolchainPreparationSettings));
            }
            monitor.progressUpdate(0.20);

            File unfoldingFile = new File(directory, "unfolding" + PunfSettings.getUnfoldingExtension(true));
            PunfTask punfTask = new PunfTask(netFile.getAbsolutePath(), unfoldingFile.getAbsolutePath());
            Result<? extends ExternalProcessResult> punfResult = framework.getTaskManager().execute(
                    punfTask, "Unfolding .g", mon);

            if (punfResult.getOutcome() != Outcome.FINISHED) {
                if (punfResult.getOutcome() == Outcome.CANCELLED) {
                    return new Result<MpsatChainResult>(Outcome.CANCELLED);
                }
                return new Result<MpsatChainResult>(Outcome.FAILED,
                        new MpsatChainResult(exportResult, null, punfResult, null, toolchainPreparationSettings));
            }
            monitor.progressUpdate(0.40);

            MpsatTask deadlockMpsatTask = new MpsatTask(deadlockSettings.getMpsatArguments(directory),
                    unfoldingFile, directory);
            Result<? extends ExternalProcessResult> deadlockMpsatResult = framework.getTaskManager().execute(
                    deadlockMpsatTask, "Running deadlock checking [MPSat]", mon);

            if (deadlockMpsatResult.getOutcome() != Outcome.FINISHED) {
                if (deadlockMpsatResult.getOutcome() == Outcome.CANCELLED) {
                    return new Result<MpsatChainResult>(Outcome.CANCELLED);
                }
                return new Result<MpsatChainResult>(Outcome.FAILED,
                        new MpsatChainResult(exportResult, null, punfResult, deadlockMpsatResult, deadlockSettings));
            }
            monitor.progressUpdate(0.60);

            MpsatResultParser deadlockMpsatResultParser = new MpsatResultParser(deadlockMpsatResult.getReturnValue());
            if (!deadlockMpsatResultParser.getSolutions().isEmpty()) {
                return new Result<MpsatChainResult>(Outcome.FINISHED,
                        new MpsatChainResult(exportResult, null, punfResult, deadlockMpsatResult, deadlockSettings, "Dataflow has a deadlock"));
            }
            monitor.progressUpdate(0.70);

            MpsatTask persistencyMpsatTask = new MpsatTask(persistencySettings.getMpsatArguments(directory),
                    unfoldingFile, directory, true, netFile);
            Result<? extends ExternalProcessResult> persistencyMpsatResult = framework.getTaskManager().execute(persistencyMpsatTask, "Running semimodularity checking [MPSat]", mon);
            if (persistencyMpsatResult.getOutcome() != Outcome.FINISHED) {
                if (persistencyMpsatResult.getOutcome() == Outcome.CANCELLED) {
                    return new Result<MpsatChainResult>(Outcome.CANCELLED);
                }
                return new Result<MpsatChainResult>(Outcome.FAILED,
                        new MpsatChainResult(exportResult, null, punfResult, persistencyMpsatResult, persistencySettings));
            }
            monitor.progressUpdate(0.90);

            MpsatResultParser persistencyMpsatResultParser = new MpsatResultParser(persistencyMpsatResult.getReturnValue());
            if (!persistencyMpsatResultParser.getSolutions().isEmpty()) {
                return new Result<MpsatChainResult>(Outcome.FINISHED,
                        new MpsatChainResult(exportResult, null, punfResult, persistencyMpsatResult, persistencySettings, "Dataflow is not output persistent"));
            }
            monitor.progressUpdate(1.0);

            return new Result<MpsatChainResult>(Outcome.FINISHED,
                    new MpsatChainResult(exportResult, null, punfResult, null, toolchainCompletionSettings, "Dataflow is deadlock-free and output persistent"));

        } catch (Throwable e) {
            return new Result<MpsatChainResult>(e);
        } finally {
            FileUtils.deleteOnExitRecursively(directory);
        }
    }

}
