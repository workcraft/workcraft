package org.workcraft.gui.tools;

import org.workcraft.dom.math.PageNode;
import org.workcraft.dom.generators.DefaultNodeGenerator;
import org.workcraft.dom.generators.NodeGenerator;

public class PageGeneratorTool extends NodeGeneratorTool {

    public PageGeneratorTool(NodeGenerator generator) {
        super(new DefaultNodeGenerator(PageNode.class) {
        });
    }

}
