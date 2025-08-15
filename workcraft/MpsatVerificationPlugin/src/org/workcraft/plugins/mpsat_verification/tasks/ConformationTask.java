package org.workcraft.plugins.mpsat_verification.tasks;

import org.workcraft.Framework;
import org.workcraft.plugins.mpsat_verification.presets.VerificationParameters;
import org.workcraft.plugins.mpsat_verification.utils.CompositionUtils;
import org.workcraft.plugins.mpsat_verification.utils.ReachUtils;
import org.workcraft.plugins.pcomp.CompositionData;
import org.workcraft.plugins.pcomp.tasks.PcompOutput;
import org.workcraft.plugins.pcomp.tasks.PcompParameters;
import org.workcraft.plugins.pcomp.tasks.PcompTask;
import org.workcraft.plugins.stg.*;
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
import java.util.*;

public class ConformationTask implements Task<VerificationChainOutput> {

    private static final String STG_FILE_EXTENSION = StgFormat.getInstance().getExtension();
    private static final String DEV_STG_FILE_NAME = StgUtils.DEVICE_FILE_PREFIX + STG_FILE_EXTENSION;
    private static final String ENV_STG_FILE_NAME = StgUtils.ENVIRONMENT_FILE_PREFIX + STG_FILE_EXTENSION;
    private static final String MOD_SYS_STG_FILE_NAME = StgUtils.SYSTEM_FILE_PREFIX + StgUtils.MODIFIED_FILE_SUFFIX + STG_FILE_EXTENSION;

    private static final String RISING_PHASE_PREFIX = "Rising phase for signal '";
    private static final String FALLING_PHASE_PREFIX = "Falling phase for signal '";
    private static final String ENV_MISSING_PHASE_SUFFIX = "' is present in environment STG but not in the current model";
    private static final String DEV_MISSING_PHASE_SUFFIX = "' is present in the current model but not in environment STG";

    private final WorkspaceEntry we;
    private final File envFile;

    public ConformationTask(WorkspaceEntry we, File envFile) {
        this.we = we;
        this.envFile = envFile;
    }

    @Override
    public Result<? extends VerificationChainOutput> run(ProgressMonitor<? super VerificationChainOutput> monitor) {
        Result<? extends VerificationChainOutput> result = checkTrivialCases();
        if (result == null) {
            File directory = FileUtils.createTempDirectory(FileUtils.getTempPrefix(we.getTitle()));
            Chain<VerificationChainOutput> chain = new Chain<>(this::init, monitor);
            chain.andOnSuccess(payload -> exportInterfaces(payload, monitor, directory), 0.1);
            chain.andOnSuccess(payload -> composeInterfaces(payload, monitor, directory), 0.2);
            chain.andOnSuccess(payload -> exportComposition(payload, monitor, directory), 0.3);
            chain.andOnSuccess(payload -> verifyProperty(payload, monitor, directory), 1.0);
            chain.andThen(() -> FileUtils.deleteOnExitRecursively(directory));
            result = chain.process();
        }
        return result;
    }

    private Result<? extends VerificationChainOutput> checkTrivialCases() {
        if (!WorkspaceUtils.isApplicable(we, StgModel.class)) {
            return Result.exception("Incorrect model type");
        }

        if (envFile == null) {
            return Result.exception("Environment STG is undefined");
        }

        return null;
    }

    private Result<? extends VerificationChainOutput> init() {
        VerificationParameters verificationParameters = ReachUtils.getToolchainPreparationParameters();
        return Result.success(new VerificationChainOutput().applyVerificationParameters(verificationParameters));
    }

