package org.workcraft.gui.graph.tools;

import org.workcraft.dom.math.PageNode;

public class PageGeneratorTool extends NodeGeneratorTool {

    public PageGeneratorTool(NodeGenerator generator) {
        super(new DefaultNodeGenerator(PageNode.class) {
        });
    }

}
