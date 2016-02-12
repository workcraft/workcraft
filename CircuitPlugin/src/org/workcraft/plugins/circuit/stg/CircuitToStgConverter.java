package org.workcraft.plugins.circuit.stg;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.workcraft.dom.Connection;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.hierarchy.NamespaceHelper;
import org.workcraft.dom.visual.Movable;
import org.workcraft.dom.visual.Positioning;
import org.workcraft.dom.visual.TransformHelper;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.plugins.circuit.CircuitSettings;
import org.workcraft.plugins.circuit.CircuitUtils;
import org.workcraft.plugins.circuit.Contact;
import org.workcraft.plugins.circuit.VisualCircuit;
import org.workcraft.plugins.circuit.VisualCircuitComponent;
import org.workcraft.plugins.circuit.VisualCircuitConnection;
import org.workcraft.plugins.circuit.VisualContact;
import org.workcraft.plugins.circuit.VisualFunctionComponent;
import org.workcraft.plugins.circuit.VisualFunctionContact;
import org.workcraft.plugins.cpog.optimisation.BooleanFormula;
import org.workcraft.plugins.cpog.optimisation.BooleanVariable;
import org.workcraft.plugins.cpog.optimisation.Literal;
import org.workcraft.plugins.cpog.optimisation.booleanvisitors.FormulaToString;
import org.workcraft.plugins.cpog.optimisation.dnf.Dnf;
import org.workcraft.plugins.cpog.optimisation.dnf.DnfClause;
import org.workcraft.plugins.cpog.optimisation.dnf.DnfGenerator;
import org.workcraft.plugins.cpog.optimisation.expressions.DumbBooleanWorker;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.stg.STG;
import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.plugins.stg.SignalTransition.Direction;
import org.workcraft.plugins.stg.VisualImplicitPlaceArc;
import org.workcraft.plugins.stg.VisualSTG;
import org.workcraft.plugins.stg.VisualSignalTransition;
import org.workcraft.plugins.stg.generator.SignalStg;
import org.workcraft.util.Geometry;
import org.workcraft.util.Hierarchy;
import org.workcraft.util.Pair;
import org.workcraft.util.TwoWayMap;

public class CircuitToStgConverter {
    public static final String NAME_SUFFIX_0 = "_0";
    public static final String NAME_SUFFIX_1 = "_1";
    public static final String LABEL_SUFFIX_0 = "=0";
    public static final String LABEL_SUFFIX_1 = "=1";

    private static final double SCALE_X = 4.0;
    private static final double SCALE_Y = 4.0;
    private static final Point2D OFFSET_P1 = new Point2D.Double(0.0, -2.0);
    private static final Point2D OFFSET_P0 = new Point2D.Double(0.0, +2.0);
    private static final Point2D OFFSET_INIT_PLUS = new Point2D.Double(0.0, -2.0);
    private static final Point2D OFFSET_INIT_MINUS = new Point2D.Double(0.0, +2.0);
    private static final Point2D OFFSET_INC_PLUS = new Point2D.Double(0.0, -1.0);
    private static final Point2D OFFSET_INC_MINUS = new Point2D.Double(0.0, +1.0);

    private final VisualCircuit circuit;
    private final VisualSTG stg;

    private final HashMap<String, Container> refToPageMap;
    private final Map<VisualNode, Pair<VisualContact, Boolean>> nodeToDriverMap;
    private final TwoWayMap<VisualContact, SignalStg> driverToStgMap;

    public CircuitToStgConverter(VisualCircuit circuit) {
        this.circuit = circuit;
        this.stg = new VisualSTG(new STG());
        this.refToPageMap = convertPages();
        HashSet<VisualContact> drivers = identifyDrivers();
        this.nodeToDriverMap = associateNodesToDrivers(drivers);
        this.driverToStgMap = convertDriversToStgs(drivers);
        connectDriverStgs(drivers);
        if (CircuitSettings.getSimplifyStg()) {
            simplifyDriverStgs(drivers); // remove dead transitions
        }
        positionDriverStgs(drivers);
        //groupDriverStgs(drivers);
    }

    public CircuitToStgConverter(VisualCircuit circuit, VisualSTG stg) {
        this.circuit = circuit;
        this.stg = stg;
        this.refToPageMap = convertPages();
        HashSet<VisualContact> drivers = identifyDrivers();
        this.nodeToDriverMap = associateNodesToDrivers(drivers);
        this.driverToStgMap = associateDriversToStgs(drivers);  // STGs already exist, just associate them with the drivers
        if (CircuitSettings.getSimplifyStg()) {
            simplifyDriverStgs(drivers); // remove dead transitions
        }
        positionDriverStgs(drivers);
        //groupDriverStgs(drivers);
    }