    private Result<? extends VerificationChainOutput> exportInterfaces(VerificationChainOutput payload,
            ProgressMonitor<? super VerificationChainOutput> monitor, File directory) {

        Stg envStg = StgUtils.loadOrImportStg(envFile);
        if (envStg == null) {
            return Result.exception("Cannot load environment STG from file '" + envFile.getAbsolutePath() + "'");
        }

        // Clone device STG as its internal signals will be converted to dummies
        ModelEntry me = WorkUtils.cloneModel(we.getModelEntry());
        Stg devStg = WorkspaceUtils.getAs(me, Stg.class);

        // Make sure that signal types of the environment STG match those of the device STG
        Set<String> devInputs = devStg.getSignalReferences(Signal.Type.INPUT);
        Set<String> devOutputs = devStg.getSignalReferences(Signal.Type.OUTPUT);
        StgUtils.restoreInterfaceSignals(envStg, devInputs, devOutputs);

        // Check that the same phases of signals are present in device and environment STGs
        Set<String> interfaceSignalRefs = new HashSet<>();
        interfaceSignalRefs.addAll(devInputs);
        interfaceSignalRefs.addAll(devOutputs);
        for (String signalRef : interfaceSignalRefs) {
            if (hasSignalPhase(devStg, signalRef, true) && missesSignalPhase(envStg, signalRef, true)) {
                return Result.exception(RISING_PHASE_PREFIX + signalRef + ENV_MISSING_PHASE_SUFFIX);
            }
            if (hasSignalPhase(devStg, signalRef, false) && missesSignalPhase(envStg, signalRef, false)) {
                return Result.exception(FALLING_PHASE_PREFIX + signalRef + ENV_MISSING_PHASE_SUFFIX);
            }
            if (hasSignalPhase(envStg, signalRef, true) && missesSignalPhase(devStg, signalRef, true)) {
                return Result.exception(RISING_PHASE_PREFIX + signalRef + DEV_MISSING_PHASE_SUFFIX);
            }
            if (hasSignalPhase(envStg, signalRef, false) && missesSignalPhase(devStg, signalRef, false)) {
                return Result.exception(FALLING_PHASE_PREFIX + signalRef + DEV_MISSING_PHASE_SUFFIX);
            }
        }

        // Export environment STG (convert internal signals to dummies and keep track of renaming)
        Map<String, String> envSubstitutions = StgUtils.convertInternalSignalsToDummies(envStg);
        File envStgFile = new File(directory, ENV_STG_FILE_NAME);
        Result<? extends ExportOutput> envExportResult = StgUtils.exportStg(envStg, envStgFile, monitor);
        if (!envExportResult.isSuccess()) {
            return new Result<>(envExportResult.getOutcome(), payload);
        }

        // Export device STG (convert internal signals to dummies and keep track of renaming)
        Map<String, String> devSubstitutions = StgUtils.convertInternalSignalsToDummies(devStg);
        File devStgFile = new File(directory, DEV_STG_FILE_NAME);
        Result<? extends ExportOutput> devExportResult = StgUtils.exportStg(devStg, devStgFile, monitor);
        if (!devExportResult.isSuccess()) {
            return new Result<>(devExportResult.getOutcome(), payload);
        }

        ExtendedExportOutput extendedExportOutput = new ExtendedExportOutput();
        extendedExportOutput.add(envStgFile, envSubstitutions);
        extendedExportOutput.add(devStgFile, devSubstitutions);
        Result<ExtendedExportOutput> extendedExportResult = Result.success(extendedExportOutput);
        return Result.success(payload.applyExportResult(extendedExportResult));
    }

    private boolean hasSignalPhase(Stg stg, String signalRef, boolean phase) {
        Collection<SignalTransition> signalTransitions = stg.getSignalTransitions(signalRef);
        for (SignalTransition transition : signalTransitions) {
            if ((transition.getDirection() == SignalTransition.Direction.TOGGLE)
                    || (phase && (transition.getDirection() == SignalTransition.Direction.PLUS))
                    || (!phase && (transition.getDirection() == SignalTransition.Direction.MINUS))) {
                return true;
            }
        }
        return false;
    }

    private boolean missesSignalPhase(Stg stg, String signalRef, boolean phase) {
        Collection<SignalTransition> signalTransitions = stg.getSignalTransitions(signalRef);
        if (signalTransitions.isEmpty()) {
            return false;
        }
        for (SignalTransition transition : signalTransitions) {
            if ((transition.getDirection() == SignalTransition.Direction.TOGGLE)
                    || (phase && (transition.getDirection() == SignalTransition.Direction.PLUS))
                    || (!phase && (transition.getDirection() == SignalTransition.Direction.MINUS))) {
                return false;
            }
        }
        return true;
    }

