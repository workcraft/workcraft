package org.workcraft.plugins.stg;

import java.io.File;
import java.util.Collection;
import java.util.Set;

import org.workcraft.Framework;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathModel;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.plugins.petri.VisualReadArc;
import org.workcraft.plugins.stg.SignalTransition.Direction;
import org.workcraft.plugins.stg.SignalTransition.Type;
import org.workcraft.util.LogUtils;
import org.workcraft.workspace.ModelEntry;

public class StgUtils {
    public static final String SPEC_FILE_NAME = "net";
    public static final String DEVICE_FILE_NAME = "dev";
    public static final String ENVIRONMENT_FILE_NAME = "env";
    public static final String SYSTEM_FILE_NAME = "sys";

    public static final String MUTEX_FILE_SUFFIX = "-mutex";
    public static final String MODIFIED_FILE_SUFFIX = "-mod";

    public static final String PLACES_FILE_NAME = "places.list";

    private static void replaceNamedTransition(Stg stg, NamedTransition oldTransition, NamedTransition newTransition) {
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

    public static DummyTransition convertSignalToDummyTransition(Stg stg, SignalTransition signalTransition) {
        Container container = (Container) signalTransition.getParent();
        DummyTransition dummyTransition = stg.createDummyTransition(null, container);
        replaceNamedTransition(stg, signalTransition, dummyTransition);
        return dummyTransition;
    }

    private static void replaceNamedTransition(VisualStg stg, VisualNamedTransition oldTransition, VisualNamedTransition newTransition) {
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

    public static VisualDummyTransition convertSignalToDummyTransition(VisualStg stg, VisualSignalTransition signalTransition) {
        Container container = (Container) signalTransition.getParent();
        VisualDummyTransition dummyTransition = stg.createVisualDummyTransition(null, container);
        replaceNamedTransition(stg, signalTransition, dummyTransition);
        return dummyTransition;
    }

    public static VisualSignalTransition convertDummyToSignalTransition(VisualStg stg, VisualNamedTransition dummyTransition) {
        Container container = (Container) dummyTransition.getParent();
        VisualSignalTransition signalTransition = stg.createVisualSignalTransition(null, Type.INTERNAL, Direction.TOGGLE, container);
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
            try {
                ModelEntry me = framework.loadModel(file);
                if (me != null) {
                    MathModel model = me.getMathModel();
                    if (model instanceof Stg) {
                        result = (Stg) model;
                    } else {
                        LogUtils.logError("Model in file '" + file.getAbsolutePath() + "' is not an STG.");
                    }
                } else {
                    LogUtils.logError("Cannot read file '" + file.getAbsolutePath() + "'.");
                }
            } catch (DeserialisationException e) {
                LogUtils.logError("Cannot read STG model from file '" + file.getAbsolutePath() + "': "
                        + e.getMessage());
            }
        }
        return result;
    }

    public static void restoreInterfaceSignals(Stg stg, Collection<String> inputSignalNames, Collection<String> outputSignalNames) {
        Container container = stg.getRoot();
        for (String signalName: stg.getSignalNames(container)) {
            stg.setSignalType(signalName, Type.INTERNAL, container);
        }
        for (String inputName: inputSignalNames) {
            stg.setSignalType(inputName, Type.INPUT, container);
        }
        for (String outputName: outputSignalNames) {
            stg.setSignalType(outputName, Type.OUTPUT, container);
        }
    }

    public static void convertInternalSignalsToDummies(Stg stg) {
        for (SignalTransition transition: stg.getSignalTransitions(Type.INTERNAL)) {
            StgUtils.convertSignalToDummyTransition(stg, transition);
        }
    }

    public static boolean isSameSignals(StgModel srcStg, StgModel dstStg) {
        Set<String> srcInputs = srcStg.getSignalReferences(Type.INPUT);
        Set<String> srcOutputs = srcStg.getSignalReferences(Type.OUTPUT);
        Set<String> srcInternal = srcStg.getSignalReferences(Type.INTERNAL);

        Set<String> dstInputs = dstStg.getSignalReferences(Type.INPUT);
        Set<String> dstOutputs = dstStg.getSignalReferences(Type.OUTPUT);
        Set<String> dstInternal = dstStg.getSignalReferences(Type.INTERNAL);

        return srcInputs.equals(dstInputs) && srcOutputs.equals(dstOutputs) && srcInternal.equals(dstInternal);
    }

}
