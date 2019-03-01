package org.workcraft.plugins.graph.converters;

import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

import org.workcraft.dom.Connection;
import org.workcraft.dom.Node;
import org.workcraft.dom.references.HierarchyReferenceManager;
import org.workcraft.dom.references.NameManager;
import org.workcraft.dom.visual.ConnectionHelper;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualTransformableNode;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.plugins.graph.Graph;
import org.workcraft.plugins.graph.Symbol;
import org.workcraft.plugins.graph.VisualGraph;
import org.workcraft.plugins.graph.VisualVertex;
import org.workcraft.plugins.petri.VisualPetriNet;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.petri.VisualTransition;
import org.workcraft.utils.Hierarchy;

public class GraphToPetriConverter {
    private final VisualGraph srcModel;
    private final VisualPetriNet dstModel;

    private final Map<VisualConnection, VisualPlace> arcToPlaceMap;
    private final Map<VisualVertex, VisualTransition> vertexToTransitionMap;
    private final Map<String, String> refToSymbolMap;

    public GraphToPetriConverter(VisualGraph srcModel, VisualPetriNet dstModel) {
        this.srcModel = srcModel;
        this.dstModel = dstModel;
        arcToPlaceMap = convertArcs();
        vertexToTransitionMap = convertVertices();
        refToSymbolMap = cacheLabels();
        try {
            connect();
            connectTerminals();
        } catch (InvalidConnectionException e) {
            throw new RuntimeException(e);
        }
    }

    private Map<String, String> cacheLabels() {
        Map<String, String> result = new HashMap<>();
        for (Entry<VisualVertex, VisualTransition> entry: vertexToTransitionMap.entrySet()) {
            VisualVertex vertex = entry.getKey();
            VisualTransition transition = entry.getValue();
            Symbol symbol = vertex.getReferencedVertex().getSymbol();
            String dstName = dstModel.getMathName(transition);
            String srcName = (symbol == null) ? "" : srcModel.getMathName(symbol);
            result.put(dstName, srcName);
        }
        return result;
    }

    private Map<VisualConnection, VisualPlace> convertArcs() {
        Map<VisualConnection, VisualPlace> result = new HashMap<>();
        for (VisualConnection connection: Hierarchy.getDescendantsOfType(srcModel.getRoot(), VisualConnection.class)) {
            String name = srcModel.getMathModel().getNodeReference(connection.getReferencedConnection());
            VisualPlace place = dstModel.createPlace(name, null);
            place.setPosition(connection.getMiddleSegmentCenterPoint());
            place.setForegroundColor(connection.getColor());
            place.getReferencedPlace().setTokens(0);
            place.setTokenColor(connection.getColor());
            result.put(connection, place);
        }
        return result;
    }

    private Map<VisualVertex, VisualTransition> convertVertices() {
        Map<VisualVertex, VisualTransition> result = new HashMap<>();
        HierarchyReferenceManager refManager = (HierarchyReferenceManager) dstModel.getPetriNet().getReferenceManager();
        NameManager nameManagerer = refManager.getNameManager(null);
        for (VisualVertex vertex: Hierarchy.getDescendantsOfType(srcModel.getRoot(), VisualVertex.class)) {
            Symbol symbol = vertex.getReferencedVertex().getSymbol();
            String symbolName = (symbol == null) ? Graph.EPSILON_SERIALISATION : srcModel.getMathName(symbol);
            String name = nameManagerer.getDerivedName(null, symbolName);
            VisualTransition transition = dstModel.createTransition(name, null);
            transition.copyPosition(vertex);
            transition.copyStyle(vertex);
            if (symbol != null) {
                transition.setLabel(symbolName);
            }
            result.put(vertex, transition);
        }
        return result;
    }

