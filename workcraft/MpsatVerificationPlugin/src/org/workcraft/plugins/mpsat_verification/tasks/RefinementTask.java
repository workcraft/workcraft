package org.workcraft.plugins.mpsat_verification.tasks;

import org.workcraft.Framework;
import org.workcraft.dom.references.ReferenceHelper;
import org.workcraft.plugins.mpsat_verification.MpsatVerificationSettings;
import org.workcraft.plugins.mpsat_verification.presets.VerificationMode;
import org.workcraft.plugins.mpsat_verification.presets.VerificationParameters;
import org.workcraft.plugins.mpsat_verification.utils.CompositionUtils;
import org.workcraft.plugins.mpsat_verification.utils.ReachUtils;
import org.workcraft.plugins.pcomp.CompositionData;
import org.workcraft.plugins.pcomp.tasks.PcompOutput;
import org.workcraft.plugins.pcomp.tasks.PcompParameters;
import org.workcraft.plugins.pcomp.tasks.PcompTask;
import org.workcraft.plugins.stg.Signal;
import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.interop.StgFormat;
import org.workcraft.plugins.stg.utils.StgUtils;
import org.workcraft.tasks.*;
import org.workcraft.utils.FileUtils;
import org.workcraft.utils.TextUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.stream.Collectors;

public class RefinementTask implements Task<VerificationChainOutput> {

    private static final VerificationParameters TRIVIAL_VIOLATION_PARAMETERS = new VerificationParameters(
            "Refinement", VerificationMode.UNDEFINED, 0,
            null, 0, null, false);

    private static final String SHADOW_TRANSITIONS_REPLACEMENT =
            "/* insert set of names of shadow transitions here */"; // For example: "x+/1", "x-", "y+", "y-/1"

    private static final String REFINEMENT_REACH =
            "// Check whether one STG (implementation) refines another STG (specification).\n" +
            "// The enabled-via-dummies semantics is assumed for @, and configurations with maximal\n" +
            "// dummies are assumed to be allowed - this corresponds to the -Fe mode of MPSAT.\n" +
            "let\n" +
            "    // Names of all shadow transitions in the composed STG.\n" +
            "    SHADOW_TRANSITIONS_NAMES = {" + SHADOW_TRANSITIONS_REPLACEMENT + "\"\"} \\ {\"\"},\n" +
            "    SHADOW_TRANSITIONS = gather n in SHADOW_TRANSITIONS_NAMES { T n }\n" +
            "{\n" +
            "    // Optimisation: make sure shadow events are not in the configuration.\n" +
            "    forall e in ev SHADOW_TRANSITIONS \\ CUTOFFS { ~$e }\n" +
            "    &\n" +
            "    // Check if some signal is enabled due to shadow transitions only,\n" +
            "    // which would mean that some condition of violation witness holds.\n" +
            "    exists s in (INPUTS + OUTPUTS) {\n" +
            "        let tran_s = tran s {\n" +
            "            exists t in tran_s * SHADOW_TRANSITIONS {\n" +
            "                forall p in pre t { $p }\n" +
            "            }\n" +
            "            &\n" +
            "            forall tt in tran_s \\ SHADOW_TRANSITIONS { ~@tt }\n" +
            "        }\n" +
            "    }\n" +
            "}\n";

    private static final String STG_FILE_EXTENSION = StgFormat.getInstance().getExtension();
    private static final String IMPLEMENTATION_STG_FILE_NAME = "implementation" + STG_FILE_EXTENSION;
    private static final String SPECIFICATION_STG_FILE_NAME = "specification" + STG_FILE_EXTENSION;
    private static final String COMPOSITION_SHADOW_STG_FILE_NAME = "composition-shadow" + STG_FILE_EXTENSION;

    private final WorkspaceEntry we;
    private final Stg implementationStg;
    private final File specificationFile;
    private final boolean allowConcurrencyReduction;
    private final boolean assumeInputReceptiveness;

