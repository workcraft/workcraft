package org.workcraft.plugins.stg.utils;

import org.workcraft.Framework;
import org.workcraft.dom.Connection;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathModel;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.exceptions.NoExporterException;
import org.workcraft.interop.Exporter;
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
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

import java.awt.*;
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
            try {
                stg.connect(pred, newTransition);
            } catch (InvalidConnectionException e) {
                e.printStackTrace();
            }
        }

        for (MathNode succ : stg.getPostset(oldTransition)) {
            try {
                stg.connect(newTransition, succ);
            } catch (InvalidConnectionException e) {
                e.printStackTrace();
            }
        }
        stg.remove(oldTransition);
    }

    private static DummyTransition convertSignalToDummyTransition(Stg stg, SignalTransition signalTransition) {
        Container container = (Container) signalTransition.getParent();
        DummyTransition dummyTransition = stg.createDummyTransition(null, container);
        replaceNamedTransition(stg, signalTransition, dummyTransition);
        return dummyTransition;
    }

    private static void replaceNamedTransition(VisualStg stg, VisualNamedTransition oldTransition, VisualNamedTransition newTransition) {
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

    public static VisualDummyTransition convertSignalToDummyTransition(VisualStg stg, VisualSignalTransition signalTransition) {
        Container container = (Container) signalTransition.getParent();
        VisualDummyTransition dummyTransition = stg.createVisualDummyTransition(null, container);
        replaceNamedTransition(stg, signalTransition, dummyTransition);
        return dummyTransition;
    }

    public static VisualSignalTransition convertDummyToSignalTransition(VisualStg stg, VisualNamedTransition dummyTransition) {
        Container container = (Container) dummyTransition.getParent();
        VisualSignalTransition signalTransition = stg.createVisualSignalTransition(null, Signal.Type.INTERNAL, SignalTransition.Direction.TOGGLE, container);
        replaceNamedTransition(stg, dummyTransition, signalTransition);
        return signalTransition;
    }

    // Load STG model from .work or .g file
    public static Stg loadStg(File file) {
        Stg result = null;
        if (file != null) {
            String filePath = FileUtils.getFullPath(file);
            try {
                ModelEntry me = WorkUtils.loadModel(file);
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
            } catch (DeserialisationException e) {
                LogUtils.logError("Cannot read STG model from file '" + filePath + "':\n" + e.getMessage());
            }
        }
        return result;
    }

    public static void restoreInterfaceSignals(Stg stg, Collection<String> inputSignals, Collection<String> outputSignals) {
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
            return importer.importStg(is);
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
            Node zeroNode = stg.getNodeByReference(SignalStg.appendLowSuffix(signalRef));
            Node oneNode = stg.getNodeByReference(SignalStg.appendHighSuffix(signalRef));
            if (!(zeroNode instanceof StgPlace) || !(oneNode instanceof StgPlace)) {
                continue;
            }

            StgPlace zeroPlace = (StgPlace) zeroNode;
            StgPlace onePlace = (StgPlace) oneNode;
            if (zeroPlace.getTokens() + onePlace.getTokens() != 1) {
                continue;
            }

            Collection<SignalTransition> signalTransitions = stg.getSignalTransitions(signalRef);

            Set<MathNode> riseTransitions = new HashSet<>(signalTransitions);
            riseTransitions.retainAll(stg.getPostset(zeroPlace));
            riseTransitions.retainAll(stg.getPreset(onePlace));

            Set<MathNode> fallTransitions = new HashSet<>(signalTransitions);
            fallTransitions.retainAll(stg.getPostset(onePlace));
            fallTransitions.retainAll(stg.getPreset(zeroPlace));

            if (riseTransitions.isEmpty() || fallTransitions.isEmpty()) {
                continue;
            }

            result.put(signalRef, onePlace.getTokens() > 0);
        }
        return result;
    }

    public static HashMap<String, Boolean> getInitialState(StgModel stg, int timeout) {
        HashMap<String, Boolean> result = new HashMap<>();
        stg = copyStgPreserveSignals(stg);
        Set<String> signalRefs = stg.getSignalReferences();
        HashSet<HashMap<Place, Integer>> visitedMarkings = new HashSet<>();
        Queue<HashMap<Place, Integer>> queue = new LinkedList<>();
        HashMap<Place, Integer> initialMarking = PetriUtils.getMarking(stg);
        queue.add(initialMarking);
        long endTime = System.currentTimeMillis() + timeout;
        while (!queue.isEmpty() && !signalRefs.isEmpty() && System.currentTimeMillis() < endTime) {
            HashMap<Place, Integer> curMarking = queue.remove();
            visitedMarkings.add(curMarking);
            PetriUtils.setMarking(stg, curMarking);
            List<Transition> enabledTransitions = new ArrayList<>(PetriUtils.getEnabledTransitions(stg));
            for (Transition transition : enabledTransitions) {
                if (transition instanceof SignalTransition) {
                    SignalTransition signalTransition = (SignalTransition) transition;
                    String signalRef = stg.getSignalReference(signalTransition);
                    if (signalRefs.remove(signalRef)) {
                        result.put(signalRef, signalTransition.getDirection() == SignalTransition.Direction.MINUS);
                    }
                }
                if (signalRefs.isEmpty() || (System.currentTimeMillis() >= endTime)) break;
                stg.fire(transition);
                HashMap<Place, Integer> marking = PetriUtils.getMarking(stg);
                if (!visitedMarkings.contains(marking)) {
                    queue.add(marking);
                }
                stg.unFire(transition);
            }
        }
        PetriUtils.setMarking(stg, initialMarking);
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

    /**
     * Copy the given STG renaming signals. Note that STG places are copied without
     * hierarchy and their names are not preserved.
     *
     * @param stg an STG to be copied
     * @param newStg an STG to be populated
     * @return node mapping from the original stg to newStg
     */
    public static Map<MathNode, MathNode> copyStgRenameSignals(StgModel stg, Stg newStg, Map<String, String> signalRenames) {
        Map<MathNode, MathNode> result = new HashMap<>();
        // Copy signal transitions with their hierarchy, renaming their signals if necessary.
        for (SignalTransition signalTransition : stg.getSignalTransitions()) {
            String ref = stg.getNodeReference(signalTransition);
            Triple<String, SignalTransition.Direction, Integer> r = LabelParser.parseSignalTransition(ref);
            if (r != null) {
                String signalRef = r.getFirst();
                ref = signalRenames.getOrDefault(signalRef, signalRef) + r.getSecond();
            }
            SignalTransition newSignalTransition = newStg.createSignalTransition(ref, null);
            newSignalTransition.setSignalType(signalTransition.getSignalType());
            newSignalTransition.setDirection(signalTransition.getDirection());
            result.put(signalTransition, newSignalTransition);
        }
        // Copy dummy transitions with their hierarchy.
        for (DummyTransition dummyTransition : stg.getDummyTransitions()) {
            String ref = stg.getNodeReference(dummyTransition);
            DummyTransition newDummyTransition = newStg.createDummyTransition(ref, null);
            result.put(dummyTransition, newDummyTransition);
        }
        // Copy places WITHOUT their hierarchy -- implicit places cannot be copied (NOTE that implicit place ref in NOT C-style).
        for (Place place : stg.getPlaces()) {
            StgPlace newPlace = newStg.createPlace();
            newPlace.setCapacity(place.getCapacity());
            newPlace.setTokens(place.getTokens());
            result.put(place, newPlace);
        }
        // Connect places and transitions.
        for (Connection connection : stg.getConnections()) {
            MathNode firstNode = result.get(connection.getFirst());
            MathNode secondNode = result.get(connection.getSecond());
            try {
                newStg.connect(firstNode, secondNode);
            } catch (InvalidConnectionException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public static Color getTypeColor(Signal.Type type) {
        if (type != null) {
            switch (type) {
            case INPUT:
                return SignalCommonSettings.getInputColor();
            case OUTPUT:
                return SignalCommonSettings.getOutputColor();
            case INTERNAL:
                return SignalCommonSettings.getInternalColor();
            }
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
        for (SignalTransition st: stg.getSignalTransitions(type)) {
            if (st.getDirection() == SignalTransition.Direction.TOGGLE) {
                String signalRef = stg.getSignalReference(st);
                result.add(signalRef);
            }
        }
        return result;
    }

}