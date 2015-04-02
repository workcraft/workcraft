package org.workcraft.plugins.stg.tools;

import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.plugins.stg.DummyTransition;
import org.workcraft.plugins.stg.STG;
import org.workcraft.plugins.stg.SignalTransition.Direction;
import org.workcraft.plugins.stg.SignalTransition.Type;
import org.workcraft.plugins.stg.VisualDummyTransition;
import org.workcraft.plugins.stg.VisualNamedTransition;
import org.workcraft.plugins.stg.VisualSTG;
import org.workcraft.plugins.stg.VisualSignalTransition;

public class StgTransformationUtils {

	private static void replaceNamedTransition(VisualSTG stg, VisualNamedTransition oldTransition, VisualNamedTransition newTransition) {
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

}