    public RefinementTask(WorkspaceEntry we, Stg implementationStg, File specificationFile,
            boolean allowConcurrencyReduction, boolean assumeInputReceptiveness) {

        this.we = we;
        this.implementationStg = implementationStg;
        this.specificationFile = specificationFile;
        this.allowConcurrencyReduction = allowConcurrencyReduction;
        this.assumeInputReceptiveness = assumeInputReceptiveness;
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
        if (specificationFile == null) {
            return Result.exception("Specification STG is undefined");
        }

        Stg specificationStg = StgUtils.loadStg(specificationFile);
        if (specificationStg == null) {
            return Result.exception("Cannot load specification STG from file '" + specificationFile.getAbsolutePath() + "'");
        }

        // Make sure that signal types of the specification STG match those of the implementation STG
        Set<String> implementationInputs = implementationStg.getSignalReferences(Signal.Type.INPUT);
        Set<String> implementationOutputs = implementationStg.getSignalReferences(Signal.Type.OUTPUT);
        StgUtils.restoreInterfaceSignals(specificationStg, implementationInputs, implementationOutputs);

        if (!specificationStg.getSignalReferences(Signal.Type.INPUT).containsAll(implementationInputs)) {
            return Result.success(new VerificationChainOutput()
                    .applyVerificationParameters(TRIVIAL_VIOLATION_PARAMETERS)
                    .applyMessage("Refinement is violated because\nimplementation has inputs that are not in specification"));
        }

        if (!specificationStg.getSignalReferences(Signal.Type.OUTPUT).equals(implementationOutputs)) {
            return Result.success(new VerificationChainOutput()
                    .applyVerificationParameters(TRIVIAL_VIOLATION_PARAMETERS)
                    .applyMessage("Refinement is violated because\nimplementation outputs differ from specification"));
        }

        Collection<String> implementationToggleOutputs = StgUtils.getSignalsWithToggleTransitions(implementationStg, Signal.Type.OUTPUT);
        if (!implementationToggleOutputs.isEmpty()) {
            return Result.exception(TextUtils.wrapMessageWithItems(
                    "Refinement cannot be checked for implementation STG with toggle outputs transitions.\n" +
                            "Problematic signal", implementationToggleOutputs));
        }

        if (!allowConcurrencyReduction) {
            Collection<String> specificationToggleOutputs = StgUtils.getSignalsWithToggleTransitions(specificationStg, Signal.Type.OUTPUT);
            if (!specificationToggleOutputs.isEmpty()) {
                return Result.exception(TextUtils.wrapMessageWithItems(
                        "Refinement cannot be checked for specification STG with toggle output transitions.\n" +
                                "Problematic signal", specificationToggleOutputs));
            }
        }

        if (!assumeInputReceptiveness) {
            Collection<String> specificationToggleInputs = StgUtils.getSignalsWithToggleTransitions(specificationStg, Signal.Type.INPUT);
            specificationToggleInputs.retainAll(implementationInputs);
            if (!specificationToggleInputs.isEmpty()) {
                return Result.exception(TextUtils.wrapMessageWithItems(
                        "Refinement cannot be checked for specification STG with toggle input transitions.\n" +
                                "Problematic signal", specificationToggleInputs));
            }
        }

        return null;
    }

    private Result<? extends VerificationChainOutput> init() {
        VerificationParameters verificationParameters = ReachUtils.getToolchainPreparationParameters();
        return Result.success(new VerificationChainOutput().applyVerificationParameters(verificationParameters));
    }

    private Result<? extends VerificationChainOutput> exportInterfaces(VerificationChainOutput payload,
            ProgressMonitor<? super VerificationChainOutput> monitor, File directory) {

        Stg specificationStg = StgUtils.loadStg(specificationFile);
        if (specificationStg == null) {
            return Result.exception("Cannot load specification STG from file '" + specificationFile.getAbsolutePath() + "'");
        }

        // Make sure that signal types of the specification STG match those of the implementation STG
        Set<String> implementationInputs = implementationStg.getSignalReferences(Signal.Type.INPUT);
        Set<String> implementationOutputs = implementationStg.getSignalReferences(Signal.Type.OUTPUT);
        StgUtils.restoreInterfaceSignals(specificationStg, implementationInputs, implementationOutputs);

        if (!specificationStg.getSignalReferences(Signal.Type.INPUT).containsAll(implementationInputs)) {
            return Result.exception("Inputs of specification STG are not subset of implementation STG");
        }

        if (!specificationStg.getSignalReferences(Signal.Type.OUTPUT).equals(implementationOutputs)) {
            return Result.exception("Outputs of specification STG differ from outputs of implementation STG");
        }

        // Export specification STG (convert internal signals to dummies and keep track of renaming)
        @SuppressWarnings("PMD.PrematureDeclaration")
        Map<String, String> specificationSubstitutions = StgUtils.convertInternalSignalsToDummies(specificationStg);
        File specificationStgFile = new File(directory, SPECIFICATION_STG_FILE_NAME);
        Result<? extends ExportOutput> specificationExportResult = StgUtils.exportStg(specificationStg, specificationStgFile, monitor);
        if (!specificationExportResult.isSuccess()) {
            return new Result<>(specificationExportResult.getOutcome(), payload);
        }

        // Export implementation STG (convert internal signals to dummies and keep track of renaming)
        @SuppressWarnings("PMD.PrematureDeclaration")
        Map<String, String> implementationSubstitutions = allowConcurrencyReduction ? Collections.emptyMap()
                : StgUtils.convertInternalSignalsToDummies(implementationStg);

        File implementationStgFile = new File(directory, IMPLEMENTATION_STG_FILE_NAME);
        Result<? extends ExportOutput> implementationExportResult = StgUtils.exportStg(implementationStg, implementationStgFile, monitor);
        if (!implementationExportResult.isSuccess()) {
            return new Result<>(implementationExportResult.getOutcome(), payload);
        }

        ExtendedExportOutput extendedExportOutput = new ExtendedExportOutput();
        extendedExportOutput.add(specificationStgFile, specificationSubstitutions);
        extendedExportOutput.add(implementationStgFile, implementationSubstitutions);
        Result<ExtendedExportOutput> extendedExportResult = Result.success(extendedExportOutput);
        return Result.success(payload.applyExportResult(extendedExportResult));
    }

