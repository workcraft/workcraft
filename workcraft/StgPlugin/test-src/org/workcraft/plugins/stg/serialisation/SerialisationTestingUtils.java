package org.workcraft.plugins.stg.serialisation;

import org.junit.jupiter.api.Assertions;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualGroup;
import org.workcraft.dom.visual.connections.ComponentsTransformObserver;
import org.workcraft.dom.visual.connections.ControlPoint;
import org.workcraft.dom.visual.connections.Polyline;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.plugins.stg.VisualDummyTransition;
import org.workcraft.plugins.stg.VisualImplicitPlaceArc;
import org.workcraft.plugins.stg.VisualSignalTransition;

import java.util.Collection;
import java.util.Iterator;

class SerialisationTestingUtils {

    public static void comparePlaces(Place p1, Place p2) {
        Assertions.assertEquals(p1.getTokens(), p2.getTokens());
        Assertions.assertEquals(p1.getCapacity(), p2.getCapacity());
    }

    public static void compareTransitions(SignalTransition t1, SignalTransition t2) {
        Assertions.assertEquals(t1.getSignalName(), t2.getSignalName());
        Assertions.assertEquals(t1.getDirection(), t2.getDirection());
    }

    public static void compareConnections(MathConnection con1, MathConnection con2) {
        compareNodes(con1.getFirst(), con2.getFirst());
        compareNodes(con1.getSecond(), con2.getSecond());
    }

    public static void compareVisualPlaces(VisualPlace p1, VisualPlace p2) {
        Assertions.assertEquals(p1.getTransform(), p2.getTransform());
        comparePlaces(p1.getReferencedComponent(), p2.getReferencedComponent());
    }

    public static void compareVisualSignalTransitions(VisualSignalTransition t1, VisualSignalTransition t2) {
        Assertions.assertEquals(t1.getTransform(), t2.getTransform());
        compareTransitions(t1.getReferencedComponent(), t2.getReferencedComponent());
    }

    public static void compareVisualDummyTransitions(VisualDummyTransition t1, VisualDummyTransition t2) {
        Assertions.assertEquals(t1.getTransform(), t2.getTransform());
        Assertions.assertEquals(t1.getName(), t2.getName());
    }

    public static void compareVisualConnections(VisualConnection vc1, VisualConnection vc2) {
        compareNodes(vc1.getFirst(), vc2.getFirst());
        compareNodes(vc1.getSecond(), vc2.getSecond());
        compareConnections(vc1.getReferencedConnection(), vc2.getReferencedConnection());
    }

    public static void compareImplicitPlaceArcs(VisualImplicitPlaceArc vc1, VisualImplicitPlaceArc vc2) {
        compareNodes(vc1.getFirst(), vc2.getFirst());
        compareNodes(vc1.getSecond(), vc2.getSecond());
        comparePlaces(vc1.getImplicitPlace(), vc2.getImplicitPlace());
        compareConnections(vc1.getRefCon1(), vc2.getRefCon1());
        compareConnections(vc1.getRefCon2(), vc2.getRefCon2());
    }

    public static void comparePolylines(Polyline p1, Polyline p2) {
        Assertions.assertEquals(p1.getChildren().size(), p2.getChildren().size());

        Iterator<Node> i1 = p1.getChildren().iterator();
        Iterator<Node> i2 = p2.getChildren().iterator();

        for (int i = 0; i < p1.getChildren().size(); i++) {
            ControlPoint cp1 = (ControlPoint) i1.next();
            ControlPoint cp2 = (ControlPoint) i2.next();

            Assertions.assertEquals(cp1.getX(), cp2.getX(), 0.0001);
            Assertions.assertEquals(cp1.getY(), cp2.getY(), 0.0001);
        }
    }

    public static void compareNodes(Node node1, Node node2) {
        Assertions.assertEquals(node1.getClass(), node2.getClass());

        if (node1 instanceof MathNode) {
            if (node1 instanceof Place) {
                comparePlaces((Place) node1, (Place) node2);
            }
            if (node1 instanceof MathConnection) {
                compareConnections((MathConnection) node1, (MathConnection) node2);
            }
            if (node1 instanceof SignalTransition) {
                compareTransitions((SignalTransition) node1, (SignalTransition) node2);
            }
        }

        if (node1 instanceof VisualComponent) {
            if (node1 instanceof VisualPlace) {
                compareVisualPlaces((VisualPlace) node1, (VisualPlace) node2);
            }
            if (node1 instanceof VisualSignalTransition) {
                compareVisualSignalTransitions((VisualSignalTransition) node1, (VisualSignalTransition) node2);
            }
            if (node1 instanceof VisualDummyTransition) {
                compareVisualDummyTransitions((VisualDummyTransition) node1, (VisualDummyTransition) node2);
            }
        }

        if (node1 instanceof VisualConnection) {
            if (node1 instanceof VisualImplicitPlaceArc) {
                compareImplicitPlaceArcs((VisualImplicitPlaceArc) node1, (VisualImplicitPlaceArc) node2);
            } else {
                compareVisualConnections((VisualConnection) node1, (VisualConnection) node2);
            }
        }
        if (node1 instanceof Polyline) {
            comparePolylines((Polyline) node1, (Polyline) node2);
        }

        if (!(node1 instanceof MathNode)
                && !(node1 instanceof VisualComponent)
                && !(node1 instanceof VisualConnection)
                && !(node1 instanceof VisualGroup)
                && !(node1 instanceof Polyline)
                && !(node1 instanceof ComponentsTransformObserver)) {

            Assertions.fail("Unexpected class " + node1.getClass().getName());
        }

        Collection<Node> ch1 = node1.getChildren();
        Collection<Node> ch2 = node2.getChildren();

        Assertions.assertEquals(ch1.size(), ch2.size());

        Iterator<Node> i1 = ch1.iterator();
        Iterator<Node> i2 = ch2.iterator();

        while (i1.hasNext()) {
            Node n1 = i1.next();
            Node n2 = i2.next();

            compareNodes(n1, n2);
        }
    }

}
