package org.workcraft.plugins.mpsat.tasks;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import org.workcraft.Framework;
import org.workcraft.PluginManager;
import org.workcraft.exceptions.NoExporterException;
import org.workcraft.interop.Exporter;
import org.workcraft.plugins.mpsat.MpsatMode;
import org.workcraft.plugins.mpsat.MpsatParameters;
import org.workcraft.plugins.pcomp.ComponentData;
import org.workcraft.plugins.pcomp.CompositionData;
import org.workcraft.plugins.pcomp.tasks.PcompOutput;
import org.workcraft.plugins.pcomp.tasks.PcompTask;
import org.workcraft.plugins.pcomp.tasks.PcompTask.ConversionMode;
import org.workcraft.plugins.punf.tasks.PunfOutput;
import org.workcraft.plugins.punf.tasks.PunfTask;
import org.workcraft.plugins.shared.tasks.ExportOutput;
import org.workcraft.plugins.shared.tasks.ExportTask;
import org.workcraft.plugins.stg.SignalTransition.Type;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.StgUtils;
import org.workcraft.plugins.stg.interop.StgFormat;
import org.workcraft.tasks.ProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.tasks.SubtaskMonitor;
import org.workcraft.tasks.Task;
import org.workcraft.tasks.TaskManager;
import org.workcraft.util.ExportUtils;
import org.workcraft.util.FileUtils;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

public class MpsatNwayConformationTask implements Task<MpsatChainOutput> {

    private final MpsatParameters toolchainPreparationSettings = new MpsatParameters("Toolchain preparation of data",
            MpsatMode.UNDEFINED, 0, null, 0);

    private final MpsatParameters toolchainCompletionSettings = new MpsatParameters("Toolchain completion",
            MpsatMode.UNDEFINED, 0, null, 0);

    private final ArrayList<WorkspaceEntry> wes;

    public MpsatNwayConformationTask(ArrayList<WorkspaceEntry> wes) {
        this.wes = wes;
    }

