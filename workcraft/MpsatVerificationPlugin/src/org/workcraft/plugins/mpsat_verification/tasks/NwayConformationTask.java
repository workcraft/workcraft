package org.workcraft.plugins.mpsat_verification.tasks;

import org.workcraft.Framework;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.references.ReferenceHelper;
import org.workcraft.gui.properties.PropertyHelper;
import org.workcraft.plugins.mpsat_verification.presets.VerificationParameters;
import org.workcraft.plugins.mpsat_verification.utils.CompositionUtils;
import org.workcraft.plugins.mpsat_verification.utils.ReachUtils;
import org.workcraft.plugins.pcomp.ComponentData;
import org.workcraft.plugins.pcomp.CompositionData;
import org.workcraft.plugins.pcomp.tasks.PcompOutput;
import org.workcraft.plugins.pcomp.tasks.PcompParameters;
import org.workcraft.plugins.pcomp.tasks.PcompTask;
import org.workcraft.plugins.stg.Signal;
import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.plugins.stg.interop.StgFormat;
import org.workcraft.plugins.stg.utils.StgUtils;
import org.workcraft.tasks.*;
import org.workcraft.utils.FileUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.stream.Collectors;

public class NwayConformationTask implements Task<VerificationChainOutput> {

    private static final String STG_FILE_EXTENSION = StgFormat.getInstance().getExtension();
    private static final String COMPOSITION_SHADOW_STG_FILE_NAME = "composition-shadow" + STG_FILE_EXTENSION;

    private final List<WorkspaceEntry> wes;
    private final Map<WorkspaceEntry, Map<String, String>> renames;

    public NwayConformationTask(List<WorkspaceEntry> wes, Map<WorkspaceEntry, Map<String, String>> renames) {
        this.wes = wes;
        this.renames = renames;
    }

