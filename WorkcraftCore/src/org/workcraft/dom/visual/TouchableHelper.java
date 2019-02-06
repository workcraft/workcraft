package org.workcraft.dom.visual;

import org.workcraft.dom.visual.connections.VisualConnection;

import java.awt.geom.Rectangle2D;

public class TouchableHelper {

    public static boolean insideRectangle(Touchable node, Rectangle2D rect) {
        Rectangle2D boundingBox = node.getBoundingBox();
        return (boundingBox != null) && rect.contains(boundingBox);
    }

    public static boolean touchesRectangle(Touchable node, Rectangle2D rect) {
        Rectangle2D boundingBox = node.getBoundingBox();
        if ((boundingBox != null) && rect.intersects(boundingBox)) {
            if (node instanceof VisualConnection) {
                VisualConnection connection = (VisualConnection) node;
                return rect.contains(boundingBox) || !connection.getIntersections(rect).isEmpty();
            }
            return true;
        }
        return false;
    }

}
