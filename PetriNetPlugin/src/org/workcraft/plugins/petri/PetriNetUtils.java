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
		VisualNode first = consumingArc.getFirst();
		VisualNode second = consumingArc.getSecond();
		if (((first instanceof VisualPlace) || (first instanceof VisualReplicaPlace)) && (second instanceof VisualTransition)) {
			try {
				if (first instanceof VisualReplicaPlace) {
					first = copyReplicaPlace(visualModel, (VisualReplicaPlace)first);
				}
				visualModel.remove(consumingArc);
				visualModel.remove(producingArc);
				VisualConnection connection = visualModel.connectUndirected(first, second);
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
		VisualNode first = readArc.getFirst();
		VisualNode second = readArc.getSecond();
		VisualConnection consumingArc = null;
		VisualConnection producingArc = null;
		if (((first instanceof VisualPlace) || (first instanceof VisualReplicaPlace)) && (second instanceof VisualTransition)) {
			try {
				if (first instanceof VisualReplicaPlace) {
					first = copyReplicaPlace(visualModel, (VisualReplicaPlace)first);
				}
				visualModel.remove(readArc);
				consumingArc = visualModel.connect(first, second);
				consumingArc.copyShape(readArc);
				producingArc = visualModel.connect(second, first);
				producingArc.copyShape(readArc);
				producingArc.inverseShape();
			} catch (InvalidConnectionException e) {
			}
		}
		return new Pair<VisualConnection, VisualConnection>(consumingArc, producingArc);
	}

	private static VisualReplicaPlace copyReplicaPlace(VisualModel visualModel, VisualReplicaPlace replica) {
		Container container = Hierarchy.getNearestContainer(replica);
		VisualComponent master = replica.getMaster();
		VisualReplicaPlace result = visualModel.createVisualReplica(master, container, VisualReplicaPlace.class);
		result.copyPosition(replica);
		result.copyStyle(replica);
		return result;
	}

	public static VisualConnection collapseReplicaPlace(VisualModel visualModel, VisualReplicaPlace replica) {
		VisualConnection result = null;
		for (Connection c: visualModel.getConnections(replica)) {
			if (c instanceof VisualConnection) {
				VisualConnection vc = (VisualConnection)c;
				VisualNode first = vc.getFirst();
				VisualNode second = vc.getSecond();
				if (replica == first) {
					first = replica.getMaster();
				}
				if (replica == second) {
					second = replica.getMaster();
				}
				Point2D replicaPositionInRootSpace = replica.getRootSpacePosition();
				LinkedList<Point2D> locationsInRootSpace = ConnectionHelper.getSuffixControlPoints(vc, replicaPositionInRootSpace);
				locationsInRootSpace.addFirst(replicaPositionInRootSpace);
				visualModel.remove(vc);
				try {
					if (vc instanceof VisualReadArc) {
						result = visualModel.connectUndirected(first, second);
					} else {
						result = visualModel.connect(first, second);
					}
					ConnectionHelper.addControlPoints(result, locationsInRootSpace);
				} catch (InvalidConnectionException e) {
				}
			}
		}
		return result;
	}

	public static VisualConnection replicateReadArcPlace(VisualModel visualModel, VisualConnection connection) {
		VisualConnection result = null;
		VisualNode first = connection.getFirst();
		VisualNode second = connection.getSecond();
		VisualPlace place = null;
		VisualTransition transition = null;
		boolean reverse = false;
		if ((first instanceof VisualPlace) && (second instanceof VisualTransition)) {
			place = (VisualPlace)first;
			transition = (VisualTransition)second;
			reverse = false;
		} else if ((second instanceof VisualPlace) && (first instanceof VisualTransition)) {
			place = (VisualPlace)second;
			transition = (VisualTransition)first;
			reverse = true;
		}
		if ((place != null) && (transition != null)) {
			Container container = Hierarchy.getNearestContainer(transition);
			VisualReplicaPlace replica = visualModel.createVisualReplica(place, container, VisualReplicaPlace.class);
			Point2D splitPointInRootSpace = getReplicaPositionInRootSpace(connection);
			replica.setRootSpacePosition(splitPointInRootSpace);
			LinkedList<Point2D> locationsInRootSpace = ConnectionHelper.getSuffixControlPoints(connection, splitPointInRootSpace);
			visualModel.remove(connection);
			try {
				if (connection instanceof VisualReadArc) {
					result = visualModel.connectUndirected(replica, transition);
				} else {
					if (reverse) {
						result = visualModel.connect(transition, replica);
					} else {
						result = visualModel.connect(replica, transition);
					}
				}
				ConnectionHelper.addControlPoints(result, locationsInRootSpace);
			} catch (InvalidConnectionException exeption) {
			}
		}
		return result;
	}

	private static Point2D getReplicaPositionInRootSpace(VisualConnection connection) {
		Point2D positionInRootSpace = null;
		Point2D positionInLocalSpace = null;
		ConnectionGraphic graphic = connection.getGraphic();
		if (graphic instanceof Polyline) {
			Polyline polyline = (Polyline)graphic;
			ControlPoint cp = null;
			if (connection.getFirst() instanceof VisualPlace) {
				cp = polyline.getFirstControlPoint();
			} else if (connection.getSecond() instanceof VisualPlace) {
				cp = polyline.getLastControlPoint();
			}
			if (cp != null) {
				positionInLocalSpace = cp.getPosition();
			}
		}
		if (positionInLocalSpace == null) {
			positionInLocalSpace = connection.getSplitPoint();
		}
		if (positionInLocalSpace != null) {
			AffineTransform localToRootTransform = TransformHelper.getTransformToRoot(connection);
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
