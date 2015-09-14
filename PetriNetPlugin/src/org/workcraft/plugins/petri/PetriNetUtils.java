package org.workcraft.plugins.petri;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.HashSet;
import java.util.LinkedList;

import org.workcraft.dom.Connection;
import org.workcraft.dom.Container;
import org.workcraft.dom.visual.ConnectionHelper;
import org.workcraft.dom.visual.TransformHelper;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
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

	public static VisualConnection collapseReplicaPlace(VisualModel visualModel, VisualReplicaPlace replica) {
		VisualConnection result = null;
		for (Connection connection: visualModel.getConnections(replica)) {
			if ((connection instanceof VisualReadArc) && (connection.getFirst() == replica)) {
				VisualNode transition = ((VisualReadArc)connection).getSecond();
				VisualComponent master = replica.getMaster();
				visualModel.remove(replica);
				try {
					result = visualModel.connectUndirected(master, transition);
				} catch (InvalidConnectionException e) {
				}
			}
		}
		return result;
	}

	public static VisualConnection expandReplicaPlace(VisualModel visualModel, VisualReadArc readArc) {
		VisualConnection result = null;
		VisualNode first = readArc.getFirst();
		VisualNode second = readArc.getSecond();
		if ((first instanceof VisualPlace) && (second instanceof VisualTransition)) {
			VisualPlace place = (VisualPlace)first;
			VisualTransition transition = (VisualTransition)second;
			Container container = Hierarchy.getNearestContainer(transition);
			VisualReplicaPlace replica = visualModel.createVisualReplica(place, container, VisualReplicaPlace.class);
			Point2D splitPoint = readArc.getSplitPoint();
			AffineTransform localToRootTransform = TransformHelper.getTransformToRoot(readArc);
			Point2D splitPointInRootSpace = localToRootTransform.transform(splitPoint, null);
			replica.setRootSpacePosition(splitPointInRootSpace);
			LinkedList<Point2D> locationsInRootSpace = ConnectionHelper.getSuffixControlPoints(readArc, splitPoint);
			visualModel.remove(readArc);
			try {
				result = visualModel.connectUndirected(replica, transition);
				ConnectionHelper.addControlPoints(result, locationsInRootSpace);
			} catch (InvalidConnectionException exeption) {
			}
		}
		return result;
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

	public static HashSet<VisualReplicaPlace> getVisualReplicaPlaces(VisualModel visualModel) {
		return new HashSet<>(Hierarchy.getDescendantsOfType(visualModel.getRoot(), VisualReplicaPlace.class));
	}

	public static HashSet<VisualPlace> getVisualPlaces(VisualModel visualModel) {
		return new HashSet<>(Hierarchy.getDescendantsOfType(visualModel.getRoot(), VisualPlace.class));
	}

}