    private Result<? extends VerificationChainOutput> composeInterfaces(VerificationChainOutput payload,
            ProgressMonitor<? super VerificationChainOutput> monitor, File directory) {

        File specificationStgFile = new File(directory, SPECIFICATION_STG_FILE_NAME);
        File implementationStgFile = new File(directory, IMPLEMENTATION_STG_FILE_NAME);
        PcompParameters pcompParameters = new PcompParameters(
                PcompParameters.SharedSignalMode.OUTPUT, true, false);

        // Note: implementation STG must go first, as this order is used in the analysis of violation traces
        PcompTask task = new PcompTask(Arrays.asList(implementationStgFile, specificationStgFile),
                pcompParameters, directory);

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

        File specificationStgFile = new File(directory, SPECIFICATION_STG_FILE_NAME);
        File implementationStgFile = new File(directory, IMPLEMENTATION_STG_FILE_NAME);

        Stg implementationStg = StgUtils.importStg(implementationStgFile);
        Set<String> inputSignals = implementationStg.getSignalReferences(Signal.Type.INPUT);
        Set<String> outputSignals = implementationStg.getSignalReferences(Signal.Type.OUTPUT);

        // Insert shadow transitions into the composition STG and adjust compositionData accordingly
        Stg compositionStg = StgUtils.importStg(pcompOutput.getOutputFile());
        CompositionTransformer transformer = new CompositionTransformer(compositionStg, compositionData);
        Set<SignalTransition> shadowTransitions = new HashSet<>();
        // - all outputs of implementation STG
        shadowTransitions.addAll(transformer.insetShadowTransitions(outputSignals, implementationStgFile));
        if (!allowConcurrencyReduction) {
            // - all outputs of specification (for strict refinement only, without concurrency reduction)
            shadowTransitions.addAll(transformer.insetShadowTransitions(outputSignals, specificationStgFile));
        }
        if (!assumeInputReceptiveness) {
            // - all inputs of specification STG known to implementation STG
            shadowTransitions.addAll(transformer.insetShadowTransitions(inputSignals, specificationStgFile));
        }
        // Insert a marked choice place shared by all shadow transitions (to prevent inconsistency)
        transformer.insertShadowEnablerPlace(shadowTransitions);

        // Apply substitutions to the composition data of the STG components
        CompositionUtils.applyExportSubstitutions(compositionData, payload.getExportResult().getPayload());

        // Fill verification parameters with the inserted shadow transitions
        Collection<String> shadowTransitionRefs = ReferenceHelper.getReferenceList(compositionStg, shadowTransitions);
        VerificationParameters verificationParameters = getVerificationParameters(shadowTransitionRefs);

        File shadowCompositionStgFile = new File(directory, COMPOSITION_SHADOW_STG_FILE_NAME);
        Result<? extends ExportOutput> exportResult = StgUtils.exportStg(compositionStg, shadowCompositionStgFile, monitor);
        CompositionExportOutput compositionExportOutput = new CompositionExportOutput(shadowCompositionStgFile, compositionData);

        return new Result<>(exportResult.getOutcome(), payload
                .applyExportResult(Result.success(compositionExportOutput))
                .applyVerificationParameters(verificationParameters));
    }

    private VerificationParameters getVerificationParameters(Collection<String> shadowTransitionRefs) {
        String str = shadowTransitionRefs.stream()
                .map(ref -> "\"" + ref + "\", ")
                .collect(Collectors.joining());

        String reach = REFINEMENT_REACH.replace(SHADOW_TRANSITIONS_REPLACEMENT, str);
        return new VerificationParameters("Refinement",
                VerificationMode.STG_REACHABILITY_REFINEMENT, 0,
                MpsatVerificationSettings.getSolutionMode(),
                MpsatVerificationSettings.getSolutionCount(),
                reach, true);
    }

    private Result<? extends VerificationChainOutput> verifyProperty(VerificationChainOutput payload,
            ProgressMonitor<? super VerificationChainOutput> monitor, File directory) {

        File compositionStgFile = new File(directory, COMPOSITION_SHADOW_STG_FILE_NAME);
        VerificationParameters verificationParameters = payload.getVerificationParameters();
        MpsatTask mpsatTask = new MpsatTask(compositionStgFile, verificationParameters, directory);
        Result<? extends MpsatOutput>  result = Framework.getInstance().getTaskManager().execute(
                mpsatTask, "Running refinement check [MPSat]", new SubtaskMonitor<>(monitor));

        return new Result<>(result.getOutcome(), payload.applyMpsatResult(result));
    }

}
