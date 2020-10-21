package org.workcraft.dom.visual;

import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.visual.connections.VisualConnection;

public class MockVisualModel extends AbstractVisualModel {

    public MockVisualModel() {
        super(new MockMathModel());
    }

    @Override
    public void validateConnection(VisualNode first, VisualNode second) {
    }

    @Override
    public VisualConnection connect(VisualNode first, VisualNode second, MathConnection mConnection) {
        return null;
    }

}
