package org.workcraft.plugins.stg.tools;

import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.petri.VisualTransition;
import org.workcraft.plugins.stg.DummyTransition;
import org.workcraft.plugins.stg.STG;
import org.workcraft.plugins.stg.SignalTransition.Direction;
import org.workcraft.plugins.stg.SignalTransition.Type;
import org.workcraft.plugins.stg.VisualDummyTransition;
import org.workcraft.plugins.stg.VisualNamedTransition;
import org.workcraft.plugins.stg.VisualReadArc;
import org.workcraft.plugins.stg.VisualSTG;
import org.workcraft.plugins.stg.VisualSignalTransition;
import org.workcraft.util.Pair;

public class StgTransformationUtils {

	private static void replaceNamedTransition(VisualSTG stg, VisualNamedTransition oldTransition, VisualNamedTransition newTransition) {
		newTransition.copyPosition(oldTransition);
		newTransition.copyStyle(oldTransition);

		for (Node pred: stg.getPreset(oldTransition)) {
			try {
				VisualConnection oldPredConnection = (VisualConnection)stg.getConnection(pred, oldTransition);
				VisualConnection newPredConnection = stg.connect(pred, newTransition);
				newPredConnection.copyStyle(oldPredConnection);
			} catch (InvalidConnectionException e) {
				e.printStackTrace();
			}
		}

		for (Node succ: stg.getPostset(oldTransition)) {
			try {
				VisualConnection oldSuccConnection = (VisualConnection)stg.getConnection(oldTransition, succ);
				VisualConnection newSuccConnection = stg.connect(newTransition, succ);
				newSuccConnection.copyStyle(oldSuccConnection);
				newSuccConnection.copyShape(oldSuccConnection);
			} catch (InvalidConnectionException e) {
				e.printStackTrace();
			}
		}
		stg.remove(oldTransition);
	}

	static public VisualDummyTransition convertSignalToDummyTransition(VisualSTG stg, VisualSignalTransition signalTransition) {
		Container container = (Container)signalTransition.getParent();
		VisualDummyTransition dummyTransition = stg.createDummyTransition(null, container);
		replaceNamedTransition(stg, signalTransition, dummyTransition);
		return dummyTransition;
	}

	static public VisualSignalTransition convertDummyToSignalTransition(VisualSTG stg, VisualNamedTransition dummyTransition) {
		Container container = (Container)dummyTransition.getParent();
		VisualSignalTransition signalTransition = stg.createSignalTransition(null, Type.INTERNAL, Direction.TOGGLE, container);
		replaceNamedTransition(stg, dummyTransition, signalTransition);
		return signalTransition;
	}

	public static VisualDummyTransition convertDummyToDummyWithouInstance(VisualSTG stg, VisualDummyTransition dummyTransition) {
		DummyTransition mathDummyTransition = dummyTransition.getReferencedTransition();
		STG mathStg = (STG)stg.getMathModel();
		VisualDummyTransition newDummyTransition;
		if (mathStg.getInstanceNumber(mathDummyTransition) == 0) {
			newDummyTransition = dummyTransition;
		} else {
			Container container = (Container)dummyTransition.getParent();
			newDummyTransition = stg.createDummyTransition(null, container);
			replaceNamedTransition(stg, dummyTransition, newDummyTransition);
		}
		return newDummyTransition;
	}

	public static VisualReadArc convertDualArcToReadArc(VisualSTG stg, VisualConnection consumingArc, VisualConnection producingArc) {
		VisualReadArc readArc = null;
		VisualPlace place = null;
		if (consumingArc.getFirst() instanceof VisualPlace) {
			place = (VisualPlace)consumingArc.getFirst();
		}
		VisualTransition transition = null;
		if (consumingArc.getSecond() instanceof VisualTransition) {
			transition = (VisualTransition)consumingArc.getSecond();
		}
		if ((place != null) && (transition != null)) {
			try {
				stg.remove(consumingArc);
				stg.remove(producingArc);
				VisualConnection connection = stg.connectUndirected(place, transition);
				if (connection instanceof VisualReadArc) {
					readArc = (VisualReadArc)connection;
				}
			} catch (InvalidConnectionException e) {
			}
		}
		return readArc;
	}

	public static Pair<VisualConnection, VisualConnection> converReadArcTotDualArc(VisualSTG stg, VisualReadArc readArc) {
		VisualPlace place = null;
		if (readArc.getFirst() instanceof VisualPlace) {
			place = (VisualPlace)readArc.getFirst();
		}
		VisualTransition transition = null;
		if (readArc.getSecond() instanceof VisualTransition) {
			transition = (VisualTransition)readArc.getSecond();
		}
		VisualConnection consumingArc = null;
		VisualConnection producingArc = null;
		if ((place != null) && (transition != null)) {
			try {
				stg.remove(readArc);
				consumingArc = stg.connect(place, transition);
				producingArc = stg.connect(transition, place);
			} catch (InvalidConnectionException e) {
			}
		}
		return new Pair<VisualConnection, VisualConnection>(consumingArc, producingArc);
	}

}
