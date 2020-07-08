package org.workcraft.plugins.mpsat_verification.tasks;

import org.workcraft.Framework;
import org.workcraft.plugins.mpsat_verification.presets.VerificationParameters;
import org.workcraft.plugins.mpsat_verification.utils.ReachUtils;
import org.workcraft.plugins.mpsat_verification.utils.TransformUtils;
import org.workcraft.plugins.pcomp.ComponentData;
import org.workcraft.plugins.pcomp.CompositionData;
import org.workcraft.plugins.pcomp.tasks.PcompOutput;
import org.workcraft.plugins.pcomp.tasks.PcompParameters;
import org.workcraft.plugins.pcomp.tasks.PcompTask;
import org.workcraft.plugins.punf.tasks.PunfOutput;
import org.workcraft.plugins.punf.tasks.PunfTask;
import org.workcraft.plugins.stg.Signal;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.interop.StgFormat;
import org.workcraft.plugins.stg.utils.StgUtils;
import org.workcraft.tasks.*;
import org.workcraft.utils.FileUtils;
import org.workcraft.utils.WorkUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

import java.io.File;
import java.util.*;

public class ConformationTask implements Task<VerificationChainOutput> {

    private final WorkspaceEntry we;
    private final File envFile;

    public ConformationTask(WorkspaceEntry we, File envFile) {
        this.we = we;
        this.envFile = envFile;
    }

