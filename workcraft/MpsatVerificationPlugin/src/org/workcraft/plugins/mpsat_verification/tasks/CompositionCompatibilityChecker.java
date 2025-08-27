package org.workcraft.plugins.mpsat_verification.tasks;

import org.workcraft.plugins.stg.Signal;
import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.utils.StgUtils;
import org.workcraft.types.Pair;
import org.workcraft.utils.SortUtils;
import org.workcraft.utils.TextUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.util.*;

public class CompositionCompatibilityChecker {

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


    public CompositionCompatibilityChecker(List<WorkspaceEntry> wes, Map<WorkspaceEntry, Map<String, String>> renames) {
        this.wes = wes;
        this.renames = renames;
    }

    public String getIncorrectModelTypeMessageOrNull() {
        // Check that all component models are STGs
        for (WorkspaceEntry we : wes) {
            if (!WorkspaceUtils.isApplicable(we, Stg.class)) {
                return "Incorrect model type for '" + we.getTitle() + "'";
            }
        }
        return null;
    }

    public String getMissingPhaseSignalMessageOrNull() {
        SignalPhaseData signalPhaseData = getPhasePresenceForInterfaceSignals();
        // Check for interface signals with missing phases
        for (WorkspaceEntry we : wes) {
            Collection<String> problems = getMissingPhaseProblems(we, signalPhaseData);
            if (!problems.isEmpty()) {
                return TextUtils.getTextWithBulletpoints(
                        "Model '" + we.getTitle() + "' misses phases of interface signals that are present in other models",
                        SortUtils.getSortedNatural(problems));
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

    public String getInconsistentInitialStateMessageOrNull() {
        Map<WorkspaceEntry, Map<String, Boolean>> workToInitialStateMap = calcInitialStates();
        SignalStateData signalStateData = getDriverInitialState(workToInitialStateMap);
        // Check initial state consistency for the driver and its driven signals
        for (WorkspaceEntry we : wes) {
            Map<String, Boolean> initialState = workToInitialStateMap.getOrDefault(we, Collections.emptyMap());
            Collection<String> problems = getInitialStateMismatchProblems(we, initialState, signalStateData);
            if (!problems.isEmpty()) {
                return TextUtils.getTextWithBulletpoints(
                        "Model '" + we.getTitle() + "' has interface signals whose initial state is different in other models",
                        SortUtils.getSortedNatural(problems));
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

}
