package org.workcraft.dom.visual;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.connections.VisualConnection;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import static org.workcraft.dom.visual.Tools.*;

class VisualModelTests {

    @Test
    void testGroupWithEmptySelection() {
        VisualModel model = new MockVisualModel();

        model.getCurrentLevel().add(new VisualGroup());

        Node[] old = model.getCurrentLevel().getChildren().toArray(new Node[0]);
        Assertions.assertEquals(1, old.length);
        model.groupSelection();
        Node[] newNode = model.getCurrentLevel().getChildren().toArray(new Node[0]);
        Assertions.assertEquals(1, newNode.length);
        Assertions.assertEquals(old[0], newNode[0]);
    }

    private Node[] findMissing(Node[] oldNodes, Node[] newNodes) {
        Node[] diffs = new VisualNode[oldNodes.length
                - (newNodes.length - 1)];

        int dc = 0;
        for (int oc = 0; oc < oldNodes.length; oc++) {
            if (oldNodes[oc] == newNodes[oc - dc]) {
                continue;
            }
            diffs[dc] = oldNodes[oc];
            dc++;
        }
        if (dc != diffs.length) {
            throw new RuntimeException("incorrect arrays!");
        }

        return diffs;
    }

    private void checkGroup(VisualModel model, VisualNode[] toGroup) {
        model.selectNone();
        for (VisualNode node : toGroup) {
            model.addToSelection(node);
        }

        Node[] old = model.getCurrentLevel().getChildren().toArray(new Node[0]);

        model.groupSelection();

        Node[] newNode = model.getCurrentLevel().getChildren().toArray(new Node[0]);

        Node[] diff = findMissing(old, newNode);

        ArrayList<Node> missingList = new ArrayList<>(Arrays.asList(diff));

        Assertions.assertEquals(toGroup.length, missingList.size());
        for (VisualNode node : toGroup) {
            Assertions.assertTrue(missingList.contains(node));
        }

        ArrayList<Node> oldList = new ArrayList<>(Arrays.asList(old));

        VisualGroup newGroup = (VisualGroup) newNode[newNode.length - 1];
        Assertions.assertFalse(oldList.contains(newGroup));
        Assertions.assertNotNull(newGroup);

        ArrayList<Node> newNodeList = new ArrayList<>(newGroup.getChildren());

        Assertions.assertEquals(toGroup.length, newNodeList.size());
        for (VisualNode node : toGroup) {
            Assertions.assertTrue(newNodeList.contains(node));
        }
    }

    @Test
    void testGroup2Items() {
        VisualModel model = new MockVisualModel();

        Container root = model.getCurrentLevel();
        VisualGroup node1 = createGroup(root);
        VisualGroup node2 = createGroup(root);

        checkGroup(model, new VisualNode[] {node1, node2 });
    }

    @Test
    void testGroup1Item() {
        VisualModel model = createModel();

        Container root = model.getCurrentLevel();
        VisualGroup node1 = createGroup(root);

        model.addToSelection(node1);
        model.groupSelection();
        Assertions.assertEquals(1, root.getChildren().toArray(new Node[0]).length);
        Node group = root.getChildren().toArray(new Node[0])[0];
        Assertions.assertEquals(node1, group.getChildren().toArray(new Node[0])[0]);
        Assertions.assertEquals(0, node1.getChildren().toArray(new Node[0]).length);
    }

    @Test
    void testGroup5Items() {
        VisualModel model = createModel();

        Container root = model.getCurrentLevel();
        VisualGroup node1 = new VisualGroup();
        VisualGroup node2 = new VisualGroup();
        VisualGroup node3 = new VisualGroup();
        VisualGroup node4 = new VisualGroup();
        SquareNode sq1 = new SquareNode(new Rectangle2D.Double(0, 0, 1, 1));
        SquareNode sq2 = new SquareNode(new Rectangle2D.Double(0, 0, 1, 1));
        SquareNode sq3 = new SquareNode(new Rectangle2D.Double(0, 0, 1, 1));
        SquareNode sq4 = new SquareNode(new Rectangle2D.Double(0, 0, 1, 1));
        SquareNode sq5 = new SquareNode(new Rectangle2D.Double(0, 0, 1, 1));

        root.add(node1);
        root.add(node2);
        root.add(node3);
        root.add(node4);
        root.add(sq1);
        root.add(sq2);
        root.add(sq3);
        root.add(sq4);
        root.add(sq5);

        checkGroup(model, new VisualNode[] {node1, node3, sq1, sq5 });
    }

