package org.workcraft.plugins.dfs.commands;

import org.workcraft.dom.Container;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.visual.*;
import org.workcraft.dom.visual.connections.ControlPoint;
import org.workcraft.dom.visual.connections.Polyline;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.dom.visual.connections.VisualConnection.ConnectionType;
import org.workcraft.dom.visual.connections.VisualConnection.ScaleMode;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.plugins.dfs.BinaryRegister.Marking;
import org.workcraft.plugins.dfs.*;
import org.workcraft.plugins.dfs.ControlRegister.SynchronisationType;
import org.workcraft.types.Pair;
import org.workcraft.utils.Hierarchy;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class WaggingGenerator {

    private final VisualDfs dfs;
    private final int count;

    private final HashSet<VisualComponent> selectedComponents = new HashSet<>();
    private final HashSet<VisualConnection> selectedConnections = new HashSet<>();
    private final HashMap<VisualComponent, VisualComponent> replicaToOriginalMap = new HashMap<>();
    private final ArrayList<WaggingData> wagging = new ArrayList<>();

    private static final class WaggingData {
        public final HashSet<VisualComponent> dataComponents = new HashSet<>();
        public final HashSet<VisualPushRegister> pushRegisters = new HashSet<>();
        public final HashSet<VisualControlRegister> pushControls = new HashSet<>();
        public final HashSet<VisualPopRegister> popRegisters = new HashSet<>();
        public final HashSet<VisualControlRegister> popControls = new HashSet<>();
    }

    public WaggingGenerator(VisualDfs dfs, int count) {
        this.dfs = dfs;
        this.count = count;

        for (VisualNode node: dfs.getSelection()) {
            if (node instanceof VisualComponent) {
                selectedComponents.add((VisualComponent) node);
            } else if (node instanceof VisualConnection) {
                selectedConnections.add((VisualConnection) node);
            }
        }
        for (VisualConnection connection: Hierarchy.getDescendantsOfType(dfs.getRoot(), VisualConnection.class)) {
            if (selectedComponents.contains(connection.getFirst()) && selectedComponents.contains(connection.getSecond())) {
                selectedConnections.add(connection);
            }
        }
    }

    public void run() {
        replicateSelection();
        Pair<Boolean, Boolean> hasInterface = insertInterface();
        if (hasInterface.getFirst()) {
            insertPushControl();
        }
        if (hasInterface.getSecond()) {
            insertPopControl();
        }
        cleanup();
        group();
    }

    private void replicateSelection() {
        replicaToOriginalMap.clear();
        wagging.clear();
        Rectangle2D bb = null;
        for (VisualComponent component: selectedComponents) {
            bb = BoundingBoxHelper.union(bb, component.getBoundingBox());
        }
        if (bb != null) {
            double step = Math.ceil(bb.getHeight());
            for (int i = 0; i < count; ++i) {
                HashMap<VisualComponent, VisualComponent> mapComponentToReplica = new HashMap<>();
                WaggingData waggingData = new WaggingData();
                for (VisualComponent component: selectedComponents) {
                    VisualComponent replicaComponenet = replicateComponent(component);
                    if (replicaComponenet != null) {
                        replicaComponenet.setY(replicaComponenet.getY() + step * (2 * i + 1 - count) / 2);
                        mapComponentToReplica.put(component, replicaComponenet);
                        replicaToOriginalMap.put(replicaComponenet, component);
                        waggingData.dataComponents.add(replicaComponenet);
                    }
                }
                for (VisualConnection connection: selectedConnections) {
                    replicateConnection(connection, mapComponentToReplica);
                }
                wagging.add(waggingData);
            }
        }
    }

    private VisualComponent replicateComponent(VisualComponent component) {
        VisualComponent replica = null;
        if (component instanceof VisualLogic) {
            replica = new VisualLogic(new Logic());
        } else if (component instanceof VisualRegister) {
            replica = new VisualRegister(new Register());
        } else if (component instanceof VisualControlRegister) {
            replica = new VisualControlRegister(new ControlRegister());
        } else if (component instanceof VisualPushRegister) {
            replica = new VisualPushRegister(new PushRegister());
        } else if (component instanceof VisualPopRegister) {
            replica = new VisualPopRegister(new PopRegister());
        }

        if (replica != null) {
            replica.copyPosition(component);
            replica.copyStyle(component);
            // postpone adding to the model so no notifications are sent too early
            dfs.getMathModel().add(replica.getReferencedComponent());
            Hierarchy.getNearestContainer(component).add(replica);
        }
        return replica;
    }

    private VisualConnection replicateConnection(VisualConnection connection, HashMap<VisualComponent, VisualComponent> c2c) {
        VisualConnection replica = null;
        VisualComponent first = c2c.get(connection.getFirst());
        VisualComponent second = c2c.get(connection.getSecond());
        if ((first != null) && (second != null)) {
            if (connection instanceof VisualControlConnection) {
                ControlConnection connectionRef = ((VisualControlConnection) connection).getReferencedConnection();
                replica = createControlConnection(first, second, connectionRef.isInverting());
            } else {
                replica = createConnection(first, second);
            }
            replica.copyStyle(connection);
            replica.copyShape(connection);
            Point2D p = connection.getFirstCenter();
            Point2D offset = new Point2D.Double(first.getX() - p.getX(), first.getY() - p.getY());
            ConnectionHelper.moveControlPoints(replica, offset);
        }
        return replica;
    }

    private Pair<Boolean, Boolean> insertInterface() {
        boolean hasPred = false;
        boolean hasSucc = false;
        for (WaggingData waggingData: wagging) {
            waggingData.pushRegisters.clear();
            waggingData.popRegisters.clear();
            for (VisualComponent cur: waggingData.dataComponents) {
                for (VisualNode pred: dfs.getPreset(replicaToOriginalMap.get(cur))) {
                    if (selectedComponents.contains(pred)) continue;
                    Point2D.Double position = new Point2D.Double(cur.getX() / 2 + ((VisualComponent) pred).getX() / 2, cur.getY());
                    VisualPushRegister push = createPushRegister(Hierarchy.getNearestContainer(cur, pred), position);
                    createConnection((VisualComponent) pred, push);
                    createConnection(push, cur);
                    waggingData.pushRegisters.add(push);
                    hasPred = true;
                }
                for (VisualNode succ: dfs.getPostset(replicaToOriginalMap.get(cur))) {
                    if (selectedComponents.contains(succ)) continue;
                    Point2D.Double position = new Point2D.Double(cur.getX() / 2 + ((VisualComponent) succ).getX() / 2, cur.getY());
                    VisualPopRegister pop = createPopRegister(Hierarchy.getNearestContainer(cur, succ), position);
                    createConnection(cur, pop);
                    createConnection(pop, (VisualComponent) succ);
                    waggingData.popRegisters.add(pop);
                    hasSucc = true;
                }
            }
        }
        return Pair.of(hasPred, hasSucc);
    }

    private void insertPushControl() {
        Container container = getCommonContainer();
        Rectangle2D bb = getBoundingBox();
        double xPos = Math.floor(bb.getMinX());
        double yPos = Math.floor(bb.getMaxY());
        int iPos = 0;
        VisualControlRegister predReg1 = null;
        VisualControlRegister firstReg0 = null;
        for (WaggingData waggingData: wagging) {
            iPos++;
            // create control registers
            VisualControlRegister reg0 = createControlRegister(container,
                    new Point2D.Double(xPos - 2.0, yPos + iPos * 2.0),
                    predReg1 == null ? Marking.TRUE_TOKEN : Marking.FALSE_TOKEN, SynchronisationType.AND);
            reg0.getReferencedComponent().setProbability(1.0 / count);
            VisualControlRegister reg1 = createControlRegister(container,
                    new Point2D.Double(xPos - 4.0, yPos + iPos * 2.0),
                    Marking.EMPTY, SynchronisationType.PLAIN);
            reg1.getReferencedComponent().setProbability(1.0 / count);
            VisualControlRegister reg2 = createControlRegister(container,
                    new Point2D.Double(xPos - 6.0, yPos + iPos * 2.0),
                    Marking.EMPTY, SynchronisationType.PLAIN);
            reg2.getReferencedComponent().setProbability(1.0 / count);
            waggingData.pushControls.add(reg0);
            waggingData.pushControls.add(reg1);
            waggingData.pushControls.add(reg2);
            // connection within control layer
            VisualControlConnection con0 = createControlConnection(reg0, reg2, false);
            convertConnectionToPolyline(con0, 0.0, 1.0, 0.0, 1.0);
            createControlConnection(reg1, reg0, true);
            createControlConnection(reg2, reg1, false);
            // connection to the push registers
            for (VisualPushRegister push: waggingData.pushRegisters) {
                createControlConnection(reg0, push, false);
            }
            // connection between control layers
            if (predReg1 == null) {
                firstReg0 = reg0;
            } else {
                createControlConnection(predReg1, reg0, false);
            }
            predReg1 = reg1;
        }
        if (firstReg0 != null && predReg1 != null) {
            createControlConnection(predReg1, firstReg0, false);
        }
    }

    private void insertPopControl() {
        Container container = getCommonContainer();
        Rectangle2D bb = getBoundingBox();
        double xPos = Math.floor(bb.getMaxX());
        double yPos = Math.floor(bb.getMaxY());
        int iPos = 0;
        VisualControlRegister predReg1 = null;
        VisualControlRegister firstReg0 = null;
        for (WaggingData waggingData: wagging) {
            iPos++;
            // create control registers
            VisualControlRegister reg0 = createControlRegister(container,
                    new Point2D.Double(xPos + 2.0, yPos + iPos * 2.0),
                    predReg1 == null ? Marking.TRUE_TOKEN : Marking.FALSE_TOKEN, SynchronisationType.AND);
            reg0.getReferencedComponent().setProbability(1.0 / count);
            VisualControlRegister reg1 = createControlRegister(container,
                    new Point2D.Double(xPos + 4.0, yPos + iPos * 2.0),
                    Marking.EMPTY, SynchronisationType.PLAIN);
            reg1.getReferencedComponent().setProbability(1.0 / count);
            VisualControlRegister reg2 = createControlRegister(container,
                    new Point2D.Double(xPos + 6.0, yPos + iPos * 2.0),
                    Marking.EMPTY, SynchronisationType.PLAIN);
            reg2.getReferencedComponent().setProbability(1.0 / count);
            waggingData.popControls.add(reg0);
            waggingData.popControls.add(reg1);
            waggingData.popControls.add(reg2);
            // connection within control layer
            VisualControlConnection con0 = createControlConnection(reg0, reg2, false);
            convertConnectionToPolyline(con0, 0.0, 1.0, 0.0, 1.0);
            createControlConnection(reg1, reg0, true);
            createControlConnection(reg2, reg1, false);
            // connection to the pop registers
            for (VisualPopRegister pop: waggingData.popRegisters) {
                createControlConnection(reg0, pop, false);
            }
            // connection between control layers
            if (predReg1 == null) {
                firstReg0 = reg0;
            } else {
                createControlConnection(predReg1, reg0, false);
            }
            predReg1 = reg1;
        }
        if (firstReg0 != null && predReg1 != null) {
            createControlConnection(predReg1, firstReg0, false);
        }
    }

    private void cleanup() {
        dfs.selectNone();
        for (VisualComponent component: selectedComponents) {
            dfs.addToSelection(component);
        }
        dfs.deleteSelection();
    }

    private void group() {
        // data components
        ArrayList<VisualNode> dataNodes = new ArrayList<>();
        for (WaggingData waggingData: wagging) {
            dataNodes.addAll(waggingData.dataComponents);
            dataNodes.addAll(waggingData.pushRegisters);
            dataNodes.addAll(waggingData.popRegisters);
        }
        dfs.select(dataNodes);
        dfs.groupSelection();
        // push control
        ArrayList<VisualNode> pushNodes = new ArrayList<>();
        for (WaggingData waggingData: wagging) {
            pushNodes.addAll(waggingData.pushControls);
        }
        dfs.select(pushNodes);
        dfs.groupSelection();
        // pop control
        ArrayList<VisualNode> popNodes = new ArrayList<>();
        for (WaggingData waggingData: wagging) {
            popNodes.addAll(waggingData.popControls);
        }
        dfs.select(popNodes);
        dfs.groupSelection();
    }

    private Rectangle2D getBoundingBox() {
        Rectangle2D bb = null;
        for (WaggingData waggingData: wagging) {
            for (VisualComponent component: waggingData.dataComponents) {
                bb = BoundingBoxHelper.union(bb, component.getBoundingBox());
            }
            for (VisualPushRegister push: waggingData.pushRegisters) {
                bb = BoundingBoxHelper.union(bb, push.getBoundingBox());
            }
            for (VisualPopRegister pop: waggingData.popRegisters) {
                bb = BoundingBoxHelper.union(bb, pop.getBoundingBox());
            }
        }
        return bb;
    }

    private Container getCommonContainer() {
        ArrayList<VisualNode> nodes = new ArrayList<>();
        for (WaggingData waggingData: wagging) {
            nodes.addAll(waggingData.dataComponents);
            nodes.addAll(waggingData.pushRegisters);
            nodes.addAll(waggingData.popRegisters);
        }
        return Hierarchy.getNearestContainer(nodes);
    }

    private void addComponent(VisualComponent component, Container container, Point2D position) {
        component.setPosition(position);
        // postpone adding to the model so no notifications are sent too early
        dfs.getMathModel().add(component.getReferencedComponent());
        if (container == null) {
            container = dfs.getRoot();
        }
        container.add(component);
    }

    private VisualPushRegister createPushRegister(Container container, Point2D position) {
        VisualPushRegister component = new VisualPushRegister(new PushRegister());
        addComponent(component, container, position);
        return component;
    }

    private VisualPopRegister createPopRegister(Container container, Point2D position) {
        VisualPopRegister component = new VisualPopRegister(new PopRegister());
        addComponent(component, container, position);
        return component;
    }

    private VisualControlRegister createControlRegister(Container container, Point2D position,
            Marking marking, SynchronisationType syncType) {
        VisualControlRegister component = new VisualControlRegister(new ControlRegister());
        component.getReferencedComponent().setSynchronisationType(syncType);
        component.getReferencedComponent().setMarking(marking);
        addComponent(component, container, position);
        return component;
    }

    private VisualConnection createConnection(VisualComponent first, VisualComponent second) {
        MathNode firstRef = first.getReferencedComponent();
        MathNode secondRef = second.getReferencedComponent();
        try {
            MathConnection connectionRef = dfs.getMathModel().connect(firstRef, secondRef);
            VisualConnection connection = new VisualConnection(connectionRef, first, second);
            Hierarchy.getNearestContainer(first, second).add(connection);
            return connection;
        } catch (InvalidConnectionException e) {
            throw new RuntimeException();
        }
    }

    private VisualControlConnection createControlConnection(VisualComponent first, VisualComponent second, boolean inversing) {
        MathNode firstRef = first.getReferencedComponent();
        MathNode secondRef = second.getReferencedComponent();
        try {
            ControlConnection connectionRef = dfs.getMathModel().controlConnect(firstRef, secondRef);
            connectionRef.setInverting(inversing);
            VisualControlConnection connection = new VisualControlConnection(connectionRef, first, second);
            connection.setBubble(inversing);
            connection.setScaleMode(ScaleMode.ADAPTIVE);
            Hierarchy.getNearestContainer(first, second).add(connection);
            return connection;
        } catch (InvalidConnectionException e) {
            throw new RuntimeException();
        }
    }

    private void convertConnectionToPolyline(VisualConnection connection, double x1Offset, double y1Offset, double x2Offset, double y2Offset) {
        connection.setConnectionType(ConnectionType.POLYLINE);
        Polyline p = (Polyline) connection.getGraphic();
        ControlPoint cp1 = new ControlPoint();
        VisualTransformableNode firstNode = (VisualTransformableNode) connection.getFirst();
        cp1.setPosition(new Point2D.Double(firstNode.getX() + x1Offset, firstNode.getY() + y1Offset));
        p.add(cp1);
        ControlPoint cp2 = new ControlPoint();
        VisualTransformableNode secondNode = (VisualTransformableNode) connection.getSecond();
        cp2.setPosition(new Point2D.Double(secondNode.getX() + x2Offset, secondNode.getY() + y2Offset));
        p.add(cp2);
    }

}
