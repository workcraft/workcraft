package org.workcraft.gui.graph.tools;

import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.math.PageNode;
import org.workcraft.exceptions.NodeCreationException;

public class PageGeneratorTool extends NodeGeneratorTool {

    public PageGeneratorTool(NodeGenerator generator) {
        super(new DefaultNodeGenerator(PageNode.class) {
            @Override
            public MathNode createMathNode() throws NodeCreationException {
                MathNode node = super.createMathNode();
                return node;
            }
        });
    }


}