    @Test
    void testUngroupRoot() {
        VisualModel model = new MockVisualModel();

        Container root = model.getCurrentLevel();

        VisualGroup node1 = new VisualGroup();
        VisualGroup node2 = new VisualGroup();
        VisualGroup node3 = new VisualGroup();
        VisualGroup node4 = new VisualGroup();
        VisualGroup node5 = new VisualGroup();

        node2.add(node3);
        node1.add(node2);
        node1.add(node4);
        root.add(node1);
        root.add(node5);

        model.addToSelection(node1);
        model.ungroupSelection();

        Node[] newList = root.getChildren().toArray(new Node[0]);

        Assertions.assertEquals(3, newList.length);
        Assertions.assertSame(node5, newList[0]);
        Assertions.assertSame(node2, newList[1]);
        Assertions.assertSame(node4, newList[2]);

        Node[] n2Children = node2.getChildren().toArray(new Node[0]);
        Assertions.assertEquals(1, n2Children.length);
        Assertions.assertSame(node3, n2Children[0]);
    }

    @Test
    void testUngroupNonRoot() {
        VisualModel model = new MockVisualModel();

        Container root = model.getCurrentLevel();

        VisualGroup node1 = new VisualGroup();
        VisualGroup node2 = new VisualGroup();
        VisualGroup node3 = new VisualGroup();
        VisualGroup node4 = new VisualGroup();
        VisualGroup node5 = new VisualGroup();

        node2.add(node3);
        node1.add(node2);
        node1.add(node4);
        root.add(node1);
        root.add(node5);

        model.setCurrentLevel(node1);

        model.addToSelection(node2);
        model.ungroupSelection();

        Node[] newList = root.getChildren().toArray(new Node[0]);

        Assertions.assertEquals(2, newList.length);
        Assertions.assertSame(node1, newList[0]);
        Assertions.assertSame(node5, newList[1]);

        Node[] n1Children = node1.getChildren().toArray(new Node[0]);
        Assertions.assertEquals(2, n1Children.length);
        Assertions.assertSame(node4, n1Children[0]);
        Assertions.assertSame(node3, n1Children[1]);
    }

    @Test
    void testUngroupEmpty() {
        VisualModel model = new MockVisualModel();

        Container root = model.getCurrentLevel();

        VisualGroup node1 = new VisualGroup();
        VisualGroup node2 = new VisualGroup();

        root.add(node1);
        root.add(node2);

        model.addToSelection(node2);
        model.ungroupSelection();

        Node[] newList = root.getChildren().toArray(new Node[0]);

        Assertions.assertEquals(1, newList.length);
        Assertions.assertSame(node1, newList[0]);

        Assertions.assertEquals(0, node1.getChildren().toArray(new Node[0]).length);
    }

    @Test
    void testUngroupTwoGroups() {
        VisualModel model = new MockVisualModel();

        Container root = model.getCurrentLevel();

        VisualGroup node1 = new VisualGroup();
        VisualGroup node2 = new VisualGroup();
        VisualGroup node3 = new VisualGroup();
        VisualGroup node4 = new VisualGroup();

        VisualGroup node1c = new VisualGroup();
        VisualGroup node2c = new VisualGroup();
        VisualGroup node3c = new VisualGroup();
        VisualGroup node4c = new VisualGroup();

        root.add(node1);
        root.add(node2);
        root.add(node3);
        root.add(node4);

        node1.add(node1c);
        node2.add(node2c);
        node3.add(node3c);
        node4.add(node4c);

        model.addToSelection(node1);
        model.addToSelection(node2);
        model.addToSelection(node3);
        model.addToSelection(node4);
        model.ungroupSelection();

        Collection<Node> newList = root.getChildren();

        Assertions.assertEquals(4, newList.size());

        Assertions.assertEquals(0, node1.getChildren().size());
        Assertions.assertEquals(0, node2.getChildren().size());
        Assertions.assertEquals(0, node3.getChildren().size());
        Assertions.assertEquals(0, node4.getChildren().size());

        Assertions.assertTrue(newList.contains(node1c));
        Assertions.assertTrue(newList.contains(node2c));
        Assertions.assertTrue(newList.contains(node3c));
        Assertions.assertTrue(newList.contains(node4c));
    }

