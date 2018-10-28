package org.workcraft.plugins.mpsat.tasks;

import org.workcraft.Framework;
import org.workcraft.plugins.mpsat.MpsatMode;
import org.workcraft.plugins.mpsat.MpsatParameters;
import org.workcraft.plugins.mpsat.utils.TransformUtils;
import org.workcraft.plugins.pcomp.ComponentData;
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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MpsatOutputDeterminacyTask implements Task<MpsatChainOutput> {

    private final MpsatParameters toolchainPreparationSettings = new MpsatParameters("Toolchain preparation of data",
            MpsatMode.UNDEFINED, 0, null, 0);

    private final MpsatParameters toolchainCompletionSettings = new MpsatParameters("Toolchain completion",
            MpsatMode.UNDEFINED, 0, null, 0);

    private final WorkspaceEntry we;

    public MpsatOutputDeterminacyTask(WorkspaceEntry we) {
        this.we = we;
    }

    @Override
    public Result<? extends MpsatChainOutput> run(ProgressMonitor<? super MpsatChainOutput> monitor) {
        Framework framework = Framework.getInstance();
        TaskManager taskManager = framework.getTaskManager();

        String prefix = FileUtils.getTempPrefix(we.getTitle());
        File directory = FileUtils.createTempDirectory(prefix);
        String stgFileExtension = StgFormat.getInstance().getExtension();
        try {
            // Clone STG before converting its internal signals to dummies
            ModelEntry me = framework.cloneModel(we.getModelEntry());
            Stg stg = WorkspaceUtils.getAs(me, Stg.class);
            Map<String, String> dummy2InternalRefs = StgUtils.convertInternalSignalsToDummies(stg);

            // Generating two copies of .g file for the model (dev and env)
            File devStgFile = new File(directory, StgUtils.DEVICE_FILE_PREFIX + stgFileExtension);
            Result<? extends ExportOutput> devExportResult = StgUtils.exportStg(stg, devStgFile, monitor);
            if (devExportResult.getOutcome() != Outcome.SUCCESS) {
                if (devExportResult.getOutcome() == Outcome.CANCEL) {
                    return new Result<>(Outcome.CANCEL);
                }
                return new Result<>(Outcome.FAILURE,
                        new MpsatChainOutput(devExportResult, null, null, null, toolchainPreparationSettings));
            }

            File envStgFile = new File(directory, StgUtils.ENVIRONMENT_FILE_PREFIX + stgFileExtension);
            Result<? extends ExportOutput> envExportResult = StgUtils.exportStg(stg, envStgFile, monitor);
            if (envExportResult.getOutcome() != Outcome.SUCCESS) {
                if (envExportResult.getOutcome() == Outcome.CANCEL) {
                    return new Result<>(Outcome.CANCEL);
                }
                return new Result<>(Outcome.FAILURE,
                        new MpsatChainOutput(envExportResult, null, null, null, toolchainPreparationSettings));
            }

            List<File> stgFiles = Arrays.asList(devStgFile, envStgFile);
            List<Map<String, String>> substitutes = Arrays.asList(dummy2InternalRefs, dummy2InternalRefs);
            Result<MultiSubExportOutput> multiExportResult = new Result<>(new MultiSubExportOutput(stgFiles, substitutes));
            monitor.progressUpdate(0.30);

            // Generating .g for the whole system (model and environment)
            File sysStgFile = new File(directory, StgUtils.SYSTEM_FILE_PREFIX + stgFileExtension);
            File detailFile = new File(directory, StgUtils.DETAIL_FILE_PREFIX + StgUtils.XML_FILE_EXTENSION);
            PcompTask pcompTask = new PcompTask(stgFiles.toArray(new File[0]), sysStgFile, detailFile,
                    ConversionMode.OUTPUT, true, false, directory);

            Result<? extends PcompOutput> pcompResult = taskManager.execute(
                    pcompTask, "Running parallel composition [PComp]", new SubtaskMonitor<>(monitor));

            if (pcompResult.getOutcome() != Outcome.SUCCESS) {
                if (pcompResult.getOutcome() == Outcome.CANCEL) {
                    return new Result<>(Outcome.CANCEL);
                }
                return new Result<>(Outcome.FAILURE,
                        new MpsatChainOutput(multiExportResult, pcompResult, null, null, toolchainPreparationSettings));
            }
            monitor.progressUpdate(0.50);

            // Insert shadow transitions into the composed STG
            CompositionData compositionData = new CompositionData(detailFile);
            ComponentData devComponentData = compositionData.getComponentData(devStgFile);
            Stg modSysStg = StgUtils.loadStg(sysStgFile);
            Set<String> devShadowTransitions = TransformUtils.generateShadowTransitions(modSysStg, devComponentData);
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
                        new MpsatChainOutput(modSysExportResult, pcompResult, punfResult, null, toolchainPreparationSettings));
            }
            monitor.progressUpdate(0.60);

            // Check for output determinacy
            MpsatParameters settings = MpsatParameters.getOutputDeterminacySettings(devShadowTransitions);
            MpsatTask mpsatConformationTask = new MpsatTask(settings.getMpsatArguments(directory),
                    unfoldingFile, directory, sysStgFile);
            Result<? extends MpsatOutput>  mpsatConformationResult = taskManager.execute(
                    mpsatConformationTask, "Running output determinacy check [MPSat]", new SubtaskMonitor<>(monitor));

            if (mpsatConformationResult.getOutcome() != Outcome.SUCCESS) {
                if (mpsatConformationResult.getOutcome() == Outcome.CANCEL) {
                    return new Result<>(Outcome.CANCEL);
                }
                return new Result<>(Outcome.FAILURE,
                        new MpsatChainOutput(modSysExportResult, pcompResult, punfResult, mpsatConformationResult, settings));
            }
            monitor.progressUpdate(0.80);

            MpsatOutputParser mpsatConformationParser = new MpsatOutputParser(mpsatConformationResult.getPayload());
            if (!mpsatConformationParser.getSolutions().isEmpty()) {
                return new Result<>(Outcome.SUCCESS,
                        new MpsatChainOutput(multiExportResult, pcompResult, punfResult, mpsatConformationResult, settings,
                                "This model does is not output determinate."));
            }
            monitor.progressUpdate(1.0);

            // Success
            return new Result<>(Outcome.SUCCESS,
                    new MpsatChainOutput(multiExportResult, pcompResult, punfResult, null,
                            toolchainCompletionSettings, "Output determinacy holds."));

        } catch (Throwable e) {
            return new Result<>(e);
        } finally {
            FileUtils.deleteOnExitRecursively(directory);
        }
    }

}
