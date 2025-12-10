package org.workcraft.plugins.stg.utils;

import org.workcraft.Framework;
import org.workcraft.dom.Connection;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathModel;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.visual.Positioning;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.exceptions.NoExporterException;
import org.workcraft.exceptions.OperationCancelledException;
import org.workcraft.interop.Exporter;
import org.workcraft.interop.Importer;
import org.workcraft.plugins.builtin.settings.SignalCommonSettings;
import org.workcraft.plugins.petri.PetriModel;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.petri.Transition;
import org.workcraft.plugins.petri.VisualReadArc;
import org.workcraft.plugins.petri.utils.PetriUtils;
import org.workcraft.plugins.stg.*;
import org.workcraft.plugins.stg.converters.SignalStg;
import org.workcraft.plugins.stg.interop.StgFormat;
import org.workcraft.plugins.stg.interop.StgImporter;
import org.workcraft.tasks.*;
import org.workcraft.types.Triple;
import org.workcraft.utils.*;
import org.workcraft.workspace.FileFilters;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

import java.awt.*;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.Queue;
import java.util.*;

public class StgUtils {

    public static final String SPEC_FILE_PREFIX = "net";
    public static final String DEVICE_FILE_PREFIX = "dev";
    public static final String ENVIRONMENT_FILE_PREFIX = "env";
    public static final String SYSTEM_FILE_PREFIX = "sys";

    public static final String MUTEX_FILE_SUFFIX = "-mutex";
    public static final String MODIFIED_FILE_SUFFIX = "-mod";

    private static void replaceNamedTransition(Stg stg, NamedTransition oldTransition, NamedTransition newTransition) {
        for (MathNode pred : stg.getPreset(oldTransition)) {
            connectIfPossible(stg, pred, newTransition);
        }
        for (MathNode succ : stg.getPostset(oldTransition)) {
            connectIfPossible(stg, newTransition, succ);
        }
        stg.remove(oldTransition);
    }

    private static DummyTransition convertSignalToDummyTransition(Stg stg, SignalTransition signalTransition) {
        Container container = (Container) signalTransition.getParent();
        DummyTransition dummyTransition = stg.createDummyTransition(null, container);
        replaceNamedTransition(stg, signalTransition, dummyTransition);
        return dummyTransition;
    }

    private static void replaceNamedTransition(VisualStg stg,
            VisualNamedTransition oldTransition, VisualNamedTransition newTransition) {

        newTransition.copyPosition(oldTransition);
        newTransition.copyStyle(oldTransition);

        for (VisualNode pred : stg.getPreset(oldTransition)) {
            try {
                VisualConnection oldPredConnection = stg.getConnection(pred, oldTransition);
                VisualConnection newPredConnection = null;
                if (oldPredConnection instanceof VisualReadArc) {
                    newPredConnection = stg.connectUndirected(pred, newTransition);
                } else {
                    newPredConnection = stg.connect(pred, newTransition);
                }
                if (newPredConnection != null) {
                    newPredConnection.copyStyle(oldPredConnection);
                    newPredConnection.copyShape(oldPredConnection);
                }
            } catch (InvalidConnectionException e) {
                e.printStackTrace();
            }
        }

        for (VisualNode succ : stg.getPostset(oldTransition)) {
            try {
                VisualConnection oldSuccConnection = stg.getConnection(oldTransition, succ);
                VisualConnection newSuccConnection = null;
                if (oldSuccConnection instanceof VisualReadArc) {
                    newSuccConnection = stg.connectUndirected(newTransition, succ);
                } else {
                    newSuccConnection = stg.connect(newTransition, succ);
                }
                if (newSuccConnection != null) {
                    newSuccConnection.copyStyle(oldSuccConnection);
                    newSuccConnection.copyShape(oldSuccConnection);
                }
            } catch (InvalidConnectionException e) {
                e.printStackTrace();
            }
        }
        stg.remove(oldTransition);
    }