    public VisualSTG getStg() {
        return stg;
    }

    public boolean isDriver(VisualContact contact) {
        return driverToStgMap.containsKey(contact);

    }

    public SignalStg getSignalStg(VisualNode node) {
        SignalStg result = null;
        Pair<VisualContact, Boolean> driverAndInversion = nodeToDriverMap.get(node);
        if (driverAndInversion != null) {
            VisualContact driver = driverAndInversion.getFirst();
            result = driverToStgMap.getValue(driver);
        }
        return result;
    }

    public Pair<SignalStg, Boolean> getSignalStgAndInvertion(VisualNode node) {
        Pair<SignalStg, Boolean> result = null;
        Pair<VisualContact, Boolean> driverAndInvert = nodeToDriverMap.get(node);
        SignalStg signal = null;
        boolean isInverted = false;
        if (driverAndInvert != null) {
            VisualContact driver = driverAndInvert.getFirst();
            signal = driverToStgMap.getValue(driver);
            isInverted = driverAndInvert.getSecond();
        }
        if (signal != null) {
            result = new Pair<>(signal, isInverted);
        }
        return result;
    }

    private HashMap<String, Container> convertPages() {
        NamespaceHelper.copyPageStructure(circuit, stg);
        return NamespaceHelper.getRefToPageMapping(stg);
    }

    private Container getContainer(VisualContact contact) {
        String nodeReference = circuit.getMathModel().getNodeReference(contact.getReferencedComponent());
        String parentReference = NamespaceHelper.getParentReference(nodeReference);
        Container container = (Container) refToPageMap.get(parentReference);
        while (container==null) {
            parentReference = NamespaceHelper.getParentReference(parentReference);
            container = (Container) refToPageMap.get(parentReference);
        }
        return container;
    }

    private HashSet<VisualContact> identifyDrivers() {
        HashSet<VisualContact> result = new HashSet<>();
        for (VisualContact contact : Hierarchy.getDescendantsOfType(circuit.getRoot(), VisualContact.class)) {
            VisualContact driver = CircuitUtils.findDriver(circuit, contact);
            if (driver == null) {
                driver = contact;
            }
            result.add(driver);
        }
        return result;
    }

    private HashMap<VisualNode, Pair<VisualContact, Boolean>> associateNodesToDrivers(HashSet<VisualContact> driverSet) {
        HashMap<VisualNode, Pair<VisualContact, Boolean>> result = new HashMap<>();
        for (VisualContact driver: driverSet) {
            if (!result.containsKey(driver)) {
                result.putAll(propagateDriverInversion(driver, new Pair<>(driver, false)));
            }
        }
        return result;
    }

    private HashMap<VisualNode, Pair<VisualContact, Boolean>> propagateDriverInversion(VisualNode node, Pair<VisualContact, Boolean> driverAndInversion) {
        HashMap<VisualNode, Pair<VisualContact, Boolean>> result = new HashMap<>();
        result.put(node, driverAndInversion);
        // Support for zero-delay buffers and inverters.
        if (node instanceof VisualContact) {
            VisualContact contact = (VisualContact) node;
            Node parent = node.getParent();
            if (contact.isInput() && (parent instanceof VisualCircuitComponent)) {
                VisualFunctionComponent component = (VisualFunctionComponent) parent;
                if (component.getIsZeroDelay() && (component.isBuffer() || component.isInverter())) {
                    VisualContact driver = driverAndInversion.getFirst();
                    boolean isInverting = component.isInverter();
                    driverAndInversion = new Pair<>(driver, isInverting);
                    for (VisualContact c: component.getContacts()) {
                        if (c.isOutput()) {
                            node = c;
                            result.put(node, driverAndInversion);
                        }
                    }
                }
            }
        }
        for (Connection connection: circuit.getConnections(node)) {
            if ((connection.getFirst() == node) && (connection instanceof VisualCircuitConnection)) {
                result.put((VisualCircuitConnection) connection, driverAndInversion);
                Node succNode = connection.getSecond();
                if (!result.containsKey(succNode) && (succNode instanceof VisualNode)) {
                    result.putAll(propagateDriverInversion((VisualNode) succNode, driverAndInversion));
                }
            }
        }
        return result;
    }

