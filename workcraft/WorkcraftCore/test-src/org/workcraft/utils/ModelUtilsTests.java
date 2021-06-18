package org.workcraft.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.math.MathModel;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.visual.MockMathModel;
import org.workcraft.exceptions.InvalidConnectionException;

class ModelUtilsTests {

    static class MockNode extends MathNode {
    }

    @Test
    void testGroupWithEmptySelection() throws InvalidConnectionException {
        MathModel model = new MockMathModel();

        MathNode n1 = new MockNode();
        MathNode n2 = new MockNode();
        MathNode n3 = new MockNode();
        MathNode n4 = new MockNode();
        MathNode n5 = new MockNode();

        model.add(n1);
        model.add(n2);
        model.add(n3);
        model.add(n4);
        model.add(n5);

        model.connect(n1, n2);
        MathConnection c23 = model.connect(n2, n3);
        model.connect(n3, n4);
        model.connect(n4, n5);
        model.connect(n4, n1);
        MathConnection c14 = model.connect(n1, n4);

        Assertions.assertFalse(ModelUtils.hasPath(model, n5, n1));
        Assertions.assertTrue(ModelUtils.hasPath(model, n2, n3));
        Assertions.assertTrue(ModelUtils.hasPath(model, n1, n4));

        Assertions.assertFalse(ModelUtils.isTransitive(model, c23));
        Assertions.assertTrue(ModelUtils.isTransitive(model, c14));
    }

}
