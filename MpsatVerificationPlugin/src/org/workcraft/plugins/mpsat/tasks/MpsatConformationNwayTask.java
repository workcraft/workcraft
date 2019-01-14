package org.workcraft.plugins.mpsat.tasks;

import org.workcraft.Framework;
import org.workcraft.plugins.mpsat.MpsatParameters;
import org.workcraft.plugins.mpsat.utils.TransformUtils;
import org.workcraft.plugins.pcomp.CompositionData;
import org.workcraft.plugins.pcomp.tasks.PcompOutput;
import org.workcraft.plugins.pcomp.tasks.PcompTask;
import org.workcraft.plugins.pcomp.tasks.PcompTask.ConversionMode;
import org.workcraft.plugins.punf.tasks.PunfOutput;
import org.workcraft.plugins.punf.tasks.PunfTask;
import org.workcraft.plugins.shared.tasks.ExportOutput;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.interop.StgFormat;
import org.workcraft.plugins.stg.utils.StgUtils;
import org.workcraft.tasks.*;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.util.FileUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MpsatConformationNwayTask implements Task<MpsatChainOutput> {

    private final ArrayList<WorkspaceEntry> wes;

    public MpsatConformationNwayTask(ArrayList<WorkspaceEntry> wes) {
        this.wes = wes;
    }

    @Override
    public Result<? extends MpsatChainOutput> run(ProgressMonitor<? super MpsatChainOutput> monitor) {
        Framework framework = Framework.getInstance();
        TaskManager taskManager = framework.getTaskManager();

        String prefix = FileUtils.getTempPrefix("-pcomp");
        File directory = FileUtils.createTempDirectory(prefix);
        String stgFileExtension = StgFormat.getInstance().getExtension();
        MpsatParameters preparationSettings = MpsatParameters.getToolchainPreparationSettings();
        try {
            List<File> stgFiles = new ArrayList<>();
            List<Map<String, String>> substitutes = new ArrayList<>();
            for (WorkspaceEntry we: wes) {
                // Clone STG before converting its internal signals to dummies
                ModelEntry me = framework.cloneModel(we.getModelEntry());
                Stg stg = WorkspaceUtils.getAs(me, Stg.class);
                Map<String, String> dummy2InternalRefs = StgUtils.convertInternalSignalsToDummies(stg);
                substitutes.add(dummy2InternalRefs);

                // Generating .g for the model
                File stgFile = new File(directory, we.getTitle() + stgFileExtension);
                stgFiles.add(stgFile);

                Result<? extends ExportOutput> exportResult = StgUtils.exportStg(stg, stgFile, monitor);
                if (exportResult.getOutcome() != Outcome.SUCCESS) {
                    if (exportResult.getOutcome() == Outcome.CANCEL) {
                        return new Result<>(Outcome.CANCEL);
                    }
                    return new Result<>(Outcome.FAILURE,
                            new MpsatChainOutput(exportResult, null, null, null, preparationSettings));
                }
            }
            Result<MultiSubExportOutput> multiExportResult = new Result<>(new MultiSubExportOutput(stgFiles, substitutes));
            monitor.progressUpdate(0.30);

            // Generating .g for the whole system (model and environment)
            File sysStgFile = new File(directory, StgUtils.SYSTEM_FILE_PREFIX + stgFileExtension);
            File detailFile = new File(directory, StgUtils.DETAIL_FILE_PREFIX + StgUtils.XML_FILE_EXTENSION);
            PcompTask pcompTask = new PcompTask(stgFiles.toArray(new File[0]), sysStgFile, detailFile,
                    ConversionMode.OUTPUT, false, false, directory);

            Result<? extends PcompOutput> pcompResult = taskManager.execute(
                    pcompTask, "Running parallel composition [PComp]", new SubtaskMonitor<>(monitor));

            if (pcompResult.getOutcome() != Outcome.SUCCESS) {
                if (pcompResult.getOutcome() == Outcome.CANCEL) {
                    return new Result<>(Outcome.CANCEL);
                }
                return new Result<>(Outcome.FAILURE,
                        new MpsatChainOutput(multiExportResult, pcompResult, null, null, preparationSettings));
            }
            monitor.progressUpdate(0.50);

            // Insert shadow transitions into the composed STG
            CompositionData compositionData = new CompositionData(detailFile);
            Stg modSysStg = StgUtils.loadStg(sysStgFile);
            Set<String> shadowTransitions = TransformUtils.generateShadowTransitions(modSysStg, compositionData);
            File modSysStgFile = new File(directory, StgUtils.SYSTEM_FILE_PREFIX + StgUtils.MODIFIED_FILE_SUFFIX + stgFileExtension);
            Result<? extends ExportOutput> modSysExportResult = StgUtils.exportStg(modSysStg, modSysStgFile, monitor);

            // Generate unfolding
            File unfoldingFile = new File(directory, StgUtils.SYSTEM_FILE_PREFIX + StgUtils.MODIFIED_FILE_SUFFIX + PunfTask.PNML_FILE_EXTENSION);
            PunfTask punfTask = new PunfTask(modSysStgFile, unfoldingFile, directory);
            Result<? extends PunfOutput> punfResult = taskManager.execute(
                    punfTask, "Unfolding .g", new SubtaskMonitor<>(monitor));

            if (punfResult.getOutcome() != Outcome.SUCCESS) {
                if (punfResult.getOutcome() == Outcome.CANCEL) {
                    return new Result<>(Outcome.CANCEL);
                }
                return new Result<>(Outcome.FAILURE,
                        new MpsatChainOutput(modSysExportResult, pcompResult, punfResult, null, preparationSettings));
            }
            monitor.progressUpdate(0.60);

            // Check for conformation
            MpsatParameters mpsatSettings = MpsatParameters.getConformationNwaySettings(shadowTransitions);
            MpsatTask mpsatTask = new MpsatTask(mpsatSettings.getMpsatArguments(directory),
                    unfoldingFile, directory, sysStgFile);
            Result<? extends MpsatOutput>  mpsatResult = taskManager.execute(
                    mpsatTask, "Running conformation check [MPSat]", new SubtaskMonitor<>(monitor));

            if (mpsatResult.getOutcome() != Outcome.SUCCESS) {
                if (mpsatResult.getOutcome() == Outcome.CANCEL) {
                    return new Result<>(Outcome.CANCEL);
                }
                return new Result<>(Outcome.FAILURE,
                        new MpsatChainOutput(modSysExportResult, pcompResult, punfResult, mpsatResult, mpsatSettings));
            }
            monitor.progressUpdate(0.80);

            MpsatOutputParser mpsatParser = new MpsatOutputParser(mpsatResult.getPayload());
            if (!mpsatParser.getSolutions().isEmpty()) {
                return new Result<>(Outcome.SUCCESS,
                        new MpsatChainOutput(multiExportResult, pcompResult, punfResult, mpsatResult, mpsatSettings,
                                "This model does not conform to the environment."));
            }
            monitor.progressUpdate(1.0);

            // Success
            String message = "N-way conformation holds.";
            return new Result<>(Outcome.SUCCESS,
                    new MpsatChainOutput(multiExportResult, pcompResult, punfResult, mpsatResult, mpsatSettings, message));

        } catch (Throwable e) {
            return new Result<>(e);
        } finally {
            FileUtils.deleteOnExitRecursively(directory);
        }
    }

}