    public static VisualDummyTransition convertSignalToDummyTransition(VisualStg stg,
            VisualSignalTransition signalTransition) {

        Container container = (Container) signalTransition.getParent();
        VisualDummyTransition dummyTransition = stg.createVisualDummyTransition(null, container);
        replaceNamedTransition(stg, signalTransition, dummyTransition);
        return dummyTransition;
    }

    public static VisualSignalTransition convertDummyToSignalTransition(VisualStg stg,
            VisualNamedTransition dummyTransition) {

        Container container = (Container) dummyTransition.getParent();
        VisualSignalTransition signalTransition = stg.createVisualSignalTransition(
                null, Signal.Type.INTERNAL, SignalTransition.Direction.TOGGLE, container);

        replaceNamedTransition(stg, dummyTransition, signalTransition);
        return signalTransition;
    }

    // Load STG model from .work or .g file
    public static Stg loadOrImportStg(File file) {
        Stg result = null;
        if (file != null) {
            String filePath = FileUtils.getFullPath(file);
            ModelEntry me = null;
            try {
                if (FileFilters.isWorkFile(file)) {
                    me = WorkUtils.loadModel(file);
                } else {
                    Importer importer = ExportUtils.chooseBestImporter(file);
                    if (importer == null) {
                        LogUtils.logError("Cannot identify appropriate importer for file '" + filePath + "'");
                    } else {
                        me = importer.importFromFile(file, null);
                    }
                }
            } catch (DeserialisationException e) {
                LogUtils.logError("Cannot read STG model from file '" + filePath + "':\n" + e.getMessage());
            } catch (OperationCancelledException ignored) {
                // Operation cancelled by the user
            }
            if (me != null) {
                MathModel model = me.getMathModel();
                if (model instanceof Stg) {
                    result = (Stg) model;
                } else {
                    LogUtils.logError("Model in file '" + filePath + "' is not an STG.");
                }
            } else {
                LogUtils.logError("Cannot read file '" + filePath + "'.");
            }
        }
        return result;
    }

    public static void restoreInterfaceSignals(Stg stg,
            Collection<String> inputSignals, Collection<String> outputSignals) {

        for (String signal : stg.getSignalReferences()) {
            stg.setSignalType(signal, Signal.Type.INTERNAL);
        }
        for (String inputSignal : inputSignals) {
            stg.setSignalType(inputSignal, Signal.Type.INPUT);
        }
        for (String outputSignal : outputSignals) {
            stg.setSignalType(outputSignal, Signal.Type.OUTPUT);
        }
    }

    public static Map<String, String> convertInternalSignalsToDummies(Stg stg) {
        Map<String, String> result = new HashMap<>();
        for (SignalTransition signalTransition : stg.getSignalTransitions(Signal.Type.INTERNAL)) {
            String signalTransitionRef = stg.getNodeReference(signalTransition);
            DummyTransition dummyTransition = StgUtils.convertSignalToDummyTransition(stg, signalTransition);
            String dummyTransitionRef = stg.getNodeReference(dummyTransition);
            result.put(dummyTransitionRef, signalTransitionRef);
        }
        return result;
    }

    public static void convertInternalSignalsToOutputs(Stg stg) {
        for (String signal : stg.getSignalReferences(Signal.Type.INTERNAL)) {
            stg.setSignalType(signal, Signal.Type.OUTPUT);
        }
    }

    public static WorkspaceEntry createStgWorkIfNewSignals(WorkspaceEntry srcWe, Stg dstStg) {
        WorkspaceEntry dstWe = null;
        if (dstStg != null) {
            Stg srcStg = WorkspaceUtils.getAs(srcWe, Stg.class);
            Set<String> newSignals = dstStg.getSignalReferences();
            newSignals.removeAll(srcStg.getSignalReferences());

            if (newSignals.isEmpty()) {
                LogUtils.logInfo("No new signals are inserted in the STG");
            } else {
                String msg = TextUtils.wrapMessageWithItems("STG modified by inserting new signal", newSignals);
                LogUtils.logInfo(msg);
                ModelEntry dstMe = new ModelEntry(new StgDescriptor(), dstStg);
                dstWe = Framework.getInstance().createWork(dstMe, srcWe.getFileName());
            }
        }
        return dstWe;
    }