    @Override
    public Result<? extends MpsatChainOutput> run(ProgressMonitor<? super MpsatChainOutput> monitor) {
        Framework framework = Framework.getInstance();
        TaskManager taskManager = framework.getTaskManager();
        PluginManager pluginManager = framework.getPluginManager();

        String prefix = FileUtils.getTempPrefix("-pcomp");
        File directory = FileUtils.createTempDirectory(prefix);
        StgFormat format = StgFormat.getInstance();
        String stgFileExtension = format.getExtension();
        try {
            SubtaskMonitor<Object> subtaskMonitor = new SubtaskMonitor<>(monitor);
            Result<? extends ExportOutput> exportResult = null;
            Collection<Stg> stgs = new ArrayList<>();
            Collection<File> stgFiles = new ArrayList<>();
            ArrayList<Set<String>> allOutputSets = new ArrayList<>();
            for (WorkspaceEntry we: wes) {
                Stg stg = WorkspaceUtils.getAs(we, Stg.class);
                stgs.add(stg);
                Exporter stgExporter = ExportUtils.chooseBestExporter(pluginManager, stg, format);
                if (stgExporter == null) {
                    throw new NoExporterException(stg, format);
                }
                Set<String> outputSignalNames = stg.getSignalNames(Type.OUTPUT, null);
                allOutputSets.add(outputSignalNames);

                // Generating .g for the model
                File stgFile = new File(directory, we.getTitle() + stgFileExtension);
                stgFiles.add(stgFile);
                ExportTask exportTask = new ExportTask(stgExporter, stg, stgFile.getAbsolutePath());
                exportResult = taskManager.execute(
                        exportTask, "Exporting circuit .g", subtaskMonitor);

                if (exportResult.getOutcome() != Outcome.SUCCESS) {
                    if (exportResult.getOutcome() == Outcome.CANCEL) {
                        return new Result<MpsatChainOutput>(Outcome.CANCEL);
                    }
                    return new Result<MpsatChainOutput>(Outcome.FAILURE,
                            new MpsatChainOutput(exportResult, null, null, null, toolchainPreparationSettings));
                }
            }
            monitor.progressUpdate(0.30);

            // Generating .g for the whole system (model and environment)
            File stgFile = new File(directory, StgUtils.SYSTEM_FILE_PREFIX + stgFileExtension);
            File detailsFile = new File(directory, StgUtils.DETAIL_FILE_PREFIX + StgUtils.XML_FILE_EXTENSION);
            stgFile.deleteOnExit();
            PcompTask pcompTask = new PcompTask(stgFiles.toArray(new File[0]), stgFile, detailsFile,
                    ConversionMode.OUTPUT, false, false, directory);

            Result<? extends PcompOutput> pcompResult = taskManager.execute(
                    pcompTask, "Running parallel composition [PComp]", subtaskMonitor);

            if (pcompResult.getOutcome() != Outcome.SUCCESS) {
                if (pcompResult.getOutcome() == Outcome.CANCEL) {
                    return new Result<MpsatChainOutput>(Outcome.CANCEL);
                }
                return new Result<MpsatChainOutput>(Outcome.FAILURE,
                        new MpsatChainOutput(exportResult, pcompResult, null, null, toolchainPreparationSettings));
            }
            monitor.progressUpdate(0.50);

            // Generate unfolding
            File unfoldingFile = new File(directory, StgUtils.SYSTEM_FILE_PREFIX + PunfTask.PNML_FILE_EXTENSION);
            PunfTask punfTask = new PunfTask(stgFile.getAbsolutePath(), unfoldingFile.getAbsolutePath());
            Result<? extends PunfOutput> punfResult = taskManager.execute(
                    punfTask, "Unfolding .g", subtaskMonitor);

            if (punfResult.getOutcome() != Outcome.SUCCESS) {
                if (punfResult.getOutcome() == Outcome.CANCEL) {
                    return new Result<MpsatChainOutput>(Outcome.CANCEL);
                }
                return new Result<MpsatChainOutput>(Outcome.FAILURE,
                        new MpsatChainOutput(exportResult, pcompResult, punfResult, null, toolchainPreparationSettings));
            }
            monitor.progressUpdate(0.60);

            // Check for interface conformation
            CompositionData compositionData = new CompositionData(detailsFile);
            ArrayList<Set<String>> allPlaceSets = new ArrayList<>();
            for (File file: stgFiles) {
                ComponentData componentData = compositionData.getComponentData(file);
                Set<String> placeNames = componentData.getDstPlaces();
                allPlaceSets.add(placeNames);
            }

            MpsatParameters conformationSettings = MpsatParameters.getNwayConformationSettings(allPlaceSets, allOutputSets);
            MpsatTask mpsatConformationTask = new MpsatTask(conformationSettings.getMpsatArguments(directory),
                    unfoldingFile, directory, stgFile);
            Result<? extends MpsatOutput>  mpsatConformationResult = taskManager.execute(
                    mpsatConformationTask, "Running conformation check [MPSat]", subtaskMonitor);

            if (mpsatConformationResult.getOutcome() != Outcome.SUCCESS) {
                if (mpsatConformationResult.getOutcome() == Outcome.CANCEL) {
                    return new Result<MpsatChainOutput>(Outcome.CANCEL);
                }
                return new Result<MpsatChainOutput>(Outcome.FAILURE,
                        new MpsatChainOutput(exportResult, pcompResult, punfResult, mpsatConformationResult, conformationSettings));
            }
            monitor.progressUpdate(0.80);

            MpsatOutoutParser mpsatConformationParser = new MpsatOutoutParser(mpsatConformationResult.getPayload());
            if (!mpsatConformationParser.getSolutions().isEmpty()) {
                return new Result<MpsatChainOutput>(Outcome.SUCCESS,
                        new MpsatChainOutput(exportResult, pcompResult, punfResult, mpsatConformationResult, conformationSettings,
                                "This model does not conform to the environment."));
            }
            monitor.progressUpdate(1.0);

            // Success
            unfoldingFile.delete();
            String message = "All models conform to each other.";
            return new Result<MpsatChainOutput>(Outcome.SUCCESS,
                    new MpsatChainOutput(exportResult, pcompResult, punfResult, null, toolchainCompletionSettings, message));

        } catch (Throwable e) {
            return new Result<MpsatChainOutput>(e);
        } finally {
            FileUtils.deleteOnExitRecursively(directory);
        }
    }

}
