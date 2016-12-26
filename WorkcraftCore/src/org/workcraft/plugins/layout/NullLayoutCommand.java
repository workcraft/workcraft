package org.workcraft.plugins.layout;

import java.awt.geom.Point2D;

import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualTransformableNode;
import org.workcraft.gui.graph.commands.AbstractLayoutCommand;

public class NullLayoutCommand extends AbstractLayoutCommand {

    @Override
    public String getDisplayName() {
        return "Reset";
    }

    @Override
    public void layout(VisualModel model) {
        setChildrenNullPosition(model.getRoot());
    }

    private void setChildrenNullPosition(Container container) {
        Point2D.Double pos = new Point2D.Double(0.0, 0.0);
        for (Node node : container.getChildren()) {
            if (node instanceof VisualTransformableNode) {
                VisualTransformableNode transformableNode = (VisualTransformableNode) node;
                transformableNode.setRootSpacePosition(pos);
            }
            if (node instanceof Container) {
                setChildrenNullPosition((Container) node);
            }
        }
    }

}