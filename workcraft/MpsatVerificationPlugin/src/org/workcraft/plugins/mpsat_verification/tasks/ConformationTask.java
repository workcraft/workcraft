package org.workcraft.plugins.mpsat_verification.tasks;

import org.workcraft.Framework;
import org.workcraft.plugins.mpsat_verification.presets.VerificationParameters;
import org.workcraft.plugins.mpsat_verification.utils.ReachUtils;
import org.workcraft.plugins.pcomp.CompositionData;
import org.workcraft.plugins.pcomp.tasks.PcompOutput;
import org.workcraft.plugins.pcomp.utils.PcompUtils;
import org.workcraft.plugins.punf.tasks.PunfOutput;
import org.workcraft.plugins.punf.tasks.PunfTask;
import org.workcraft.plugins.stg.Signal;
import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.plugins.stg.interop.StgFormat;
import org.workcraft.plugins.stg.utils.StgUtils;
import org.workcraft.tasks.*;
import org.workcraft.utils.FileUtils;
import org.workcraft.utils.WorkUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ConformationTask implements Task<VerificationChainOutput> {

    private static final String STG_FILE_EXTENSION = StgFormat.getInstance().getExtension();
    private static final String DEV_STG_FILE_NAME = StgUtils.DEVICE_FILE_PREFIX + STG_FILE_EXTENSION;
    private static final String ENV_STG_FILE_NAME = StgUtils.ENVIRONMENT_FILE_PREFIX + STG_FILE_EXTENSION;
    private static final String MOD_SYS_STG_FILE_NAME = StgUtils.SYSTEM_FILE_PREFIX + StgUtils.MODIFIED_FILE_SUFFIX + STG_FILE_EXTENSION;
    private static final String MOD_UNFOLDING_FILE_NAME = StgUtils.SYSTEM_FILE_PREFIX + StgUtils.MODIFIED_FILE_SUFFIX + PunfTask.PNML_FILE_EXTENSION;

    private final WorkspaceEntry we;
    private final File envFile;

    public ConformationTask(WorkspaceEntry we, File envFile) {
        this.we = we;
        this.envFile = envFile;
    }

    @Override
    public Result<? extends VerificationChainOutput> run(ProgressMonitor<? super VerificationChainOutput> monitor) {
        File directory = FileUtils.createTempDirectory(FileUtils.getTempPrefix(we.getTitle()));
        Chain<VerificationChainOutput> chain = new Chain<>(this::init, monitor);
        chain.andOnSuccess(payload -> exportDevStg(payload, monitor, directory), 0.1);
        chain.andOnSuccess(payload -> exportEnvStg(payload, monitor, directory), 0.2);
        chain.andOnSuccess(payload -> composeSysStg(payload, monitor, directory), 0.4);
        chain.andOnSuccess(payload -> exportShadowSysStg(payload, monitor, directory), 0.6);
        chain.andOnSuccess(payload -> unfoldSysStg(payload, monitor, directory), 0.8);
        chain.andOnSuccess(payload -> verifyProperty(payload, monitor, directory), 1.0);
        chain.andThen(() -> FileUtils.deleteOnExitRecursively(directory));
        return chain.process();
    }

    private Result<? extends VerificationChainOutput> init() {
        if (!WorkspaceUtils.isApplicable(we, StgModel.class)) {
            return Result.exception("Incorrect model type");
        }

        if (envFile == null) {
            return Result.exception("Environment STG is undefined");
        }

        VerificationParameters verificationParameters = ReachUtils.getToolchainPreparationParameters();
        return Result.success(new VerificationChainOutput().applyVerificationParameters(verificationParameters));
    }

    private Result<? extends VerificationChainOutput> exportDevStg(VerificationChainOutput payload,
            ProgressMonitor<? super VerificationChainOutput> monitor, File directory) {

        // Clone STG before converting its internal signals to dummies
        ModelEntry me = WorkUtils.cloneModel(we.getModelEntry());
        Stg devStg = WorkspaceUtils.getAs(me, Stg.class);

        // Convert internal signals of the device STG to dummies and keep track of renaming
        Map<String, String> devSubstitutions = StgUtils.convertInternalSignalsToDummies(devStg);

        File devStgFile = new File(directory, DEV_STG_FILE_NAME);
        Result<? extends ExportOutput> devExportResult = StgUtils.exportStg(devStg, devStgFile, monitor);
        Result<SubExportOutput> exportResult = new Result<>(new SubExportOutput(devStgFile, devSubstitutions));
        return new Result<>(devExportResult.getOutcome(), payload.applyExportResult(exportResult));
    }

    private Result<? extends VerificationChainOutput> exportEnvStg(VerificationChainOutput payload,
            ProgressMonitor<? super VerificationChainOutput> monitor, File directory) {

        Stg envStg = StgUtils.loadStg(envFile);
        if (envStg == null) {
            return Result.exception("Cannot load environment STG from file '" + envFile.getAbsolutePath() + "'");
        }

        // Make sure that signal types of the environment STG match those of the device STG
        Stg devStg = StgUtils.importStg(new File(directory, DEV_STG_FILE_NAME));
        StgUtils.restoreInterfaceSignals(envStg,
                devStg.getSignalReferences(Signal.Type.INPUT),
                devStg.getSignalReferences(Signal.Type.OUTPUT));

        // Convert internal signals of the environment STG to dummies
        StgUtils.convertInternalSignalsToDummies(envStg);

        File envStgFile = new File(directory, ENV_STG_FILE_NAME);
        Result<? extends ExportOutput> envExportResult = StgUtils.exportStg(envStg, envStgFile, monitor);
        return new Result<>(envExportResult.getOutcome(), payload);
    }

    private Result<? extends VerificationChainOutput> composeSysStg(VerificationChainOutput payload,
            ProgressMonitor<? super VerificationChainOutput> monitor, File directory) {

        // Generating composition STG for the whole system (device and environment)
        File devStgFile = new File(directory, DEV_STG_FILE_NAME);
        File envStgFile = new File(directory, ENV_STG_FILE_NAME);
        Result<? extends PcompOutput> pcompResult = PcompUtils.composeDevWithEnv(
                devStgFile, envStgFile, directory, monitor);

        return new Result<>(pcompResult.getOutcome(), payload.applyPcompResult(pcompResult));
    }

    private Result<? extends VerificationChainOutput> exportShadowSysStg(VerificationChainOutput payload,
            ProgressMonitor<? super VerificationChainOutput> monitor, File directory) {

        PcompOutput pcompOutput = payload.getPcompResult().getPayload();
        CompositionData compositionData;
        try {
            compositionData = new CompositionData(pcompOutput.getDetailFile());
        } catch (FileNotFoundException e) {
            return Result.exception(e);
        }

        // Insert shadow transitions into the composed STG
        Stg sysStg = StgUtils.importStg(pcompOutput.getOutputFile());
        CompositionTransformer transformer = new CompositionTransformer(sysStg, compositionData);
        File devStgFile = new File(directory, DEV_STG_FILE_NAME);
        Collection<SignalTransition> shadowTransitions = transformer.insetShadowTransitions(devStgFile);

        File modSysStgFile = new File(directory, MOD_SYS_STG_FILE_NAME);
        Result<? extends ExportOutput> modSysExportResult = StgUtils.exportStg(sysStg, modSysStgFile, monitor);
        Set<String> shadowTransitionRefs = shadowTransitions.stream()
                .map(sysStg::getNodeReference)
                .collect(Collectors.toSet());

        VerificationParameters verificationParameters = ReachUtils.getConformationParameters(shadowTransitionRefs);
        return new Result<>(modSysExportResult.getOutcome(), payload.applyVerificationParameters(verificationParameters));
    }

    private Result<? extends VerificationChainOutput> unfoldSysStg(VerificationChainOutput payload,
            ProgressMonitor<? super VerificationChainOutput> monitor, File directory) {

        File modSysStgFile = new File(directory, MOD_SYS_STG_FILE_NAME);
        File unfoldingFile = new File(directory, MOD_UNFOLDING_FILE_NAME);
        PunfTask punfTask = new PunfTask(modSysStgFile, unfoldingFile, directory);
        Result<? extends PunfOutput> punfResult = Framework.getInstance().getTaskManager().execute(
                punfTask, "Unfolding .g", new SubtaskMonitor<>(monitor));

        return new Result<>(punfResult.getOutcome(), payload.applyPunfResult(punfResult));
    }

    private Result<? extends VerificationChainOutput> verifyProperty(VerificationChainOutput payload,
            ProgressMonitor<? super VerificationChainOutput> monitor, File directory) {

        File unfoldingFile = payload.getPunfResult().getPayload().getOutputFile();
        // Store system STG WITHOUT shadow transitions -- this is important for interpretation of violation traces
        File sysStgFile = payload.getPcompResult().getPayload().getOutputFile();
        VerificationParameters verificationParameters = payload.getVerificationParameters();
        MpsatTask mpsatTask = new MpsatTask(unfoldingFile, sysStgFile, verificationParameters, directory);
        Result<? extends MpsatOutput>  mpsatResult = Framework.getInstance().getTaskManager().execute(
                mpsatTask, "Running conformation check [MPSat]", new SubtaskMonitor<>(monitor));

        String message = null;
        if (mpsatResult.isSuccess()) {
            message = mpsatResult.getPayload().hasSolutions()
                    ? "This model does not conform to the environment."
                    : "The model conforms to its environment (" + envFile.getName() + ").";
        }

        return new Result<>(mpsatResult.getOutcome(), payload.applyMpsatResult(mpsatResult).applyMessage(message));
    }

}
