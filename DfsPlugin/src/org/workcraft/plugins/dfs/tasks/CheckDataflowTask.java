package org.workcraft.plugins.dfs.tasks;

import java.io.File;

import org.workcraft.Framework;
import org.workcraft.exceptions.NoExporterException;
import org.workcraft.interop.Exporter;
import org.workcraft.plugins.dfs.VisualDfs;
import org.workcraft.plugins.dfs.stg.DfsToStgConverter;
import org.workcraft.plugins.mpsat.MpsatParameters;
import org.workcraft.plugins.mpsat.tasks.MpsatChainOutput;
import org.workcraft.plugins.mpsat.tasks.MpsatChainTask;
import org.workcraft.plugins.mpsat.tasks.MpsatOutputParser;
import org.workcraft.plugins.mpsat.tasks.MpsatOutput;
import org.workcraft.plugins.mpsat.tasks.MpsatTask;
import org.workcraft.plugins.punf.tasks.PunfOutput;
import org.workcraft.plugins.punf.tasks.PunfTask;
import org.workcraft.plugins.shared.tasks.ExportOutput;
import org.workcraft.plugins.shared.tasks.ExportTask;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.plugins.stg.interop.StgFormat;
import org.workcraft.tasks.ProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.tasks.SubtaskMonitor;
import org.workcraft.util.ExportUtils;
import org.workcraft.util.FileUtils;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

public class CheckDataflowTask extends MpsatChainTask {
    private final MpsatParameters toolchainPreparationSettings = MpsatParameters.getToolchainPreparationSettings();
    private final MpsatParameters toolchainCompletionSettings = MpsatParameters.getToolchainCompletionSettings();
    private final MpsatParameters deadlockSettings = MpsatParameters.getDeadlockSettings();
    private final MpsatParameters persistencySettings = MpsatParameters.getOutputPersistencySettings();

    public CheckDataflowTask(WorkspaceEntry we) {
        super(we, null);
    }

    @Override
    public Result<? extends MpsatChainOutput> run(ProgressMonitor<? super MpsatChainOutput> monitor) {
        final Framework framework = Framework.getInstance();
        WorkspaceEntry we = getWorkspaceEntry();
        String prefix = FileUtils.getTempPrefix(we.getTitle());
        File directory = FileUtils.createTempDirectory(prefix);
        try {
            VisualDfs dfs = WorkspaceUtils.getAs(we, VisualDfs.class);
            DfsToStgConverter converter = new DfsToStgConverter(dfs);
            StgModel model = (StgModel) converter.getStgModel().getMathModel();
            StgFormat format = StgFormat.getInstance();
            Exporter exporter = ExportUtils.chooseBestExporter(framework.getPluginManager(), model, format);
            if (exporter == null) {
                throw new NoExporterException(model, StgFormat.getInstance());
            }
            monitor.progressUpdate(0.10);

            File netFile = new File(directory, "net" + format.getExtension());
            ExportTask exportTask = new ExportTask(exporter, model, netFile.getAbsolutePath());
            SubtaskMonitor<Object> mon = new SubtaskMonitor<>(monitor);
            Result<? extends ExportOutput> exportResult = framework.getTaskManager().execute(
                    exportTask, "Exporting .g", mon);

            if (exportResult.getOutcome() != Outcome.SUCCESS) {
                if (exportResult.getOutcome() == Outcome.CANCEL) {
                    return new Result<MpsatChainOutput>(Outcome.CANCEL);
                }
                return new Result<MpsatChainOutput>(Outcome.FAILURE,
                        new MpsatChainOutput(exportResult, null, null, null, toolchainPreparationSettings));
            }
            monitor.progressUpdate(0.20);

            File unfoldingFile = new File(directory, "unfolding" + PunfTask.PNML_FILE_EXTENSION);
            PunfTask punfTask = new PunfTask(netFile.getAbsolutePath(), unfoldingFile.getAbsolutePath());
            Result<? extends PunfOutput> punfResult = framework.getTaskManager().execute(
                    punfTask, "Unfolding .g", mon);

            if (punfResult.getOutcome() != Outcome.SUCCESS) {
                if (punfResult.getOutcome() == Outcome.CANCEL) {
                    return new Result<MpsatChainOutput>(Outcome.CANCEL);
                }
                return new Result<MpsatChainOutput>(Outcome.FAILURE,
                        new MpsatChainOutput(exportResult, null, punfResult, null, toolchainPreparationSettings));
            }
            monitor.progressUpdate(0.40);

            MpsatTask deadlockMpsatTask = new MpsatTask(deadlockSettings.getMpsatArguments(directory),
                    unfoldingFile, directory);
            Result<? extends MpsatOutput> deadlockMpsatResult = framework.getTaskManager().execute(
                    deadlockMpsatTask, "Running deadlock checking [MPSat]", mon);

            if (deadlockMpsatResult.getOutcome() != Outcome.SUCCESS) {
                if (deadlockMpsatResult.getOutcome() == Outcome.CANCEL) {
                    return new Result<MpsatChainOutput>(Outcome.CANCEL);
                }
                return new Result<MpsatChainOutput>(Outcome.FAILURE,
                        new MpsatChainOutput(exportResult, null, punfResult, deadlockMpsatResult, deadlockSettings));
            }
            monitor.progressUpdate(0.60);

            MpsatOutputParser deadlockMpsatResultParser = new MpsatOutputParser(deadlockMpsatResult.getPayload());
            if (!deadlockMpsatResultParser.getSolutions().isEmpty()) {
                return new Result<MpsatChainOutput>(Outcome.SUCCESS,
                        new MpsatChainOutput(exportResult, null, punfResult, deadlockMpsatResult, deadlockSettings, "Dataflow has a deadlock"));
            }
            monitor.progressUpdate(0.70);

            MpsatTask persistencyMpsatTask = new MpsatTask(persistencySettings.getMpsatArguments(directory),
                    unfoldingFile, directory, netFile);
            Result<? extends MpsatOutput> persistencyMpsatResult = framework.getTaskManager().execute(persistencyMpsatTask, "Running semimodularity checking [MPSat]", mon);
            if (persistencyMpsatResult.getOutcome() != Outcome.SUCCESS) {
                if (persistencyMpsatResult.getOutcome() == Outcome.CANCEL) {
                    return new Result<MpsatChainOutput>(Outcome.CANCEL);
                }
                return new Result<MpsatChainOutput>(Outcome.FAILURE,
                        new MpsatChainOutput(exportResult, null, punfResult, persistencyMpsatResult, persistencySettings));
            }
            monitor.progressUpdate(0.90);

            MpsatOutputParser persistencyMpsatResultParser = new MpsatOutputParser(persistencyMpsatResult.getPayload());
            if (!persistencyMpsatResultParser.getSolutions().isEmpty()) {
                return new Result<MpsatChainOutput>(Outcome.SUCCESS,
                        new MpsatChainOutput(exportResult, null, punfResult, persistencyMpsatResult, persistencySettings, "Dataflow is not output-persistent"));
            }
            monitor.progressUpdate(1.0);

            return new Result<MpsatChainOutput>(Outcome.SUCCESS,
                    new MpsatChainOutput(exportResult, null, punfResult, null, toolchainCompletionSettings, "Dataflow is deadlock-free and output-persistent"));

        } catch (Throwable e) {
            return new Result<MpsatChainOutput>(e);
        } finally {
            FileUtils.deleteOnExitRecursively(directory);
        }
    }

}