    private TwoWayMap<VisualContact, SignalStg> convertDriversToStgs(HashSet<VisualContact> drivers) {
        TwoWayMap<VisualContact, SignalStg> result = new TwoWayMap<>();
        for (VisualContact driver: drivers) {
            VisualContact signal = CircuitUtils.findSignal(circuit, driver, true);
            Container container = getContainer(signal);
            String signalName = CircuitUtils.getSignalName(circuit, signal);

            VisualPlace zeroPlace = stg.createPlace(signalName + NAME_SUFFIX_0, container);
            zeroPlace.setNamePositioning(Positioning.TOP);
            zeroPlace.setLabelPositioning(Positioning.BOTTOM);
            if (!signal.getReferencedContact().getInitToOne()) {
                zeroPlace.getReferencedPlace().setTokens(1);
            }

            VisualPlace onePlace = stg.createPlace(signalName + NAME_SUFFIX_1, container);
            onePlace.setNamePositioning(Positioning.BOTTOM);
            onePlace.setLabelPositioning(Positioning.TOP);
            if (signal.getReferencedContact().getInitToOne()) {
                onePlace.getReferencedPlace().setTokens(1);
            }

            SignalStg signalStg = new SignalStg(zeroPlace, onePlace);
            result.put(driver, signalStg);
        }
        return result;
    }

    private void connectDriverStgs(HashSet<VisualContact> drivers) {
        for (VisualContact driver: drivers) {
            BooleanFormula setFunc = null;
            BooleanFormula resetFunc = null;
            if (driver instanceof VisualFunctionContact) {
                setFunc = ((VisualFunctionContact) driver).getSetFunction();
                resetFunc = ((VisualFunctionContact) driver).getResetFunction();
            }
            // Create complementary set/reset if only one of them is defined
            if ((setFunc != null) && (resetFunc == null)) {
                resetFunc = new DumbBooleanWorker().not(setFunc);
            } else if ((setFunc == null) && (resetFunc != null)) {
                setFunc = new DumbBooleanWorker().not(resetFunc);
            }
            Dnf setDnf = DnfGenerator.generate(setFunc);
            createSignalStgTransitions(driver, setDnf, Direction.PLUS);

            Dnf resetDnf = DnfGenerator.generate(resetFunc);
            createSignalStgTransitions(driver, resetDnf, Direction.MINUS);
        }
    }

