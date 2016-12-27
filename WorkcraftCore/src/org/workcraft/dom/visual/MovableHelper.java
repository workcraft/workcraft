package org.workcraft.dom.visual;

import java.awt.geom.AffineTransform;

import org.workcraft.util.Geometry;

public class MovableHelper {

    public static void translate(Movable m, double dx, double dy) {
        m.applyTransform(AffineTransform.getTranslateInstance(dx, dy));
    }

    public static void resetTransform(Movable m) {
        m.applyTransform(Geometry.optimisticInverse(m.getTransform()));
    }

}
