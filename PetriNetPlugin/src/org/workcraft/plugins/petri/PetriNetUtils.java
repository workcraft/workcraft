package org.workcraft.plugins.petri;

import java.util.HashSet;

import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.util.Hierarchy;
import org.workcraft.util.Pair;

public class PetriNetUtils {

	public static VisualReadArc convertDualArcToReadArc(VisualModel visualModel, VisualConnection consumingArc, VisualConnection producingArc) {
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
				visualModel.remove(consumingArc);
				visualModel.remove(producingArc);
				VisualConnection connection = visualModel.connectUndirected(place, transition);
				if (connection instanceof VisualReadArc) {
					readArc = (VisualReadArc)connection;
				}
			} catch (InvalidConnectionException e) {
			}
		}
		return readArc;
	}

	public static Pair<VisualConnection, VisualConnection> converReadArcTotDualArc(VisualModel visualModel, VisualReadArc readArc) {
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
				visualModel.remove(readArc);
				consumingArc = visualModel.connect(place, transition);
				producingArc = visualModel.connect(transition, place);
			} catch (InvalidConnectionException e) {
			}
		}
		return new Pair<VisualConnection, VisualConnection>(consumingArc, producingArc);
	}

	public static HashSet<VisualConnection> getVisualConsumingArcs(VisualModel visualModel) {
		HashSet<VisualConnection> connections = new HashSet<>();
		for (VisualConnection connection: Hierarchy.getDescendantsOfType(visualModel.getRoot(), VisualConnection.class)) {
			if (connection instanceof VisualReadArc) continue;
			if (connection.getSecond() instanceof VisualTransition) {
				connections.add(connection);
			}
		}
		return connections;
	}

	public static HashSet<VisualConnection> getVisualProducerArcs(VisualModel visualModel) {
		HashSet<VisualConnection> connections = new HashSet<>();
		for (VisualConnection connection: Hierarchy.getDescendantsOfType(visualModel.getRoot(), VisualConnection.class)) {
			if (connection instanceof VisualReadArc) continue;
			if (connection.getFirst() instanceof VisualTransition) {
				connections.add(connection);
			}
		}
		return connections;
	}

	public static HashSet<VisualReadArc> getVisualReadArcs(VisualModel visualModel) {
		return new HashSet<>(Hierarchy.getDescendantsOfType(visualModel.getRoot(), VisualReadArc.class));
	}

}
