package org.workcraft.plugins.mpsat_verification.tasks;

import org.workcraft.Framework;
import org.workcraft.plugins.mpsat_verification.presets.VerificationParameters;
import org.workcraft.plugins.mpsat_verification.utils.CompositionUtils;
import org.workcraft.plugins.mpsat_verification.utils.ReachUtils;
import org.workcraft.plugins.pcomp.ComponentData;
import org.workcraft.plugins.pcomp.CompositionData;
import org.workcraft.plugins.pcomp.tasks.PcompOutput;
import org.workcraft.plugins.pcomp.tasks.PcompParameters;
import org.workcraft.plugins.pcomp.tasks.PcompTask;
import org.workcraft.plugins.stg.*;
import org.workcraft.plugins.stg.interop.StgFormat;
import org.workcraft.plugins.stg.utils.StgUtils;
import org.workcraft.tasks.*;
import org.workcraft.types.Pair;
import org.workcraft.utils.FileUtils;
import org.workcraft.utils.SortUtils;
import org.workcraft.utils.TextUtils;
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

    private static final class SignalPhaseData {
        private final Map<String, WorkspaceEntry> risingSignalToWorkMap = new HashMap<>();
        private final Map<String, WorkspaceEntry> fallingSignalToWorkMap = new HashMap<>();

        public void addRisingSignalIfAbsent(String signalRef, WorkspaceEntry we) {
            risingSignalToWorkMap.computeIfAbsent(signalRef, ref -> we);
        }

        public void addFallingSignalIfAbsent(String signalRef, WorkspaceEntry we) {
            fallingSignalToWorkMap.computeIfAbsent(signalRef, ref -> we);
        }

        public WorkspaceEntry getRisingSignalWork(String signalRef) {
            return risingSignalToWorkMap.get(signalRef);
        }

        public WorkspaceEntry getFallingSignalWork(String signalRef) {
            return fallingSignalToWorkMap.get(signalRef);
        }
    }

    private static final class SignalStateData {
        private final Map<String, WorkspaceEntry> lowSignalToWorkMap = new HashMap<>();
        private final Map<String, WorkspaceEntry> highSignalToWorkMap = new HashMap<>();

        public void addLowSignalIfAbsent(String signalRef, WorkspaceEntry we) {
            highSignalToWorkMap.remove(signalRef);
            lowSignalToWorkMap.computeIfAbsent(signalRef, ref -> we);
        }

        public void addHighSignalIfAbsent(String signalRef, WorkspaceEntry we) {
            lowSignalToWorkMap.remove(signalRef);
            highSignalToWorkMap.computeIfAbsent(signalRef, ref -> we);
        }

        public WorkspaceEntry getLowSignalWork(String signalRef) {
            return lowSignalToWorkMap.get(signalRef);
        }

        public WorkspaceEntry getHighSignalWork(String signalRef) {
            return highSignalToWorkMap.get(signalRef);
        }
    }


    public NwayConformationTask(List<WorkspaceEntry> wes, Map<WorkspaceEntry, Map<String, String>> renames) {
        this.wes = wes;
        this.renames = renames;
    }

    @Override
    public Result<? extends VerificationChainOutput> run(ProgressMonitor<? super VerificationChainOutput> monitor) {
        Result<? extends VerificationChainOutput> result = checkTrivialCases();
        if (result == null) {
            File directory = FileUtils.createTempDirectory(FileUtils.getTempPrefix("nway_conformation"));
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
        // Check that all component models are STGs
        for (WorkspaceEntry we : wes) {
            if (!WorkspaceUtils.isApplicable(we, Stg.class)) {
                return Result.exception("Incorrect model type for '" + we.getTitle() + "'");
            }
        }
        // Check for input signals with missing phases
        SignalPhaseData signalPhaseData = getPhasePresenceForInterfaceSignals();
        for (WorkspaceEntry we : wes) {
            Collection<String> problems = getMissingPhaseProblems(we, signalPhaseData);
            if (!problems.isEmpty()) {
                String msg = "Model '" + we.getTitle() + "' misses phases of interface signals that are present in other models";
                return Result.exception(TextUtils.getTextWithBulletpoints(msg, SortUtils.getSortedNatural(problems)));
            }
        }
        // Check initial state consistency for the driver and its driven signals
        Map<WorkspaceEntry, Map<String, Boolean>> workToInitialStateMap = calcInitialStates();
        SignalStateData signalStateData = getDriverInitialState(workToInitialStateMap);
        for (WorkspaceEntry we : wes) {
            Map<String, Boolean> initialState = workToInitialStateMap.getOrDefault(we, Collections.emptyMap());
            Collection<String> problems = getInitialStateMismatchProblems(we, initialState, signalStateData);
            if (!problems.isEmpty()) {
                String msg = "Model '" + we.getTitle() + "' has interface signals whose initial state is different in other models";
                return Result.exception(TextUtils.getTextWithBulletpoints(msg, SortUtils.getSortedNatural(problems)));
            }
        }
        return null;
    }

    private SignalPhaseData getPhasePresenceForInterfaceSignals() {
        SignalPhaseData result = new SignalPhaseData();
        for (WorkspaceEntry we : wes) {
            Stg stg = WorkspaceUtils.getAs(we, Stg.class);
            Map<String, String> signalRenames = renames.getOrDefault(we, Collections.emptyMap());
            Set<String> interfaceSignalRefs = new HashSet<>();
            interfaceSignalRefs.addAll(stg.getSignalReferences(Signal.Type.INPUT));
            interfaceSignalRefs.addAll(stg.getSignalReferences(Signal.Type.OUTPUT));
            for (String signalRef : interfaceSignalRefs) {
                Pair<Boolean, Boolean> phasePresence = getPhasePresence(stg, signalRef);
                String driverRef = signalRenames.getOrDefault(signalRef, signalRef);
                if (phasePresence.getFirst()) {
                    result.addRisingSignalIfAbsent(driverRef, we);
                }
                if (phasePresence.getSecond()) {
                    result.addFallingSignalIfAbsent(driverRef, we);
                }
            }
        }
        return result;
    }

    private Collection<String> getMissingPhaseProblems(WorkspaceEntry we, SignalPhaseData signalPhaseData) {
        Collection<String> result = new ArrayList<>();
        Stg stg = WorkspaceUtils.getAs(we, Stg.class);
        Map<String, String> signalRenames = renames.getOrDefault(we, Collections.emptyMap());
        Set<String> interfaceSignalRefs = new HashSet<>();
        interfaceSignalRefs.addAll(stg.getSignalReferences(Signal.Type.INPUT));
        interfaceSignalRefs.addAll(stg.getSignalReferences(Signal.Type.OUTPUT));
        for (String signalRef : interfaceSignalRefs) {
            String driverRef = signalRenames.getOrDefault(signalRef, signalRef);
            Pair<Boolean, Boolean> phasePresence = getPhasePresence(stg, signalRef);
            boolean hasPlus = phasePresence.getFirst();
            boolean hasMinus = phasePresence.getSecond();
            if (!hasPlus && hasMinus) {
                WorkspaceEntry risingSignalWork = signalPhaseData.getRisingSignalWork(driverRef);
                if (risingSignalWork != null) {
                    result.add("there is " + signalRef + SignalTransition.Direction.MINUS
                            + " but no " + signalRef + SignalTransition.Direction.PLUS
                            + ", which is present in model '" + risingSignalWork.getTitle() + "'");
                }
            }
            if (hasPlus && !hasMinus) {
                WorkspaceEntry fallingSignalWork = signalPhaseData.getFallingSignalWork(driverRef);
                if (fallingSignalWork != null) {
                    result.add("there is " + signalRef + SignalTransition.Direction.PLUS
                            + " but no " + signalRef + SignalTransition.Direction.MINUS
                            + ", which is present in model '" + fallingSignalWork.getTitle() + "'");
                }
            }
        }
        return result;
    }

    private Pair<Boolean, Boolean> getPhasePresence(Stg stg, String signalRef) {
        boolean hasPlus = false;
        boolean hasMinus = false;
        for (SignalTransition transition : stg.getSignalTransitions(signalRef)) {
            if (transition.getDirection() == SignalTransition.Direction.PLUS) {
                hasPlus = true;
            }
            if (transition.getDirection() == SignalTransition.Direction.MINUS) {
                hasMinus = true;
            }
            if (transition.getDirection() == SignalTransition.Direction.TOGGLE) {
                hasPlus = true;
                hasMinus = true;
            }
            if (hasPlus && hasMinus) {
                break;
            }
        }
        return Pair.of(hasPlus, hasMinus);
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

    private SignalStateData getDriverInitialState(Map<WorkspaceEntry, Map<String, Boolean>> initialStateMap) {
        SignalStateData result = new SignalStateData();
        for (WorkspaceEntry we : wes) {
            Stg stg = WorkspaceUtils.getAs(we, Stg.class);
            Map<String, Boolean> initialState = initialStateMap.getOrDefault(we, Collections.emptyMap());
            Map<String, String> signalRenames = renames.getOrDefault(we, Collections.emptyMap());
            for (String signalRef : stg.getSignalReferences(Signal.Type.OUTPUT)) {
                String driverRef = signalRenames.getOrDefault(signalRef, signalRef);
                Boolean signalState = initialState.get(signalRef);
                if (signalState == Boolean.FALSE) {
                    result.addLowSignalIfAbsent(driverRef, we);
                }
                if (signalState == Boolean.TRUE) {
                    result.addHighSignalIfAbsent(driverRef, we);
                }
            }
        }
        return result;
    }

    private Collection<String> getInitialStateMismatchProblems(WorkspaceEntry we,
            Map<String, Boolean> initialState, SignalStateData signalStateData) {

        Collection<String> result = new ArrayList<>();
        Stg stg = WorkspaceUtils.getAs(we, Stg.class);
        Map<String, String> signalRenames = renames.getOrDefault(we, Collections.emptyMap());
        for (String signalRef : stg.getSignalReferences(Signal.Type.INPUT)) {
            Boolean signalState = initialState.get(signalRef);
            String driverRef = signalRenames.getOrDefault(signalRef, signalRef);
            if (signalState == Boolean.FALSE) {
                WorkspaceEntry highDriverWork = signalStateData.getHighSignalWork(driverRef);
                if (highDriverWork == null) {
                    signalStateData.addLowSignalIfAbsent(driverRef, we);
                } else {
                    String driverWorkTitle = highDriverWork.getTitle();
                    result.add("'" + signalRef + "' is low, but in model '" + driverWorkTitle + "' it is high");
                }
            }
            if (signalState == Boolean.TRUE) {
                WorkspaceEntry lowDriverWork = signalStateData.getLowSignalWork(driverRef);
                if (lowDriverWork == null) {
                    signalStateData.addHighSignalIfAbsent(driverRef, we);
                } else {
                    String driverWorkTitle = lowDriverWork.getTitle();
                    result.add("'" + signalRef + "' is high, but in model '" + driverWorkTitle + "' it is low");
                }
            }
        }
        return result;
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
            Map<String, String> substitutions = StgUtils.copyStgRenameSignals(componentStg, processedStg,
                    renames.getOrDefault(we, Collections.emptyMap()));

            // Convert internal signals to dummies and keep track of renaming
            Map<String, String> dummySubstitutions = StgUtils.convertInternalSignalsToDummies(processedStg);
            updateSubstitutions(substitutions, dummySubstitutions);

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

    private void updateSubstitutions(Map<String, String> substitutions, Map<String, String> update) {
        for (Map.Entry<String, String> updateEntry : update.entrySet()) {
            String currentRef = updateEntry.getKey();
            String previousRef = updateEntry.getValue();
            String originalRef = substitutions.get(previousRef);
            if (originalRef != null) {
                substitutions.remove(previousRef);
                substitutions.put(currentRef, originalRef);
            }
        }
    }

    private Result<? extends VerificationChainOutput> composeInterfaces(VerificationChainOutput payload,
            ProgressMonitor<? super VerificationChainOutput> monitor, File directory) {

        List<File> componentStgFiles = wes.stream()
                .map(we -> getComponentStgFile(directory, we))
                .collect(Collectors.toList());

        PcompParameters pcompParameters = new PcompParameters(
                PcompParameters.SharedSignalMode.OUTPUT, false, false);

        // Note: the order of STG files is important, as it is used in the analysis of violation traces
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
        StgPlace shadowEnablerPlace = transformer.insertShadowEnablerPlace(shadowTransitions);
        String shadowEnablerPlaceRef = compositionStg.getNodeReference(shadowEnablerPlace);
        VerificationParameters verificationParameters = ReachUtils.getNwayConformationParameters(shadowEnablerPlaceRef);

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
                mpsatTask, "Running N-way conformation check [MPSat]", new SubtaskMonitor<>(monitor));

        if (mpsatResult.isSuccess()) {
            String message = mpsatResult.getPayload().hasSolutions()
                    ? "This model does not conform to the environment."
                    : "N-way conformation holds.";

            payload = payload.applyMessage(message);
        }

        return new Result<>(mpsatResult.getOutcome(), payload.applyMpsatResult(mpsatResult));
    }

}
