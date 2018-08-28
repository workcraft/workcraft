package org.workcraft.plugins.stg;

import org.workcraft.Framework;
import org.workcraft.dom.Connection;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathModel;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.plugins.petri.PetriUtils;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.petri.Transition;
import org.workcraft.plugins.petri.VisualReadArc;
import org.workcraft.plugins.stg.interop.StgImporter;
import org.workcraft.util.DialogUtils;
import org.workcraft.util.LogUtils;
import org.workcraft.workspace.ModelEntry;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.*;

public class StgUtils {
    public static final String SPEC_FILE_PREFIX = "net";
    public static final String DEVICE_FILE_PREFIX = "dev";
    public static final String ENVIRONMENT_FILE_PREFIX = "env";
    public static final String SYSTEM_FILE_PREFIX = "sys";

    public static final String MUTEX_FILE_SUFFIX = "-mutex";
    public static final String MODIFIED_FILE_SUFFIX = "-mod";

    public static final String DETAIL_FILE_PREFIX = "detail";
    public static final String XML_FILE_EXTENSION = ".xml";

    private static void replaceNamedTransition(Stg stg, NamedTransition oldTransition, NamedTransition newTransition) {
        for (Node pred : stg.getPreset(oldTransition)) {
            try {
                stg.connect(pred, newTransition);
            } catch (InvalidConnectionException e) {
                e.printStackTrace();
            }
        }

        for (Node succ : stg.getPostset(oldTransition)) {
            try {
                stg.connect(newTransition, succ);
            } catch (InvalidConnectionException e) {
                e.printStackTrace();
            }
        }
        stg.remove(oldTransition);
    }

    public static DummyTransition convertSignalToDummyTransition(Stg stg, SignalTransition signalTransition) {
        Container container = (Container) signalTransition.getParent();
        DummyTransition dummyTransition = stg.createDummyTransition(null, container);
        replaceNamedTransition(stg, signalTransition, dummyTransition);
        return dummyTransition;
    }

