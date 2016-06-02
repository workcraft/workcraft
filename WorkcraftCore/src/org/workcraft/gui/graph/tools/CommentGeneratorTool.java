package org.workcraft.gui.graph.tools;

import org.workcraft.dom.math.CommentNode;

public class CommentGeneratorTool extends NodeGeneratorTool {

    public CommentGeneratorTool() {
        super(new DefaultNodeGenerator(CommentNode.class) {
        });
    }

    @Override
    public String getHintMessage() {
        return "Click to create a text label.";
    }
}