    @Test
    void testGroupingAutoGroupConnections() {
        VisualModel model = createModel();

        VisualGroup root = (VisualGroup) model.getRoot();
        VisualComponent c1 = createComponent(root);
        VisualComponent c2 = createComponent(root);
        VisualConnection con = createConnection(c1, c2, root);

        model.addToSelection(c1);
        model.addToSelection(c2);
        VisualGroup g = model.groupSelection();

        Assertions.assertEquals(1, root.getChildren().size());
        Assertions.assertTrue(root.getChildren().contains(g));

        Assertions.assertEquals(3, g.getChildren().size());
        Assertions.assertTrue(g.getChildren().contains(c1));
        Assertions.assertTrue(g.getChildren().contains(c2));
        Assertions.assertTrue(g.getChildren().contains(con));
    }

    @Test
    void testGroupingAutoGroupConnectionsIgnoreSelection() {
        VisualModel model = createModel();

        VisualGroup root = (VisualGroup) model.getRoot();
        VisualComponent c1 = createComponent(root);
        VisualComponent c2 = createComponent(root);
        VisualConnection con = createConnection(c1, c2, root);

        model.addToSelection(con);
        model.addToSelection(c1);
        model.addToSelection(c2);
        VisualGroup g = model.groupSelection();

        Assertions.assertEquals(1, root.getChildren().size());
        Assertions.assertTrue(root.getChildren().contains(g));

        Assertions.assertEquals(3, g.getChildren().size());
        Assertions.assertTrue(g.getChildren().contains(c1));
        Assertions.assertTrue(g.getChildren().contains(c2));
        Assertions.assertTrue(g.getChildren().contains(con));
    }
    @Test
    void testGroupingAutoGroupTwoConnections() {
        VisualModel model = createModel();

        VisualGroup root = (VisualGroup) model.getRoot();
        VisualComponent c1 = createComponent(root);
        VisualComponent c2 = createComponent(root);
        VisualConnection con1 = createConnection(c1, c2, root);
        VisualConnection con2 = createConnection(c1, c2, root);

        model.addToSelection(con1);
        model.addToSelection(c2);
        model.addToSelection(c1);
        model.addToSelection(con2);
        VisualGroup g = model.groupSelection();

        Assertions.assertEquals(1, root.getChildren().size());
        Assertions.assertTrue(root.getChildren().contains(g));

        Assertions.assertEquals(4, g.getChildren().size());
        Assertions.assertTrue(g.getChildren().contains(c1));
        Assertions.assertTrue(g.getChildren().contains(c2));
        Assertions.assertTrue(g.getChildren().contains(con1));
        Assertions.assertTrue(g.getChildren().contains(con2));
    }

    @Test
    void testGroupingAutoGroupConnectionsPointingDeep() {
        VisualModel model = createModel();

        VisualGroup root = (VisualGroup) model.getRoot();
        VisualGroup node1 = createGroup(root);
        VisualComponent c1 = createComponent(node1);
        VisualComponent c2 = createComponent(root);
        VisualConnection con = createConnection(c1, c2, root);

        model.addToSelection(node1);
        model.addToSelection(c2);
        VisualGroup g = model.groupSelection();

        Assertions.assertEquals(1, root.getChildren().size());
        Assertions.assertTrue(root.getChildren().contains(g));

        Assertions.assertEquals(3, g.getChildren().size());
        Assertions.assertTrue(g.getChildren().contains(node1));
        Assertions.assertTrue(g.getChildren().contains(c2));
        Assertions.assertTrue(g.getChildren().contains(con));
    }

    @Test
    void testGroupingDontGroupConnectionsSimple() {
        VisualModel model = createModel();

        VisualGroup root = (VisualGroup) model.getRoot();
        VisualComponent c1 = createComponent(root);
        VisualComponent c2 = createComponent(root);
        VisualComponent c3 = createComponent(root);
        VisualConnection con1 = createConnection(c1, c2, root);
        VisualConnection con2 = createConnection(c2, c3, root);

        model.addToSelection(con1);
        model.addToSelection(con2);
        model.groupSelection();

        Assertions.assertArrayEquals(new VisualNode[] {c1, c2, c3, con1, con2 },
                root.getChildren().toArray(new Node[0]));
    }

    @Test
    void testGroupingDontGroupConnections() {
        VisualModel model = createModel();

        VisualGroup root = (VisualGroup) model.getRoot();
        VisualComponent c1 = createComponent(root);
        VisualComponent c2 = createComponent(root);
        VisualComponent c3 = createComponent(root);
        VisualConnection con1 = createConnection(c1, c2, root);

        model.addToSelection(con1);
        model.addToSelection(c2);
        model.addToSelection(c3);
        VisualGroup g = model.groupSelection();

        Assertions.assertEquals(3, root.getChildren().size());
        Assertions.assertTrue(root.getChildren().contains(g));
        Assertions.assertTrue(root.getChildren().contains(c1));
        Assertions.assertTrue(root.getChildren().contains(con1));

        Assertions.assertEquals(2, g.getChildren().size());
        Assertions.assertTrue(g.getChildren().contains(c2));
        Assertions.assertTrue(g.getChildren().contains(c3));
    }

