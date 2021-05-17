package org.workcraft.plugins.mpsat_verification.tasks;

import org.workcraft.Framework;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.references.ReferenceHelper;
import org.workcraft.plugins.mpsat_verification.MpsatVerificationSettings;
import org.workcraft.plugins.mpsat_verification.presets.VerificationMode;
import org.workcraft.plugins.mpsat_verification.presets.VerificationParameters;
import org.workcraft.plugins.mpsat_verification.utils.ReachUtils;
import org.workcraft.plugins.pcomp.CompositionData;
import org.workcraft.plugins.pcomp.tasks.PcompOutput;
import org.workcraft.plugins.pcomp.tasks.PcompParameters;
import org.workcraft.plugins.pcomp.tasks.PcompTask;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.stg.Signal;
import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.plugins.stg.interop.StgFormat;
import org.workcraft.plugins.stg.utils.LabelParser;
import org.workcraft.plugins.stg.utils.StgUtils;
import org.workcraft.tasks.*;
import org.workcraft.types.Pair;
import org.workcraft.utils.FileUtils;
import org.workcraft.utils.WorkUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class OutputDeterminacyTask implements Task<VerificationChainOutput> {

    private static final VerificationParameters TRIVIAL_HOLD_PARAMETERS = new VerificationParameters(
            "Output determinacy (vacuously)", VerificationMode.UNDEFINED, 0,
            null, 0, null, true);

    private static final String STG_FILE_EXTENSION = StgFormat.getInstance().getExtension();
    private static final String DEV_STG_FILE_NAME = StgUtils.DEVICE_FILE_PREFIX + STG_FILE_EXTENSION;
    private static final String ENV_STG_FILE_NAME = StgUtils.ENVIRONMENT_FILE_PREFIX + STG_FILE_EXTENSION;
    private static final String MOD_SYS_STG_FILE_NAME = StgUtils.SYSTEM_FILE_PREFIX + StgUtils.MODIFIED_FILE_SUFFIX + STG_FILE_EXTENSION;

    private static final String SHADOW_TRANSITIONS_REPLACEMENT =
            "/* insert set of names of shadow transitions here */"; // For example: "x+/1", "x-", "y+", "y-/1"

    private static final String OUTPUT_DETERMINACY_REACH =
            "// Check whether an STG is output-determinate.\n" +
            "// The enabled-via-dummies semantics is assumed for @.\n" +
            "// Configurations with maximal dummies are assumed to be allowed.\n" +
            "let\n" +
            "    // Set of phantom output transition names in the whole composed STG.\n" +
            "    SHADOW_OUTPUT_TRANSITIONS_NAMES = {" + SHADOW_TRANSITIONS_REPLACEMENT + "\"\"} \\ {\"\"},\n" +
            "    SHADOW_OUTPUT_TRANSITIONS = gather n in SHADOW_OUTPUT_TRANSITIONS_NAMES { T n }\n" +
            "{\n" +
            "    // Optimisation: make sure phantom events are not in the configuration.\n" +
            "    forall e in ev SHADOW_OUTPUT_TRANSITIONS \\ CUTOFFS { ~$e }\n" +
            "    &\n" +
            "    // Check if some output signal in the composition is enabled due to phantom transitions only;\n" +
            "    // this would mean that in the original STG there are two executions with the same visible\n" +
            "    // traces, leading to two markings M1 and M2 such that M1 enables some output signal o\n" +
            "    // but M2 does not enable o, so the output-determinacy is violated.\n" +
            "    exists o in OUTPUTS {\n" +
            "        let tran_o = tran o {\n" +
            "            exists t in tran_o * SHADOW_OUTPUT_TRANSITIONS {\n" +
            "                forall p in pre t { $p }\n" +
            "            }\n" +
            "            &\n" +
            "            forall tt in tran_o \\ SHADOW_OUTPUT_TRANSITIONS { ~@tt }\n" +
            "        }\n" +
            "    }\n" +
            "}\n";

    private final WorkspaceEntry we;

    public OutputDeterminacyTask(WorkspaceEntry we) {
        this.we = we;
    }

    @Override
    public Result<? extends VerificationChainOutput> run(ProgressMonitor<? super VerificationChainOutput> monitor) {
        Result<? extends VerificationChainOutput> result = checkTrivialCases();
        if (result == null) {
            File directory = FileUtils.createTempDirectory(FileUtils.getTempPrefix(we.getTitle()));
            Chain<VerificationChainOutput> chain = new Chain<>(this::init, monitor);
            chain.andOnSuccess(payload -> exportComponents(payload, monitor, directory), 0.1);
            chain.andOnSuccess(payload -> composeComponents(payload, monitor, directory), 0.2);
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

        Stg stg = WorkspaceUtils.getAs(we, Stg.class);
        if (isVacuouslyOutputDeterminate(stg)) {
            return Result.success(new VerificationChainOutput()
                    .applyVerificationParameters(TRIVIAL_HOLD_PARAMETERS)
                    .applyMessage("Output determinacy vacuously holds."));
        }

        return null;
    }

    private boolean isVacuouslyOutputDeterminate(Stg stg) {
        if (!stg.getDummyTransitions().isEmpty()) {
            return false;
        }
        for (Place place : stg.getPlaces()) {
            Set<String> postsetEvents = new HashSet<>();
            for (MathNode node : stg.getPostset(place)) {
                String ref = stg.getNodeReference(node);
                Pair<String, Integer> pair = LabelParser.parseInstancedTransition(ref);
                if ((pair != null) && !postsetEvents.add(pair.getFirst())) {
                    return false;
                }
            }
        }
        return true;
    }

    private Result<? extends VerificationChainOutput> init() {
        VerificationParameters verificationParameters = ReachUtils.getToolchainPreparationParameters();
        return Result.success(new VerificationChainOutput().applyVerificationParameters(verificationParameters));
    }

    private Result<? extends VerificationChainOutput> exportComponents(VerificationChainOutput payload,
            ProgressMonitor<? super VerificationChainOutput> monitor, File directory) {

        // Clone STG as its internal signals will be converted to outputs
        ModelEntry me = WorkUtils.cloneModel(we.getModelEntry());
        Stg stg = WorkspaceUtils.getAs(me, Stg.class);
        StgUtils.convertInternalSignalsToOutputs(stg);

        // Generating two copies of .g file for the model (dev and env)
        File devStgFile = new File(directory, DEV_STG_FILE_NAME);
        Result<? extends ExportOutput> devExportResult = StgUtils.exportStg(stg, devStgFile, monitor);
        if (!devExportResult.isSuccess()) {
            return new Result<>(devExportResult.getOutcome(), payload);
        }

        File envStgFile = new File(directory, ENV_STG_FILE_NAME);
        Result<? extends ExportOutput> envExportResult = StgUtils.exportStg(stg, envStgFile, monitor);
        if (!envExportResult.isSuccess()) {
            return new Result<>(envExportResult.getOutcome(), payload);
        }

        return Result.success(payload.applyExportResult(devExportResult));
    }

    private Result<? extends VerificationChainOutput> composeComponents(VerificationChainOutput payload,
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

        // Insert shadow transitions into the composition STG for the device local signals
        Stg sysStg = StgUtils.importStg(pcompOutput.getOutputFile());
        CompositionTransformer transformer = new CompositionTransformer(sysStg, compositionData);
        Set<String> localSignals = new HashSet<>();
        localSignals.addAll(sysStg.getSignalReferences(Signal.Type.OUTPUT));
        localSignals.addAll(sysStg.getSignalReferences(Signal.Type.INTERNAL));
        File devStgFile = new File(directory, DEV_STG_FILE_NAME);
        Collection<SignalTransition> shadowTransitions = transformer.insetShadowTransitions(localSignals, devStgFile);
        // Insert a marked choice place shared by all shadow transitions (to prevent inconsistency)
        transformer.insertShadowEnablerPlace(shadowTransitions);

        // Fill verification parameters with the inserted shadow transitions
        Collection<String> shadowTransitionRefs = ReferenceHelper.getReferenceList(sysStg, shadowTransitions);
        VerificationParameters verificationParameters = getVerificationParameters(shadowTransitionRefs);

        File modSysStgFile = new File(directory, MOD_SYS_STG_FILE_NAME);
        Result<? extends ExportOutput> exportResult = StgUtils.exportStg(sysStg, modSysStgFile, monitor);
        CompositionExportOutput sysExportOutput = new CompositionExportOutput(modSysStgFile, compositionData);

        return new Result<>(exportResult.getOutcome(), payload
                .applyExportResult(Result.success(sysExportOutput))
                .applyVerificationParameters(verificationParameters));
    }

    private VerificationParameters getVerificationParameters(Collection<String> shadowTransitionRefs) {
        String str = shadowTransitionRefs.stream()
                .map(ref -> "\"" + ref + "\", ")
                .collect(Collectors.joining());

        String reach = OUTPUT_DETERMINACY_REACH.replace(SHADOW_TRANSITIONS_REPLACEMENT, str);
        return new VerificationParameters("Output determinacy",
                VerificationMode.STG_REACHABILITY_OUTPUT_DETERMINACY, 0,
                MpsatVerificationSettings.getSolutionMode(),
                MpsatVerificationSettings.getSolutionCount(),
                reach, true);
    }

    private Result<? extends VerificationChainOutput> verifyProperty(VerificationChainOutput payload,
            ProgressMonitor<? super VerificationChainOutput> monitor, File directory) {

        File modSysStgFile = new File(directory, MOD_SYS_STG_FILE_NAME);
        VerificationParameters verificationParameters = payload.getVerificationParameters();
        MpsatTask mpsatTask = new MpsatTask(modSysStgFile, verificationParameters, directory);
        Result<? extends MpsatOutput>  mpsatResult = Framework.getInstance().getTaskManager().execute(
                mpsatTask, "Running output determinacy check [MPSat]", new SubtaskMonitor<>(monitor));

        return new Result<>(mpsatResult.getOutcome(), payload.applyMpsatResult(mpsatResult));
    }

}
