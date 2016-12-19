package org.workcraft.gui.graph.tools;

import org.workcraft.dom.math.PageNode;
import org.workcraft.gui.graph.generators.DefaultNodeGenerator;
import org.workcraft.gui.graph.generators.NodeGenerator;

public class PageGeneratorTool extends NodeGeneratorTool {

    public PageGeneratorTool(NodeGenerator generator) {
        super(new DefaultNodeGenerator(PageNode.class) {
        });
    }

}