    private static void replaceNamedTransition(VisualStg stg, VisualNamedTransition oldTransition, VisualNamedTransition newTransition) {
        newTransition.copyPosition(oldTransition);
        newTransition.copyStyle(oldTransition);

        for (Node pred : stg.getPreset(oldTransition)) {
            try {
                VisualConnection oldPredConnection = (VisualConnection) stg.getConnection(pred, oldTransition);
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

        for (Node succ : stg.getPostset(oldTransition)) {
            try {
                VisualConnection oldSuccConnection = (VisualConnection) stg.getConnection(oldTransition, succ);
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

    public static VisualDummyTransition convertDummyToDummyWithouInstance(VisualStg stg, VisualDummyTransition dummyTransition) {
        DummyTransition mathDummyTransition = dummyTransition.getReferencedTransition();
        Stg mathStg = (Stg) stg.getMathModel();
        VisualDummyTransition newDummyTransition;
        if (mathStg.getInstanceNumber(mathDummyTransition) == 0) {
            newDummyTransition = dummyTransition;
        } else {
            Container container = (Container) dummyTransition.getParent();
            newDummyTransition = stg.createVisualDummyTransition(null, container);
            replaceNamedTransition(stg, dummyTransition, newDummyTransition);
        }
        return newDummyTransition;
    }

    // Load STG model from .work or .g file
    public static Stg loadStg(File file) {
        Stg result = null;
        if (file != null) {
            Framework framework = Framework.getInstance();
            String filePath = file.getAbsolutePath();
            try {
                ModelEntry me = framework.loadModel(file);
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

    public static void convertInternalSignalsToDummies(Stg stg) {
        for (SignalTransition transition : stg.getSignalTransitions(Signal.Type.INTERNAL)) {
            StgUtils.convertSignalToDummyTransition(stg, transition);
        }
    }

    public static boolean isSameSignals(StgModel srcStg, StgModel dstStg) {
        Set<String> srcInputs = srcStg.getSignalReferences(Signal.Type.INPUT);
        Set<String> srcOutputs = srcStg.getSignalReferences(Signal.Type.OUTPUT);
        Set<String> srcInternal = srcStg.getSignalReferences(Signal.Type.INTERNAL);

        Set<String> dstInputs = dstStg.getSignalReferences(Signal.Type.INPUT);
        Set<String> dstOutputs = dstStg.getSignalReferences(Signal.Type.OUTPUT);
        Set<String> dstInternal = dstStg.getSignalReferences(Signal.Type.INTERNAL);

        return srcInputs.equals(dstInputs) && srcOutputs.equals(dstOutputs) && srcInternal.equals(dstInternal);
    }

    public static StgModel importStg(File file) {
        StgModel result = null;
        if (file != null) {
            try {
                FileInputStream is = new FileInputStream(file);
                StgImporter importer = new StgImporter();
                result = importer.importStg(is);
            } catch (DeserialisationException | FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        return result;
    }

    public static HashSet<SignalTransition> getEnabledSignalTransitions(StgModel stg) {
        HashSet<SignalTransition> result = new HashSet<>();
        for (Transition transition : PetriUtils.getEnabledTransitions(stg)) {
            if (transition instanceof SignalTransition) {
                result.add((SignalTransition) transition);
            }
        }
        return result;
    }

    public static HashSet<String> getEnabledLocalSignals(StgModel stg) {
        HashSet<String> result = new HashSet<>();
        for (SignalTransition transition : getEnabledSignalTransitions(stg)) {
            if ((transition.getSignalType() == Signal.Type.OUTPUT) || (transition.getSignalType() == Signal.Type.INTERNAL)) {
                result.add(transition.getSignalName());
            }
        }
        return result;
    }

    public static HashSet<String> getEnabledSignals(StgModel stg) {
        HashSet<String> result = new HashSet<>();
        for (SignalTransition transition : getEnabledSignalTransitions(stg)) {
            result.add(transition.getSignalName());
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
        Map<MathNode, MathNode> nodeMap = new HashMap<>();
        // Copy signal transitions with their hierarchy.
        for (SignalTransition signalTransition : stg.getSignalTransitions()) {
            String ref = stg.getNodeReference(signalTransition);
            SignalTransition newSignalTransition = result.createSignalTransition(ref, null);
            newSignalTransition.setSignalType(signalTransition.getSignalType());
            newSignalTransition.setDirection(signalTransition.getDirection());
            nodeMap.put(signalTransition, newSignalTransition);
        }
        // Copy dummy transitions with their hierarchy.
        for (DummyTransition dummyTransition : stg.getDummyTransitions()) {
            String ref = stg.getNodeReference(dummyTransition);
            DummyTransition newDummyTransition = result.createDummyTransition(ref, null);
            nodeMap.put(dummyTransition, newDummyTransition);
        }
        // Copy places WITHOUT their hierarchy -- implicit places cannot be copied (NOTE that implicit place ref in NOT C-style).
        for (Place place : stg.getPlaces()) {
            StgPlace newPlace = result.createPlace();
            newPlace.setCapacity(place.getCapacity());
            newPlace.setTokens(place.getTokens());
            nodeMap.put(place, newPlace);
        }
        // Connect places and transitions.
        for (Connection connection : stg.getConnections()) {
            Node first = nodeMap.get(connection.getFirst());
            Node second = nodeMap.get(connection.getSecond());
            try {
                result.connect(first, second);
            } catch (InvalidConnectionException e) {
                e.printStackTrace();
            }
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

    public static boolean checkStg(StgModel stg, boolean ask) {
        String msg = "";
        Set<String> hangingTransitions = new HashSet<>();
        Set<String> unboundedTransitions = new HashSet<>();
        for (Transition transition : stg.getTransitions()) {
            if (stg.getPreset(transition).isEmpty()) {
                String ref = stg.getNodeReference(transition);
                if (stg.getPostset(transition).isEmpty()) {
                    hangingTransitions.add(ref);
                } else {
                    unboundedTransitions.add(ref);
                }
            }
        }
        if (!hangingTransitions.isEmpty()) {
            msg += LogUtils.getTextWithRefs("\n* Disconnected transition", hangingTransitions);
        }
        if (!unboundedTransitions.isEmpty()) {
            msg += LogUtils.getTextWithRefs("\n* Empty preset transition", unboundedTransitions);
        }

        Set<String> hangingPlaces = new HashSet<>();
        Set<String> deadPlaces = new HashSet<>();
        for (Place place : stg.getPlaces()) {
            if (stg.getPreset(place).isEmpty()) {
                String ref = stg.getNodeReference(place);
                if (stg.getPostset(place).isEmpty()) {
                    hangingPlaces.add(ref);
                } else if (place.getTokens() == 0) {
                    deadPlaces.add(ref);
                }
            }
        }
        if (!hangingPlaces.isEmpty()) {
            msg += LogUtils.getTextWithRefs("\n* Disconnected place", hangingPlaces);
        }
        if (!deadPlaces.isEmpty()) {
            msg += LogUtils.getTextWithRefs("\n* Dead place", deadPlaces);
        }

        if (!msg.isEmpty()) {
            msg = "The STG model has the following issues:" + msg;
            if (ask) {
                msg += "\n\n Proceed anyway?";
                return DialogUtils.showConfirmWarning(msg, "Model validation", false);
            } else {
                DialogUtils.showWarning(msg);
            }
        }
        return true;
    }

}
