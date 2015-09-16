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
import org.workcraft.dom.visual.connections.ConnectionGraphic;
import org.workcraft.dom.visual.connections.ControlPoint;
import org.workcraft.dom.visual.connections.Polyline;
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
				connection.copyShape(consumingArc);
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
				consumingArc.copyShape(readArc);
				producingArc = visualModel.connect(transition, place);
				producingArc.copyShape(readArc);
				producingArc.inverseShape();
			} catch (InvalidConnectionException e) {
			}
		}
		return new Pair<VisualConnection, VisualConnection>(consumingArc, producingArc);
	}

	public static VisualConnection collapseReplicaPlace(VisualModel visualModel, VisualReplicaPlace replica) {
		VisualConnection result = null;
		for (Connection connection: visualModel.getConnections(replica)) {
			if ((connection instanceof VisualReadArc) && (connection.getFirst() == replica)) {
				VisualReadArc readArc = (VisualReadArc)connection;
				VisualNode transition = readArc.getSecond();
				VisualComponent master = replica.getMaster();
				Point2D replicaPositionInRootSpace = replica.getRootSpacePosition();
				LinkedList<Point2D> locationsInRootSpace = ConnectionHelper.getSuffixControlPoints(readArc, replicaPositionInRootSpace);
				locationsInRootSpace.addFirst(replicaPositionInRootSpace);
				visualModel.remove(replica);
				try {
					result = visualModel.connectUndirected(master, transition);
					ConnectionHelper.addControlPoints(result, locationsInRootSpace);
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
			Point2D splitPointInRootSpace = getReplicaPositionInRootSpace(readArc);
			replica.setRootSpacePosition(splitPointInRootSpace);
			LinkedList<Point2D> locationsInRootSpace = ConnectionHelper.getSuffixControlPoints(readArc, splitPointInRootSpace);
			visualModel.remove(readArc);
			try {
				result = visualModel.connectUndirected(replica, transition);
				ConnectionHelper.addControlPoints(result, locationsInRootSpace);
			} catch (InvalidConnectionException exeption) {
			}
		}
		return result;
	}

	private static Point2D getReplicaPositionInRootSpace(VisualReadArc readArc) {
		Point2D positionInRootSpace = null;
		Point2D positionInLocalSpace = null;
		ConnectionGraphic graphic = readArc.getGraphic();
		if (graphic instanceof Polyline) {
			Polyline polyline = (Polyline)graphic;
			ControlPoint cp = polyline.getFirstControlPoint();
			if (cp != null) {
				positionInLocalSpace = cp.getPosition();
			}
		}
		if (positionInLocalSpace == null) {
			positionInLocalSpace = readArc.getSplitPoint();
		}
		if (positionInLocalSpace != null) {
			AffineTransform localToRootTransform = TransformHelper.getTransformToRoot(readArc);
			positionInRootSpace = localToRootTransform.transform(positionInLocalSpace, null);
		}
		return positionInRootSpace;
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