    public static Stg importStg(File file) {
        if (file == null) {
            return null;
        }
        try {
            FileInputStream is = new FileInputStream(file);
            return importStg(is);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static Stg importStg(InputStream is) {
        StgImporter importer = new StgImporter();
        try {
            return importer.deserialiseStg(is);
        } catch (DeserialisationException e) {
            throw new RuntimeException(e);
        }
    }

    public static Result<? extends ExportOutput> exportStg(PetriModel stg, File file, ProgressMonitor<?> monitor) {
        StgFormat format = StgFormat.getInstance();
        Exporter exporter = ExportUtils.chooseBestExporter(stg, format);
        if (exporter == null) {
            throw new NoExporterException(stg, format);
        }
        ExportTask exportTask = new ExportTask(exporter, stg, file);
        String description = "Exporting " + file.getAbsolutePath();
        SubtaskMonitor<Object> subtaskMonitor = monitor == null ? null : new SubtaskMonitor<>(monitor);
        TaskManager taskManager = Framework.getInstance().getTaskManager();
        return taskManager.execute(exportTask, description, subtaskMonitor);
    }

    public static HashMap<String, Boolean> guessInitialStateFromSignalPlaces(Stg stg) {
        HashMap<String, Boolean> result = new HashMap<>();
        // Try to figure out signal states from ZERO and ONE places of circuit STG.
        Set<String> signalRefs = stg.getSignalReferences();
        for (String signalRef : signalRefs) {
            Boolean value = guessInitialStateFromSignalPlaces(stg, signalRef);
            if (value != null) {
                result.put(signalRef, value);
            }
        }
        return result;
    }

    private static Boolean guessInitialStateFromSignalPlaces(Stg stg, String signalRef) {
        Node zeroNode = stg.getNodeByReference(SignalStg.appendLowSuffix(signalRef));
        Node oneNode = stg.getNodeByReference(SignalStg.appendHighSuffix(signalRef));
        if (zeroNode instanceof StgPlace zeroPlace && oneNode instanceof StgPlace onePlace) {
            if (zeroPlace.getTokens() + onePlace.getTokens() == 1) {
                Collection<SignalTransition> signalTransitions = stg.getSignalTransitions(signalRef);

                Set<MathNode> riseTransitions = new HashSet<>(signalTransitions);
                riseTransitions.retainAll(stg.getPostset(zeroPlace));
                riseTransitions.retainAll(stg.getPreset(onePlace));

                Set<MathNode> fallTransitions = new HashSet<>(signalTransitions);
                fallTransitions.retainAll(stg.getPostset(onePlace));
                fallTransitions.retainAll(stg.getPreset(zeroPlace));

                if (!riseTransitions.isEmpty() && !fallTransitions.isEmpty()) {
                    return onePlace.getTokens() > 0;
                }
            }
        }
        return null;
    }

    public static Map<String, Boolean> getInitialState(StgModel stg, int timeout) {
        Map<String, Boolean> result = new HashMap<>();
        stg = copyStgPreserveSignals(stg);
        Set<String> undefinedSignalRefs = stg.getSignalReferences();
        HashSet<HashMap<Place, Integer>> visitedMarkings = new HashSet<>();
        Queue<HashMap<Place, Integer>> markingQueue = new ArrayDeque<>();
        HashMap<Place, Integer> initialMarking = PetriUtils.getMarking(stg);
        markingQueue.add(initialMarking);
        Set<Transition> conflictTransitions = getConflictTransitions(stg);
        long curTime = System.currentTimeMillis();
        long endTime = curTime + timeout;
        int stepCount = 0;
        while (!markingQueue.isEmpty() && !undefinedSignalRefs.isEmpty() && (curTime < endTime)) {
            if (stepCount++ > 999) {
                curTime = System.currentTimeMillis();
                stepCount = 0;
            }
            HashMap<Place, Integer> curMarking = markingQueue.remove();
            visitedMarkings.add(curMarking);
            PetriUtils.setMarking(stg, curMarking);
            // Derive state of signals from enabled transitions
            Set<Transition> enabledTransitions = PetriUtils.getEnabledTransitions(stg);
            for (Transition transition : enabledTransitions) {
                if (transition instanceof SignalTransition signalTransition) {
                    String signalRef = stg.getSignalReference(signalTransition);
                    Boolean signalState = getPrecedingState(signalTransition);
                    if ((signalState != null) && undefinedSignalRefs.remove(signalRef)) {
                        result.put(signalRef, signalState);
                    }
                }
            }
            // Process concurrently enabled transitions
            List<Transition> concurrentEnabledTransitions = new ArrayList<>(enabledTransitions);
            concurrentEnabledTransitions.removeAll(conflictTransitions);
            for (Transition transition : concurrentEnabledTransitions) {
                stg.fire(transition);
            }
            if (!concurrentEnabledTransitions.isEmpty()) {
                HashMap<Place, Integer> marking = PetriUtils.getMarking(stg);
                if (!visitedMarkings.contains(marking)) {
                    markingQueue.add(marking);
                    continue;
                }
            }
            // Process enabled transitions in conflict
            List<Transition> conflictEnabledTransitions = new ArrayList<>(enabledTransitions);
            conflictEnabledTransitions.retainAll(conflictTransitions);
            for (Transition transition : conflictEnabledTransitions) {
                stg.fire(transition);
                HashMap<Place, Integer> marking = PetriUtils.getMarking(stg);
                if (!visitedMarkings.contains(marking)) {
                    markingQueue.add(marking);
                }
                stg.unFire(transition);
            }
        }
        return result;
    }

    /**
     * Copy the given STG preserving the signal hierarchy and references. Note that
     * STG places are copied without their hierarchy and their names are not preserved.
     *
     * @param stg an STG to be copied
     * @return a new STG with the same signal references
     */
    private static StgModel copyStgPreserveSignals(StgModel stg) {
        Stg result = new Stg();
        copyStgRenameSignals(stg, result, Collections.emptyMap());
        return result;
    }

    private static Set<Transition> getConflictTransitions(StgModel stg) {
        Set<Transition> result = new HashSet<>();
        for (Transition transition : stg.getTransitions()) {
            for (MathNode predNode : stg.getPreset(transition)) {
                if (stg.getPostset(predNode).size() > 1) {
                    result.add(transition);
                    break;
                }
            }
        }
        return result;
    }

    private static Boolean getPrecedingState(SignalTransition signalTransition) {
        return switch (signalTransition.getDirection()) {
            case PLUS -> false;
            case MINUS -> true;
            default -> null;
        };
    }

    /**
     * Copy the given STG renaming signals.
     * Note that STG places are copied without hierarchy and their names are not preserved.
     *
     * @param stg original STG to be copied
     * @param newStg new STG to be populated
     * @param signalRenames signal mapping from original STG to new STG
     * @return mapping of transition references from new to original STG
     */
    public static Map<String, String> copyStgRenameSignals(StgModel stg, Stg newStg,
            Map<String, String> signalRenames) {

        Map<String, String> result = new HashMap<>();
        Map<MathNode, MathNode> oldToNewNodeMap = new HashMap<>();
        // Copy signal transitions with their hierarchy, renaming their signals if necessary
        for (SignalTransition signalTransition : stg.getSignalTransitions()) {
            String ref = stg.getNodeReference(signalTransition);
            Triple<String, SignalTransition.Direction, Integer> r = LabelParser.parseSignalTransition(ref);
            if (r != null) {
                String signalRef = r.getFirst();
                String renamedSignalRef = signalRenames.getOrDefault(signalRef, signalRef);
                String renamedRef = renamedSignalRef + r.getSecond();
                SignalTransition newSignalTransition = newStg.createSignalTransition(renamedRef, null);
                newStg.setSignalType(renamedSignalRef, signalTransition.getSignalType());
                newStg.setDirection(newSignalTransition, signalTransition.getDirection());
                oldToNewNodeMap.put(signalTransition, newSignalTransition);
                result.put(newStg.getNodeReference(newSignalTransition), stg.getNodeReference(signalTransition));
            }
        }
        // Copy dummy transitions with their hierarchy
        for (DummyTransition dummyTransition : stg.getDummyTransitions()) {
            String ref = stg.getNodeReference(dummyTransition);
            DummyTransition newDummyTransition = newStg.createDummyTransition(ref, null);
            oldToNewNodeMap.put(dummyTransition, newDummyTransition);
            result.put(newStg.getNodeReference(newDummyTransition), ref);
        }
        // Copy places WITHOUT their hierarchy -- implicit places cannot be copied (NOTE that implicit place ref is NOT C-style)
        for (Place place : stg.getPlaces()) {
            StgPlace newPlace = newStg.createPlace();
            newPlace.setCapacity(place.getCapacity());
            newPlace.setTokens(place.getTokens());
            oldToNewNodeMap.put(place, newPlace);
        }
        // Connect places and transitions
        for (Connection connection : stg.getConnections()) {
            MathNode newFromNode = oldToNewNodeMap.get(connection.getFirst());
            MathNode newToNode = oldToNewNodeMap.get(connection.getSecond());
            connectIfPossible(newStg, newFromNode, newToNode);
        }
        return result;
    }

    private static void connectIfPossible(Stg stg, MathNode fromNode, MathNode toNode) {
        if ((fromNode != null) && (toNode != null)) {
            try {
                stg.connect(fromNode, toNode);
            } catch (InvalidConnectionException e) {
                e.printStackTrace();
            }
        }
    }

    public static Color getTypeColor(Signal.Type type) {
        if (type != null) {
            return switch (type) {
                case INPUT -> SignalCommonSettings.getInputColor();
                case OUTPUT -> SignalCommonSettings.getOutputColor();
                case INTERNAL -> SignalCommonSettings.getInternalColor();
            };
        }
        return SignalCommonSettings.getDummyColor();
    }

    public static Set<String> getAllEvents(Collection<String> signals) {
        Set<String> result = new HashSet<>();
        for (String signal : signals) {
            for (SignalTransition.Direction direction : SignalTransition.Direction.values()) {
                result.add(signal + direction);
            }
        }
        return result;
    }

    public static Collection<String> getSignalsWithToggleTransitions(Stg stg) {
        return getSignalsWithToggleTransitions(stg, null);
    }

    public static Collection<String> getSignalsWithToggleTransitions(Stg stg, Signal.Type type) {
        Set<String> result = new HashSet<>();
        for (SignalTransition st : stg.getSignalTransitions(type)) {
            if (st.getDirection() == SignalTransition.Direction.TOGGLE) {
                String signalRef = stg.getSignalReference(st);
                result.add(signalRef);
            }
        }
        return result;
    }

    public static VisualStgPlace createPlace(VisualStg stg, String name, int tokens, double x, double y) {
        VisualStgPlace result = stg.createVisualPlace(name);
        result.setRootSpacePosition(new Point2D.Double(x, y));
        result.getReferencedComponent().setTokens(tokens);
        result.setNamePositioning(Positioning.BOTTOM);
        return result;
    }

    public static VisualSignalTransition createTransition(VisualStg stg, String name, Signal.Type type,
            SignalTransition.Direction direction, double x, double y) {

        VisualSignalTransition result = stg.createVisualSignalTransition(name, type, direction);
        result.setRootSpacePosition(new Point2D.Double(x, y));
        return result;
    }

}
