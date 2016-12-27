package org.workcraft.dom.visual;

import java.awt.geom.Rectangle2D;

public class TouchableHelper {

    public static boolean insideRectangle(Touchable node, Rectangle2D rect) {
        Rectangle2D boundingBox = node.getBoundingBox();
        return (boundingBox != null) && rect.contains(boundingBox);
    }

    public static boolean touchesRectangle(Touchable node, Rectangle2D rect) {
        Rectangle2D boundingBox = node.getBoundingBox();
        return (boundingBox != null) && rect.intersects(boundingBox);
    }

}
