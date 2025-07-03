package org.workcraft.dom.visual;

import org.workcraft.dom.Container;
import org.workcraft.dom.Model;
import org.workcraft.dom.math.AbstractMathModel;
import org.workcraft.dom.math.MathGroup;
import org.workcraft.dom.math.MathNode;

import java.util.Collection;

public class MockMathModel extends AbstractMathModel {

    public MockMathModel() {
        super(new MathGroup());
    }

    @Override
    public boolean reparent(Container targetContainer, Model<?, ?> sourceModel,
            Container sourceRoot, Collection<? extends MathNode> sourceChildren) {

        return true;
    }

    @Override
    public <T extends MathNode> T createNode(String name, Container container, Class<T> type) {
        return null;
    }

    @Override
    public <T extends MathNode> T createMergedNode(Collection<MathNode> srcNodes, Container container, Class<T> type) {
        return null;
    }

    @Override
    public <T extends MathNode> T createNodeWithHierarchy(String ref, Container container, Class<T> type) {
        return null;
    }

}