    private void connect() throws InvalidConnectionException {
        for (VisualVertex vertex: Hierarchy.getDescendantsOfType(srcModel.getRoot(), VisualVertex.class)) {
            VisualTransition transition = vertexToTransitionMap.get(vertex);
            if (transition == null) continue;
            for (Connection srcConnection: srcModel.getConnections(vertex)) {
                if (srcConnection instanceof VisualConnection) {
                    VisualConnection arc = (VisualConnection) srcConnection;
                    VisualPlace place = arcToPlaceMap.get(arc);
                    if (place == null) continue;
                    Point2D splitPoint = arc.getSplitPoint();
                    if (arc.getFirst() == vertex) {
                        VisualConnection dstConnection = dstModel.connect(transition, place);
                        dstConnection.copyStyle(arc);
                        LinkedList<Point2D> prefixLocationsInRootSpace = ConnectionHelper.getPrefixControlPoints(arc, splitPoint);
                        ConnectionHelper.addControlPoints(dstConnection, prefixLocationsInRootSpace);
                    } else {
                        VisualConnection dstConnection = dstModel.connect(place, transition);
                        dstConnection.copyStyle(arc);
                        LinkedList<Point2D> suffixLocationsInRootSpace = ConnectionHelper.getSuffixControlPoints(arc, splitPoint);
                        ConnectionHelper.addControlPoints(dstConnection, suffixLocationsInRootSpace);
                    }
                }
            }
        }
    }

    private Point2D getBestPredPosition(VisualModel model, VisualTransformableNode node) {
        double dx = 0.0;
        double dy = 0.0;
        int count = 0;
        for (Connection connection: model.getConnections(node)) {
            Node second = connection.getSecond();
            if ((second != node) && (connection instanceof VisualConnection)) {
                Point2D pos = ((VisualConnection) connection).getMiddleSegmentCenterPoint();
                dx += pos.getX() - node.getX();
                dy += pos.getY() - node.getY();
                count++;
            }
        }
        double x = (count > 0) ? node.getX() - dx / count : node.getX() + 5.0;
        double y = (count > 0) ? node.getY() - dy / count : node.getY();
        return new Point2D.Double(x, y);
    }

    private Point2D getBestSuccPosition(VisualModel model, VisualTransformableNode node) {
        double dx = 0.0;
        double dy = 0.0;
        int count = 0;
        for (Connection connection: model.getConnections(node)) {
            Node first = connection.getFirst();
            if ((first != node) && (connection instanceof VisualConnection)) {
                Point2D pos = ((VisualConnection) connection).getMiddleSegmentCenterPoint();
                dx += node.getX() - pos.getX();
                dy += node.getY() - pos.getY();
                count++;
            }
        }
        double x = (count > 0) ? node.getX() + dx / count : node.getX() - 5.0;
        double y = (count > 0) ? node.getY() + dy / count : node.getY();
        return new Point2D.Double(x, y);
    }

    private void connectTerminals() throws InvalidConnectionException {
        for (VisualVertex vertex: Hierarchy.getDescendantsOfType(srcModel.getRoot(), VisualVertex.class)) {
            VisualTransition transition = vertexToTransitionMap.get(vertex);
            if (transition == null) continue;
            if (srcModel.getPreset(vertex).isEmpty()) {
                VisualPlace place = dstModel.createPlace(null, null);
                Point2D pos = getBestPredPosition(srcModel, vertex);
                place.setPosition(pos);
                place.getReferencedPlace().setTokens(1);
                dstModel.connect(place, transition);
            }
            if (srcModel.getPostset(vertex).isEmpty()) {
                VisualPlace place = dstModel.createPlace(null, null);
                Point2D pos = getBestSuccPosition(srcModel, vertex);
                place.setPosition(pos);
                place.getReferencedPlace().setTokens(0);
                dstModel.connect(transition, place);
            }
        }
    }

    public VisualGraph getSrcModel() {
        return srcModel;
    }

    public VisualPetriNet getDstModel() {
        return dstModel;
    }

    public VisualPlace getRelatedPlace(VisualConnection arc) {
        return arcToPlaceMap.get(arc);
    }

    public VisualTransition getRelatedTransition(VisualVertex vertex) {
        return vertexToTransitionMap.get(vertex);
    }

    public boolean isRelated(Node highLevelNode, Node node) {
        boolean result = false;
        if (highLevelNode instanceof VisualVertex) {
            VisualTransition transiton = getRelatedTransition((VisualVertex) highLevelNode);
            if (transiton != null) {
                result = (node == transiton) || (node == transiton.getReferencedTransition());
            }
        } else if (highLevelNode instanceof VisualConnection) {
            VisualPlace place = getRelatedPlace((VisualConnection) highLevelNode);
            if (place != null) {
                result = (node == place) || (node == place.getReferencedPlace());
            }
        }
        return result;
    }

    public String getSymbol(String ref) {
        return refToSymbolMap.get(ref);
    }

}