    @Override
    public Result<? extends VerificationChainOutput> run(ProgressMonitor<? super VerificationChainOutput> monitor) {
        TaskManager taskManager = Framework.getInstance().getTaskManager();
        String prefix = FileUtils.getTempPrefix(we.getTitle());
        File directory = FileUtils.createTempDirectory(prefix);
        StgFormat format = StgFormat.getInstance();
        String stgFileExtension = format.getExtension();
        VerificationParameters preparationParameters = ReachUtils.getToolchainPreparationParameters();
        try {
            // Clone STG before converting its internal signals to dummies
            ModelEntry me = WorkUtils.cloneModel(we.getModelEntry());
            Stg devStg = WorkspaceUtils.getAs(me, Stg.class);
            // Convert internal signals of the device STG to dummies and keep track of renaming
            Map<String, String> devSubstitutions = new HashMap<>();
            StgUtils.convertInternalSignalsToDummies(devStg, devSubstitutions);

            File devStgFile = new File(directory, StgUtils.DEVICE_FILE_PREFIX + stgFileExtension);
            Result<? extends ExportOutput> devExportResult = StgUtils.exportStg(devStg, devStgFile, monitor);
            if (!devExportResult.isSuccess()) {
                if (devExportResult.isCancel()) {
                    return Result.cancel();
                }
                return Result.failure(new VerificationChainOutput(
                        devExportResult, null, null, null, preparationParameters));
            }
            monitor.progressUpdate(0.30);

            // Generating .g for the environment
            Stg envStg = StgUtils.loadStg(envFile);
            if (envStg == null) {
                return Result.failure(new VerificationChainOutput(
                        null, null, null, null, preparationParameters));
            }
            // Convert internal signals of the environment STG to dummies
            StgUtils.convertInternalSignalsToDummies(envStg);

            // Make sure that signal types of the environment STG match those of the device STG
            StgUtils.restoreInterfaceSignals(envStg,
                    devStg.getSignalReferences(Signal.Type.INPUT),
                    devStg.getSignalReferences(Signal.Type.OUTPUT));

            File envStgFile = new File(directory, StgUtils.ENVIRONMENT_FILE_PREFIX + stgFileExtension);
            Result<? extends ExportOutput> envExportResult = StgUtils.exportStg(envStg, envStgFile, monitor);

            if (!envExportResult.isSuccess()) {
                if (envExportResult.isCancel()) {
                    return Result.cancel();
                }
                return Result.failure(new VerificationChainOutput(
                        envExportResult, null, null, null, preparationParameters));
            }
            monitor.progressUpdate(0.40);

            // Generating .g for the whole system (model and environment)
            PcompParameters pcompParameters = new PcompParameters(PcompParameters.SharedSignalMode.OUTPUT, true, false);
            PcompTask pcompTask = new PcompTask(Arrays.asList(devStgFile, envStgFile), pcompParameters, directory);

            Result<? extends PcompOutput> pcompResult = taskManager.execute(
                    pcompTask, "Running parallel composition [PComp]", new SubtaskMonitor<>(monitor));

            if (!pcompResult.isSuccess()) {
                if (pcompResult.isCancel()) {
                    return Result.cancel();
                }
                return Result.failure(new VerificationChainOutput(
                        devExportResult, pcompResult, null, null, preparationParameters));
            }
            monitor.progressUpdate(0.50);

            // Insert shadow transitions into the composed STG
            File sysStgFile = pcompResult.getPayload().getOutputFile();
            File detailFile = pcompResult.getPayload().getDetailFile();
            CompositionData compositionData = new CompositionData(detailFile);
            Stg modSysStg = StgUtils.importStg(sysStgFile);
            Set<String> shadowTransitions = new HashSet<>();
            ComponentData componentData = compositionData.getComponentData(devStgFile);
            TransformUtils.generateShadows(modSysStg, componentData, shadowTransitions);
            File modSysStgFile = new File(directory, StgUtils.SYSTEM_FILE_PREFIX + StgUtils.MODIFIED_FILE_SUFFIX + stgFileExtension);
            Result<? extends ExportOutput> modSysExportResult = StgUtils.exportStg(modSysStg, modSysStgFile, monitor);

            // Generate unfolding
            File unfoldingFile = new File(directory, StgUtils.SYSTEM_FILE_PREFIX + StgUtils.MODIFIED_FILE_SUFFIX + PunfTask.PNML_FILE_EXTENSION);
            PunfTask punfTask = new PunfTask(modSysStgFile, unfoldingFile, directory);
            Result<? extends PunfOutput> punfResult = taskManager.execute(
                    punfTask, "Unfolding .g", new SubtaskMonitor<>(monitor));

            if (!punfResult.isSuccess()) {
                if (punfResult.isCancel()) {
                    return Result.cancel();
                }
                return Result.failure(new VerificationChainOutput(
                        modSysExportResult, pcompResult, punfResult, null, preparationParameters));
            }
            monitor.progressUpdate(0.60);

            // Store system STG WITHOUT shadow transitions -- this is important for interpretation of violation traces
            VerificationParameters verificationParameters = ReachUtils.getConformationParameters(shadowTransitions);
            MpsatTask mpsatTask = new MpsatTask(unfoldingFile, sysStgFile, verificationParameters, directory);
            Result<? extends MpsatOutput>  mpsatResult = taskManager.execute(
                    mpsatTask, "Running conformation check [MPSat]", new SubtaskMonitor<>(monitor));

            if (!mpsatResult.isSuccess()) {
                if (mpsatResult.isCancel()) {
                    return Result.cancel();
                }
                return Result.failure(new VerificationChainOutput(
                        modSysExportResult, pcompResult, punfResult, mpsatResult, verificationParameters));
            }
            monitor.progressUpdate(0.80);

            Result<SubExportOutput> exportResult = new Result<>(new SubExportOutput(devStgFile, devSubstitutions));
            if (mpsatResult.getPayload().hasSolutions()) {
                return Result.success(new VerificationChainOutput(
                        exportResult, pcompResult, punfResult, mpsatResult, verificationParameters,
                        "This model does not conform to the environment."));
            }
            monitor.progressUpdate(1.0);

            // Success
            return Result.success(new VerificationChainOutput(
                    exportResult, pcompResult, punfResult, mpsatResult, verificationParameters,
                    "The model conforms to its environment (" + envFile.getName() + ")."));

        } catch (Throwable e) {
            return Result.exception(e);
        } finally {
            FileUtils.deleteOnExitRecursively(directory);
        }
    }

}