    @Override
    public Result<? extends VerificationChainOutput> run(ProgressMonitor<? super VerificationChainOutput> monitor) {
        Result<? extends VerificationChainOutput> result = checkTrivialCases();
        if (result == null) {
            File directory = FileUtils.createTempDirectory(FileUtils.getTempPrefix("-nway_conformation"));
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
        for (WorkspaceEntry we : wes) {
            if (!WorkspaceUtils.isApplicable(we, Stg.class)) {
                return Result.exception("Incorrect model type for '" + we.getTitle() + "'");
            }
        }
        Map<WorkspaceEntry, Map<String, Boolean>> initialStateMap = calcInitialStates();
        Map<String, Boolean> driverInitialState = getDriverInitialState(initialStateMap);
        // Check initial state consistency for driven signals
        for (WorkspaceEntry we : wes) {
            Map<String, Boolean> initialState = initialStateMap.getOrDefault(we, Collections.emptyMap());
            Collection<String> problems = getInitialStateMismatch(we, initialState, driverInitialState);
            if (!problems.isEmpty()) {
                String msg = "Unexpected initial state of input signals in '" + we.getTitle() + "':\n"
                        + problems.stream()
                        .map(problem -> PropertyHelper.BULLET_PREFIX + problem)
                        .collect(Collectors.joining("\n"));

                return Result.exception(msg);
            }
        }
        return null;
    }

    private Map<WorkspaceEntry, Map<String, Boolean>> calcInitialStates() {
        Map<WorkspaceEntry, Map<String, Boolean>> result = new HashMap<>();
        for (WorkspaceEntry we : wes) {
            Stg stg = WorkspaceUtils.getAs(we, Stg.class);
            Map<String, Boolean> initialState = StgUtils.getInitialState(stg, 1000);
            result.put(we, initialState);
        }
        return result;
    }

    private Map<String, Boolean> getDriverInitialState(Map<WorkspaceEntry, Map<String, Boolean>> initialStateMap) {
        Map<String, Boolean> result = new HashMap<>();
        for (WorkspaceEntry we : wes) {
            Stg stg = WorkspaceUtils.getAs(we, Stg.class);
            Map<String, Boolean> initialState = initialStateMap.getOrDefault(we, Collections.emptyMap());
            Map<String, String> signalRenames = renames.getOrDefault(we, Collections.emptyMap());
            for (String signalRef : stg.getSignalReferences(Signal.Type.OUTPUT)) {
                String driverRef = signalRenames.getOrDefault(signalRef, signalRef);
                Boolean signalState = initialState.get(signalRef);
                result.put(driverRef, signalState);
            }
        }
        return result;
    }

    private Collection<String> getInitialStateMismatch(WorkspaceEntry we,
            Map<String, Boolean> initialState, Map<String, Boolean> driverInitialState) {

        Stg stg = WorkspaceUtils.getAs(we, Stg.class);
        Map<String, String> signalRenames = renames.getOrDefault(we, Collections.emptyMap());
        Collection<String> result = new ArrayList<>();
        for (String signalRef : stg.getSignalReferences(Signal.Type.INPUT)) {
            Boolean signalState = initialState.get(signalRef);
            String driverRef = signalRenames.getOrDefault(signalRef, signalRef);
            Boolean driverState = driverInitialState.get(driverRef);
            if ((signalState != null) && (driverState != null) && (signalState != driverState)) {
                result.add("'" + signalRef + "'" + " is " + getLevelString(signalState)
                        + " while its driver is " + getLevelString(driverState));
            }
        }
        return result;
    }

    private String getLevelString(boolean state) {
        return state ? "high" : "low";
    }

    private Result<? extends VerificationChainOutput> init() {
        VerificationParameters verificationParameters = ReachUtils.getToolchainPreparationParameters();
        return Result.success(new VerificationChainOutput().applyVerificationParameters(verificationParameters));
    }

    private Result<? extends VerificationChainOutput> exportInterfaces(VerificationChainOutput payload,
            ProgressMonitor<? super VerificationChainOutput> monitor, File directory) {

        ExtendedExportOutput extendedExportOutput = new ExtendedExportOutput();
        for (WorkspaceEntry we : wes) {
            // Clone component STG as its internal signals will be converted to dummies
            Stg componentStg = WorkspaceUtils.getAs(we, Stg.class);
            Stg processedStg = new Stg();
            Map<MathNode, MathNode> nodeMap = StgUtils.copyStgRenameSignals(componentStg, processedStg,
                    renames.getOrDefault(we, Collections.emptyMap()));

            // Export component STG (convert internal signals to dummies and keep track of renaming)
            @SuppressWarnings("PMD.PrematureDeclaration")
            Map<String, String> substitutions = StgUtils.convertInternalSignalsToDummies(processedStg);
            substitutions.putAll(getTransitionSubstitutions(componentStg, processedStg, nodeMap));

            File stgFile = new File(directory, we.getTitle() + STG_FILE_EXTENSION);
            Result<? extends ExportOutput> exportResult = StgUtils.exportStg(processedStg, stgFile, monitor);
            if (!exportResult.isSuccess()) {
                return new Result<>(exportResult.getOutcome(), payload);
            }
            extendedExportOutput.add(stgFile, substitutions);
        }
        Result<ExtendedExportOutput> extendedExportResult = Result.success(extendedExportOutput);
        return Result.success(payload.applyExportResult(extendedExportResult));
    }

    private Map<String, String> getTransitionSubstitutions(Stg srcStg, Stg dstStg, Map<MathNode, MathNode> nodeMap) {
        Map<String, String> result = new HashMap<>();
        for (MathNode srcNode : srcStg.getSignalTransitions()) {
            MathNode dstNode = nodeMap.get(srcNode);
            if (dstNode instanceof SignalTransition) {
                String srcRef = srcStg.getNodeReference(srcNode);
                String dstRef = dstStg.getNodeReference(dstNode);
                result.put(dstRef, srcRef);
            }
        }
        return result;
    }

    private Result<? extends VerificationChainOutput> composeInterfaces(VerificationChainOutput payload,
            ProgressMonitor<? super VerificationChainOutput> monitor, File directory) {

        List<File> componentStgFiles = wes.stream()
                .map(we -> getComponentStgFile(directory, we))
                .collect(Collectors.toList());

        PcompParameters pcompParameters = new PcompParameters(
                PcompParameters.SharedSignalMode.OUTPUT, false, false);

        // Note: the order of STG files is important, as it ts used in the analysis of violation traces
        PcompTask task = new PcompTask(componentStgFiles, pcompParameters, directory);

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
        ExportOutput exportOutput = payload.getExportResult().getPayload();
        CompositionUtils.applyExportSubstitutions(compositionData, exportOutput);

        // Insert shadow transitions into the composition STG for device outputs and internal signals
        Stg compositionStg = StgUtils.importStg(pcompOutput.getOutputFile());
        CompositionTransformer transformer = new CompositionTransformer(compositionStg, compositionData);
        Set<SignalTransition> shadowTransitions = new HashSet<>();
        int index = 0;
        for (WorkspaceEntry we : wes) {
            StgModel componentStg = WorkspaceUtils.getAs(we, StgModel.class);
            Map<String, String> renameMap = renames.getOrDefault(we, Collections.emptyMap());
            Set<String> componentOutputSignals = componentStg.getSignalReferences(Signal.Type.OUTPUT).stream()
                    .map(name -> renameMap.getOrDefault(name, name)).collect(Collectors.toSet());

            File componentStgFile = getComponentStgFile(directory, we);
            Collection<SignalTransition> componentShadowTransitions = transformer.insetShadowTransitions(componentOutputSignals, componentStgFile);
            shadowTransitions.addAll(componentShadowTransitions);

            ComponentData componentData = compositionData.getComponentData(index);
            if (componentData != null) {
                Map<String, String> substitutions = new HashMap<>();
                for (SignalTransition shadowTransition : componentShadowTransitions) {
                    String shadowTransitionRef = compositionStg.getNodeReference(shadowTransition);
                    String srcCompositionTransitionRef = componentData.getSrcTransition(shadowTransitionRef);
                    String srcComponentTransitionRef = componentData.getSrcTransition(srcCompositionTransitionRef);
                    substitutions.put(srcCompositionTransitionRef, srcComponentTransitionRef);
                }
                componentData.substituteSrcTransitions(substitutions);
            }
            index++;
        }
        // Insert a marked choice place shared by all shadow transitions (to prevent inconsistency)
        transformer.insertShadowEnablerPlace(shadowTransitions);

        // Fill verification parameters with the inserted shadow transitions
        Collection<String> shadowTransitionRefs = ReferenceHelper.getReferenceList(compositionStg, shadowTransitions);
        VerificationParameters verificationParameters = ReachUtils.getConformationParameters(shadowTransitionRefs);

        File shadowCompositionStgFile = new File(directory, COMPOSITION_SHADOW_STG_FILE_NAME);
        Result<? extends ExportOutput> exportResult = StgUtils.exportStg(compositionStg, shadowCompositionStgFile, monitor);
        CompositionExportOutput compositionExportOutput = new CompositionExportOutput(shadowCompositionStgFile, compositionData);

        return new Result<>(exportResult.getOutcome(), payload
                .applyExportResult(Result.success(compositionExportOutput))
                .applyVerificationParameters(verificationParameters));
    }

    private File getComponentStgFile(File directory, WorkspaceEntry we) {
        return new File(directory, we.getTitle() + STG_FILE_EXTENSION);
    }

    private Result<? extends VerificationChainOutput> verifyProperty(VerificationChainOutput payload,
            ProgressMonitor<? super VerificationChainOutput> monitor, File directory) {

        File compositionStgFile = new File(directory, COMPOSITION_SHADOW_STG_FILE_NAME);
        VerificationParameters verificationParameters = payload.getVerificationParameters();
        MpsatTask mpsatTask = new MpsatTask(compositionStgFile, verificationParameters, directory);
        Result<? extends MpsatOutput>  mpsatResult = Framework.getInstance().getTaskManager().execute(
                mpsatTask, "Running refinement check [MPSat]", new SubtaskMonitor<>(monitor));

        if (mpsatResult.isSuccess()) {
            payload = payload.applyMessage(mpsatResult.getPayload().hasSolutions()
                    ? "This model does not conform to the environment."
                    : "N-way conformation holds.");
        }

        return new Result<>(mpsatResult.getOutcome(), payload.applyMpsatResult(mpsatResult));
    }

}