    @Test
    void testGroupingDontCountConnections() {
        VisualModel model = createModel();

        VisualGroup root = (VisualGroup) model.getRoot();
        VisualComponent c1 = createComponent(root);
        VisualComponent c2 = createComponent(root);
        VisualConnection con = createConnection(c1, c2, root);

        model.addToSelection(c1);
        model.addToSelection(con);
        VisualGroup g = model.groupSelection();

        Assertions.assertEquals(3, root.getChildren().size());
        Assertions.assertFalse(root.getChildren().contains(c1));
        Assertions.assertTrue(root.getChildren().contains(g));
        Assertions.assertTrue(root.getChildren().contains(c2));
        Assertions.assertTrue(root.getChildren().contains(con));
    }

    private VisualModel createModel() {
        return new MockVisualModel();
    }

    @Test
    void testHitNodeCurrentLevelTransformation() {
        VisualModel model = createModel();

        VisualGroup root = (VisualGroup) model.getRoot();
        VisualGroup group1 = createGroup(root);
        group1.setX(101);
        SquareNode sq = new SquareNode(new Rectangle2D.Double(0, 0, 1, 1));
        group1.add(sq);

        Assertions.assertNull(HitMan.hitFirstInCurrentLevel(new Point2D.Double(0.5, 0.5), model));
        Assertions.assertEquals(group1, HitMan.hitFirstInCurrentLevel(new Point2D.Double(101.5, 0.5), model));
        model.setCurrentLevel(group1);
        Assertions.assertNull(HitMan.hitFirstInCurrentLevel(new Point2D.Double(0.5, 0.5), model));
        Assertions.assertEquals(sq, HitMan.hitFirstInCurrentLevel(new Point2D.Double(101.5, 0.5), model));
    }

    @Test
    void testHitObjectsCurrentLevelTransformation() {
        VisualModel model = createModel();

        VisualGroup root = (VisualGroup) model.getRoot();
        VisualGroup group1 = createGroup(root);
        group1.setX(101);
        // Note that VisualGroup nodes have margin of 0.1 on each side.
        SquareNode sq = new SquareNode(new Rectangle2D.Double(0.1, 0.1, 0.8, 0.8));
        group1.add(sq);

        SquareNode sq2 = new SquareNode(new Rectangle2D.Double(0, 5, 1, 1));
        root.add(sq2);

        Assertions.assertEquals(0, boxHitTest(model, new Rectangle2D.Double(-0.01, -0.01, 1.02, 1.02)).size());
        Assertions.assertEquals(1, boxHitTest(model, new Rectangle2D.Double(100.99, -0.01, 1.02, 1.02)).size());
        Assertions.assertEquals(group1, boxHitTest(model, new Rectangle2D.Double(100.99, -0.01, 1.02, 1.02)).iterator().next());
        Assertions.assertEquals(1, boxHitTest(model, new Rectangle2D.Double(-0.01, 4.99, 1.02, 1.02)).size());
        Assertions.assertEquals(sq2, boxHitTest(model, new Rectangle2D.Double(-0.01, 4.99, 1.02, 1.02)).iterator().next());
        model.setCurrentLevel(group1);
        Assertions.assertEquals(0, boxHitTest(model, new Rectangle2D.Double(-0.01, -0.01, 1.02, 1.02)).size());
        Assertions.assertEquals(1, boxHitTest(model, new Rectangle2D.Double(100.99, -0.01, 1.02, 1.02)).size());
        Assertions.assertEquals(sq, boxHitTest(model, new Rectangle2D.Double(100.99, -0.01, 1.02, 1.02)).iterator().next());
        Assertions.assertEquals(0, boxHitTest(model, new Rectangle2D.Double(-0.01, 4.99, 1.02, 1.02)).size());
    }

    private Collection<VisualNode> boxHitTest(VisualModel model, Rectangle2D.Double rect) {
        Point2D.Double p1 = new Point2D.Double(rect.getMinX(), rect.getMinY());
        Point2D.Double p2 = new Point2D.Double(rect.getMaxX(), rect.getMaxY());
        return model.hitBox(p1, p2);
    }

}
