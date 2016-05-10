package org.workcraft.plugins.stg;

import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.plugins.petri.VisualReadArc;
import org.workcraft.plugins.stg.SignalTransition.Direction;
import org.workcraft.plugins.stg.SignalTransition.Type;

public class StgUtils {
    public static final String DEVICE_FILE_NAME = "device";
    public static final String ENVIRONMENT_FILE_NAME = "environment";
    public static final String SYSTEM_FILE_NAME = "system";
    public static final String MODIFIED_FILE_SUFFIX = "_mod";
    public static final String ASTG_FILE_EXT = ".g";
    public static final String PLACES_FILE_NAME = "places";
    public static final String LIST_FILE_EXT = ".list";

    private static void replaceNamedTransition(STG stg, NamedTransition oldTransition, NamedTransition newTransition) {
        for (Node pred: stg.getPreset(oldTransition)) {
            try {
                stg.connect(pred, newTransition);
            } catch (InvalidConnectionException e) {
                e.printStackTrace();
            }
        }

        for (Node succ: stg.getPostset(oldTransition)) {
            try {
                stg.connect(newTransition, succ);
            } catch (InvalidConnectionException e) {
                e.printStackTrace();
            }
        }
        stg.remove(oldTransition);
    }

    public static DummyTransition convertSignalToDummyTransition(STG stg, SignalTransition signalTransition) {
        Container container = (Container) signalTransition.getParent();
        DummyTransition dummyTransition = stg.createDummyTransition(null, container);
        replaceNamedTransition(stg, signalTransition, dummyTransition);
        return dummyTransition;
    }

    private static void replaceNamedTransition(VisualSTG stg, VisualNamedTransition oldTransition, VisualNamedTransition newTransition) {
        newTransition.copyPosition(oldTransition);
        newTransition.copyStyle(oldTransition);

        for (Node pred: stg.getPreset(oldTransition)) {
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

        for (Node succ: stg.getPostset(oldTransition)) {
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

    public static VisualDummyTransition convertSignalToDummyTransition(VisualSTG stg, VisualSignalTransition signalTransition) {
        Container container = (Container) signalTransition.getParent();
        VisualDummyTransition dummyTransition = stg.createDummyTransition(null, container);
        replaceNamedTransition(stg, signalTransition, dummyTransition);
        return dummyTransition;
    }

    public static VisualSignalTransition convertDummyToSignalTransition(VisualSTG stg, VisualNamedTransition dummyTransition) {
        Container container = (Container) dummyTransition.getParent();
        VisualSignalTransition signalTransition = stg.createSignalTransition(null, Type.INTERNAL, Direction.TOGGLE, container);
        replaceNamedTransition(stg, dummyTransition, signalTransition);
        return signalTransition;
    }

    public static VisualDummyTransition convertDummyToDummyWithouInstance(VisualSTG stg, VisualDummyTransition dummyTransition) {
        DummyTransition mathDummyTransition = dummyTransition.getReferencedTransition();
        STG mathStg = (STG) stg.getMathModel();
        VisualDummyTransition newDummyTransition;
        if (mathStg.getInstanceNumber(mathDummyTransition) == 0) {
            newDummyTransition = dummyTransition;
        } else {
            Container container = (Container) dummyTransition.getParent();
            newDummyTransition = stg.createDummyTransition(null, container);
            replaceNamedTransition(stg, dummyTransition, newDummyTransition);
        }
        return newDummyTransition;
    }

}