    private void createSignalStgTransitions(VisualContact driver, Dnf dnf, Direction direction) {
        SignalStg driverStg = driverToStgMap.getValue(driver);
        VisualPlace predPlace = direction == Direction.PLUS ? driverStg.zero : driverStg.one;
        VisualPlace succPlace = direction == Direction.PLUS ? driverStg.one : driverStg.zero;
        Collection<VisualSignalTransition> transitions = direction == Direction.PLUS ? driverStg.riseList : driverStg.fallList;

        TreeSet<DnfClause> clauses = new TreeSet<DnfClause>(
                new Comparator<DnfClause>() {
                    @Override
                    public int compare(DnfClause arg0, DnfClause arg1) {
                        String st1 = FormulaToString.toString(arg0);
                        String st2 = FormulaToString.toString(arg1);
                        return st1.compareTo(st2);
                    }
                });

        clauses.addAll(dnf.getClauses());

        VisualContact signal = CircuitUtils.findSignal(circuit, driver, true);
        Container container = getContainer(signal);
        String signalName = CircuitUtils.getSignalName(circuit, signal);
        SignalTransition.Type signalType = CircuitUtils.getSignalType(circuit, signal);
        for(DnfClause clause : clauses) {
            // In self-looped signals the read-arcs will clash with producing/consuming arcs:
            // 1) a read-arc from a preset place is redundant (is superseded by a consuming arc);
            // 2) a read-arc from a postset place makes the transition dead.
            boolean isDeadTransition = false;
            HashSet<VisualPlace> placesToRead = new HashSet<VisualPlace>();
            for (Literal literal : clause.getLiterals()) {
                BooleanVariable variable = literal.getVariable();
                VisualContact sourceContact = circuit.getVisualComponent((Contact) variable, VisualContact.class);
                Pair<VisualContact, Boolean> sourceDriverAndInversion = nodeToDriverMap.get(sourceContact);
                VisualContact sourceDriver = sourceDriverAndInversion.getFirst();
                boolean sourceInversion = sourceDriverAndInversion.getSecond();
                SignalStg sourceDriverStg = driverToStgMap.getValue(sourceDriver);
                if (sourceDriverStg == null) {
                    throw new RuntimeException("No source for '" + circuit.getMathName(sourceContact) + "' while generating '" + signalName + "'.");
                }
                VisualPlace place = (literal.getNegation() != sourceInversion) ? sourceDriverStg.zero : sourceDriverStg.one;
                if (place != predPlace) {
                    // 1) a read-arc from a preset place is redundant (is superseded by a consuming arc);
                    placesToRead.add(place);
                }

                if (place == succPlace) {
                    // 2) a read-arc from a postset place makes the transition dead.
                    isDeadTransition = true;
                }
            }

            if (!isDeadTransition) {
                VisualSignalTransition transition = stg.createSignalTransition(signalName, signalType, direction, container);
                transition.setLabel(FormulaToString.toString(clause));
                transitions.add(transition);
                // Create read-arcs.
                for (VisualPlace place : placesToRead) {
                    try {
                        stg.connectUndirected(place, transition);
                    } catch (InvalidConnectionException e) {
                        throw new RuntimeException(e);
                    }
                }
                // Create producing/consuming arcs.
                try {
                    stg.connect(predPlace, transition);
                    stg.connect(transition, succPlace);
                } catch (InvalidConnectionException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private TwoWayMap<VisualContact, SignalStg> associateDriversToStgs(HashSet<VisualContact> drivers) {
        TwoWayMap<VisualContact, SignalStg> result = new TwoWayMap<>();

        for (VisualContact driver: drivers) {
            String signalName = CircuitUtils.getSignalName(circuit, driver);

            VisualPlace zeroPlace = null;
            VisualPlace onePlace = null;
            String zeroName = signalName + NAME_SUFFIX_0;
            String oneName = signalName + NAME_SUFFIX_1;
            for (VisualPlace place: stg.getVisualPlaces()) {
                if (zeroName.equals(stg.getMathName(place))) {
                    zeroPlace = place;
                }
                if (oneName.equals(stg.getMathName(place))) {
                    onePlace = place;
                }
            }

            VisualSignalTransition plusTransition = null;
            VisualSignalTransition minusTransition = null;
            for (VisualSignalTransition transition: stg.getVisualSignalTransitions()) {
                if (signalName.equals(transition.getSignalName())) {
                    if (transition.getDirection() == Direction.PLUS) {
                        plusTransition = transition;
                    }
                    if (transition.getDirection() == Direction.MINUS) {
                        minusTransition = transition;
                    }
                }
            }
            if (zeroPlace == null) {
                Connection connection = stg.getConnection(minusTransition, plusTransition);
                if (connection instanceof VisualImplicitPlaceArc) {
                    VisualImplicitPlaceArc implicitPlace = (VisualImplicitPlaceArc) connection;
                    zeroPlace = stg.makeExplicit(implicitPlace);
                    stg.setMathName(zeroPlace, zeroName);
                }
            }

            if (onePlace == null) {
                Connection connection = stg.getConnection(plusTransition, minusTransition);
                if (connection instanceof VisualImplicitPlaceArc) {
                    VisualImplicitPlaceArc implicitPlace = (VisualImplicitPlaceArc) connection;
                    onePlace = stg.makeExplicit(implicitPlace);
                    stg.setMathName(onePlace, oneName);
                }
            }

            if ((zeroPlace != null) && (onePlace != null)) {
                SignalStg signalStg = new SignalStg(zeroPlace, onePlace);
                result.put(driver, signalStg);
                for (VisualSignalTransition transition: stg.getVisualSignalTransitions()) {
                    if (signalName.equals(transition.getSignalName())) {
                        if (transition.getDirection() == Direction.PLUS) {
                            signalStg.riseList.add(transition);
                        }
                        if (transition.getDirection() == Direction.MINUS) {
                            signalStg.fallList.add(transition);
                        }
                    }
                }
            }
        }
        return result;
    }

    private void simplifyDriverStgs(HashSet<VisualContact> drivers) {
        HashSet<Node> redundantTransitions = getDeadTransitions(drivers);
        redundantTransitions.addAll(getDuplicateTransitions(drivers));
        for (VisualContact driver: drivers) {
            SignalStg signalStg = driverToStgMap.getValue(driver);
            if (signalStg != null) {
                signalStg.riseList.removeAll(redundantTransitions);
                signalStg.fallList.removeAll(redundantTransitions);
            }
        }
        stg.remove(redundantTransitions);
    }

    private HashSet<Node> getDeadTransitions(HashSet<VisualContact> drivers) {
        HashSet<Node> result = new HashSet<>();
        for (VisualContact driver: drivers) {
            SignalStg signalStg = driverToStgMap.getValue(driver);
            if (signalStg != null) {
                HashSet<Node> deadPostset = new HashSet<Node>(stg.getPostset(signalStg.zero));
                deadPostset.retainAll(stg.getPostset(signalStg.one));
                result.addAll(deadPostset);
            }
        }
        return result;
    }

    private HashSet<Node> getDuplicateTransitions(HashSet<VisualContact> drivers) {
        HashSet<Node> result = new HashSet<>();
        for (VisualContact driver: drivers) {
            SignalStg signalStg = driverToStgMap.getValue(driver);
            if (signalStg != null) {
                result.addAll(getDuplicates(signalStg.riseList));
                result.addAll(getDuplicates(signalStg.fallList));
            }
        }
        return result;
    }

    private HashSet<Node> getDuplicates(Collection<VisualSignalTransition> transitions) {
        HashSet<Node> result = new HashSet<>();
        for (VisualSignalTransition t1: transitions) {
            if (result.contains(t1)) continue;
            for (VisualSignalTransition t2: transitions) {
                if (t1 == t2) continue;
                Set<Node> preset1 = stg.getPreset(t1);
                Set<Node> postset1 = stg.getPostset(t1);
                Set<Node> preset2 = stg.getPreset(t2);
                Set<Node> postset2 = stg.getPostset(t2);
                if (preset1.equals(preset2) && postset1.equals(postset2)) {
                    result.add(t2);
                }
            }
        }
        return result;
    }

    private void positionDriverStgs(HashSet<VisualContact> drivers) {
        for (VisualContact driver: drivers) {
            SignalStg signalStg = driverToStgMap.getValue(driver);
            if (signalStg != null) {
                VisualContact signal = CircuitUtils.findSignal(circuit, driver, true);
                Point2D centerPosition = getPosition(signal);
                setPosition(signalStg.zero, Geometry.add(centerPosition, OFFSET_P0));
                setPosition(signalStg.one, Geometry.add(centerPosition, OFFSET_P1));

                centerPosition = Geometry.add(centerPosition, getDirectionOffset(signal));
                Point2D plusPosition = Geometry.add(centerPosition, OFFSET_INIT_PLUS);
                for (VisualSignalTransition transition: signalStg.riseList) {
                    setPosition(transition, plusPosition);
                    plusPosition = Geometry.add(plusPosition, OFFSET_INC_PLUS);
                }

                Point2D minusPosition = Geometry.add(centerPosition, OFFSET_INIT_MINUS);
                for (VisualSignalTransition transition: signalStg.fallList) {
                    setPosition(transition, minusPosition);
                    minusPosition = Geometry.add(minusPosition, OFFSET_INC_MINUS);
                }
            }
        }
    }

    private Point2D getPosition(VisualContact contact) {
        AffineTransform transform = TransformHelper.getTransformToRoot(contact);
        Point2D position = new Point2D.Double(
                SCALE_X * (transform.getTranslateX() + contact.getX()),
                SCALE_Y * (transform.getTranslateY() + contact.getY()));
        return position;
    }

    private void setPosition(Movable node, Point2D point) {
        AffineTransform t = AffineTransform.getTranslateInstance(point.getX(), point.getY());
        TransformHelper.applyTransform(node, t);
    }

    private Point2D getDirectionOffset(VisualContact contact) {
        VisualContact.Direction direction = contact.getDirection();
        if (contact.isInput()) {
            direction = direction.flip();
        }
        switch (direction) {
        case WEST: return new Point2D.Double(6.0, 0.0);
        case EAST: return new Point2D.Double(-6.0, 0.0);
        case NORTH: return new Point2D.Double(6.0, 0.0);
        case SOUTH: return new Point2D.Double(-6.0, 0.0);
        default: return new Point2D.Double(0.0, 0.0);
        }
    }

    private void groupDriverStgs(HashSet<VisualContact> drivers) {
        for (VisualContact driver: drivers) {
            SignalStg signalStg = driverToStgMap.getValue(driver);
            if (signalStg != null) {
                Collection<Node> nodesToGroup = new LinkedList<Node>();
                nodesToGroup.addAll(signalStg.getAllNodes());

                Container currentLevel = null;
                Container oldLevel = stg.getCurrentLevel();
                for (Node node: nodesToGroup) {
                    if (currentLevel == null) {
                        currentLevel = (Container) node.getParent();
                    }
                    if (currentLevel != node.getParent()) {
                        throw new RuntimeException("Current level is not the same among the processed nodes");
                    }
                }
                stg.setCurrentLevel(currentLevel);
                stg.select(nodesToGroup);
                stg.groupSelection();
                stg.setCurrentLevel(oldLevel);
            }
        }
    }

}