    private Result<? extends VerificationChainOutput> composeInterfaces(VerificationChainOutput payload,
            ProgressMonitor<? super VerificationChainOutput> monitor, File directory) {

        File devStgFile = new File(directory, DEV_STG_FILE_NAME);
        File envStgFile = new File(directory, ENV_STG_FILE_NAME);
        PcompParameters pcompParameters = new PcompParameters(
                PcompParameters.SharedSignalMode.OUTPUT, true, false);

        // Note: device STG must go first, as this order is used in the analysis of violation traces
        PcompTask task = new PcompTask(Arrays.asList(devStgFile, envStgFile), pcompParameters, directory);

        Result<? extends PcompOutput> pcompResult = Framework.getInstance().getTaskManager().execute(
                task, "Running parallel composition [PComp]", new SubtaskMonitor<>(monitor));

        return new Result<>(pcompResult.getOutcome(), payload.applyPcompResult(pcompResult));
    }

    private Result<? extends VerificationChainOutput> exportComposition(VerificationChainOutput payload,
            ProgressMonitor<? super VerificationChainOutput> monitor, File directory) {

        PcompOutput pcompOutput = payload.getPcompResult().getPayload();
        CompositionData compositionData;
        try {
            compositionData = new CompositionData(pcompOutput.getDetailFile());
        } catch (FileNotFoundException e) {
            return Result.exception(e);
        }

        // Apply substitutions to the composition data of the STG components
        CompositionUtils.applyExportSubstitutions(compositionData, payload.getExportResult().getPayload());

        // Insert shadow transitions into the composition STG for device outputs and internal signals
        File devStgFile = new File(directory, DEV_STG_FILE_NAME);
        Stg sysStg = StgUtils.importStg(pcompOutput.getOutputFile());
        CompositionTransformer transformer = new CompositionTransformer(sysStg, compositionData);
        StgModel devStg = WorkspaceUtils.getAs(we, StgModel.class);
        Set<String> devOutputSignals = devStg.getSignalReferences(Signal.Type.OUTPUT);
        Collection<SignalTransition> shadowTransitions = transformer.insetShadowTransitions(devOutputSignals, devStgFile);
        // Insert a marked choice place shared by all shadow transitions (to prevent inconsistency)
        StgPlace shadowEnablerPlace = transformer.insertShadowEnablerPlace(shadowTransitions);
        String shadowEnablerPlaceRef = sysStg.getNodeReference(shadowEnablerPlace);
        VerificationParameters verificationParameters = ReachUtils.getConformationParameters(shadowEnablerPlaceRef);

        File modSysStgFile = new File(directory, MOD_SYS_STG_FILE_NAME);
        Result<? extends ExportOutput> exportResult = StgUtils.exportStg(sysStg, modSysStgFile, monitor);
        CompositionExportOutput sysExportOutput = new CompositionExportOutput(modSysStgFile, compositionData);

        return new Result<>(exportResult.getOutcome(), payload
                .applyExportResult(Result.success(sysExportOutput))
                .applyVerificationParameters(verificationParameters));
    }

    private Result<? extends VerificationChainOutput> verifyProperty(VerificationChainOutput payload,
            ProgressMonitor<? super VerificationChainOutput> monitor, File directory) {

        File modSysStgFile = new File(directory, MOD_SYS_STG_FILE_NAME);
        VerificationParameters verificationParameters = payload.getVerificationParameters();
        MpsatTask mpsatTask = new MpsatTask(modSysStgFile, verificationParameters, directory);
        Result<? extends MpsatOutput>  mpsatResult = Framework.getInstance().getTaskManager().execute(
                mpsatTask, "Running conformation check [MPSat]", new SubtaskMonitor<>(monitor));

        if (mpsatResult.isSuccess()) {
            payload = payload.applyMessage(mpsatResult.getPayload().hasSolutions()
                    ? "This model does not conform to the environment."
                    : "The model conforms to its environment (" + envFile.getName() + ").");
        }

        return new Result<>(mpsatResult.getOutcome(), payload.applyMpsatResult(mpsatResult));
    }

}
