package org.workcraft.dom.converters;

import org.workcraft.dom.Container;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.visual.*;
import org.workcraft.dom.visual.connections.VisualConnection;

import java.awt.geom.Point2D;
import java.util.Map;

public interface ModelConverter<S extends VisualModel, T extends VisualModel> {

    S getSrcModel();
    T getDstModel();
    Map<Class<? extends MathNode>, Class<? extends MathNode>> getComponentClassMap();
    Map<Class<? extends VisualReplica>, Class<? extends VisualReplica>> getReplicaClassMap();

    String convertNodeName(String srcName, Container container);
    Container getRefToDstPage(String ref);
    VisualNode getSrcToDstNode(VisualNode srcNode);

    void preprocessing();
    void postprocessing();

    String convertTitle(String title);
    VisualPage convertPage(VisualPage srcPage);
    VisualComponent convertComponent(VisualComponent srcComponent);
    VisualReplica convertReplica(VisualReplica srcReplica);
    VisualGroup convertGroup(VisualGroup srcGroup);
    VisualConnection convertConnection(VisualConnection srcConnection);

    void positionNode(VisualTransformableNode srcNode, VisualTransformableNode dstNode);
    void shapeConnection(VisualConnection srcConnection, VisualConnection dstConnection);
    void copyStyle(Stylable srcStylable, Stylable dstStylable);

    default Point2D getScale() {
        return new Point2D.Double(1.0, 1.0);
    }

    default Point2D scalePosition(Point2D p) {
        Point2D scale = getScale();
        return new Point2D.Double(p.getX() * scale.getX(), p.getY() * scale.getY());
    }

}
