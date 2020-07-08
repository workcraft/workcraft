package org.workcraft.plugins.mpsat_verification.tasks;

import org.workcraft.Framework;
import org.workcraft.plugins.mpsat_verification.presets.VerificationParameters;
import org.workcraft.plugins.mpsat_verification.utils.ReachUtils;
import org.workcraft.plugins.mpsat_verification.utils.TransformUtils;
import org.workcraft.plugins.pcomp.CompositionData;
import org.workcraft.plugins.pcomp.tasks.PcompOutput;
import org.workcraft.plugins.pcomp.tasks.PcompParameters;
import org.workcraft.plugins.pcomp.tasks.PcompTask;
import org.workcraft.plugins.punf.tasks.PunfOutput;
import org.workcraft.plugins.punf.tasks.PunfTask;
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

public class NwayConformationTask implements Task<VerificationChainOutput> {

    private final List<WorkspaceEntry> wes;

    public NwayConformationTask(List<WorkspaceEntry> wes) {
        this.wes = wes;
    }

    @Override
    public Result<? extends VerificationChainOutput> run(ProgressMonitor<? super VerificationChainOutput> monitor) {
        Framework framework = Framework.getInstance();
        TaskManager taskManager = framework.getTaskManager();

        String prefix = FileUtils.getTempPrefix("-nway_conformation");
        File directory = FileUtils.createTempDirectory(prefix);
        String stgFileExtension = StgFormat.getInstance().getExtension();
        VerificationParameters preparationParameters = ReachUtils.getToolchainPreparationParameters();
        try {
            List<File> stgFiles = new ArrayList<>();
            List<Map<String, String>> substitutionsList = new ArrayList<>();
            for (WorkspaceEntry we: wes) {
                // Clone STG before converting its internal signals to dummies
                ModelEntry me = WorkUtils.cloneModel(we.getModelEntry());
                Stg stg = WorkspaceUtils.getAs(me, Stg.class);
                Map<String, String> substitutions = new HashMap<>();
                StgUtils.convertInternalSignalsToDummies(stg, substitutions);
                substitutionsList.add(substitutions);

                // Generating .g for the model
                File stgFile = new File(directory, we.getTitle() + stgFileExtension);
                stgFiles.add(stgFile);

                Result<? extends ExportOutput> exportResult = StgUtils.exportStg(stg, stgFile, monitor);
                if (!exportResult.isSuccess()) {
                    if (exportResult.isCancel()) {
                        return Result.cancel();
                    }
                    return Result.failure(new VerificationChainOutput(
                            exportResult, null, null, null, preparationParameters));
                }
            }
            Result<MultiSubExportOutput> multiExportResult = new Result<>(new MultiSubExportOutput(stgFiles, substitutionsList));
            monitor.progressUpdate(0.20);

            // Generating .g for the whole system (model and environment)
            PcompParameters pcompParameters = new PcompParameters(PcompParameters.SharedSignalMode.OUTPUT, false, false);
            PcompTask pcompTask = new PcompTask(stgFiles, pcompParameters, directory);

            Result<? extends PcompOutput> pcompResult = taskManager.execute(
                    pcompTask, "Running parallel composition [PComp]", new SubtaskMonitor<>(monitor));

            if (!pcompResult.isSuccess()) {
                if (pcompResult.isCancel()) {
                    return Result.cancel();
                }
                return Result.failure(new VerificationChainOutput(
                        multiExportResult, pcompResult, null, null, preparationParameters));
            }
            monitor.progressUpdate(0.40);

            // Insert shadow transitions into the composed STG
            File sysStgFile = pcompResult.getPayload().getOutputFile();
            File detailFile = pcompResult.getPayload().getDetailFile();
            CompositionData compositionData = new CompositionData(detailFile);
            Stg modSysStg = StgUtils.importStg(sysStgFile);
            Set<String> shadowTransitions = new HashSet<>();
            TransformUtils.generateShadows(modSysStg, compositionData, shadowTransitions);
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

            // Check for conformation
            VerificationParameters verificationParameters = ReachUtils.getNwayConformationParameters(shadowTransitions);
            // Store system STG WITHOUT shadow transitions -- this is important for interpretation of violation traces
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
            monitor.progressUpdate(0.90);

            if (mpsatResult.getPayload().hasSolutions()) {
                return Result.success(new VerificationChainOutput(
                        multiExportResult, pcompResult, punfResult, mpsatResult, verificationParameters,
                        "This model does not conform to the environment."));
            }
            monitor.progressUpdate(1.0);

            // Success
            return Result.success(new VerificationChainOutput(
                    multiExportResult, pcompResult, punfResult, mpsatResult, verificationParameters,
                    "N-way conformation holds."));

        } catch (Throwable e) {
            return new Result<>(e);
        } finally {
            FileUtils.deleteOnExitRecursively(directory);
        }
    }

}
