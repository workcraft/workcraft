package org.workcraft.dom.visual.connections;

import static org.workcraft.utils.Geometry.add;
import static org.workcraft.utils.Geometry.changeBasis;
import static org.workcraft.utils.Geometry.multiply;
import static org.workcraft.utils.Geometry.normalize;
import static org.workcraft.utils.Geometry.reduce;
import static org.workcraft.utils.Geometry.rotate90CCW;
import static org.workcraft.utils.Geometry.subtract;

import java.awt.geom.Point2D;
import java.util.Collection;

public class ControlPointScaler {
    private static final double THRESHOLD = 0.00001;
    private final Point2D oldC1, oldC2;

    public ControlPointScaler(Point2D oldC1, Point2D oldC2) {
        this.oldC1 = oldC1;
        this.oldC2 = oldC2;
    }

    public void scale(Point2D newC1, Point2D newC2, Collection<ControlPoint> controlPoints, VisualConnection.ScaleMode mode) {
        if (mode == VisualConnection.ScaleMode.NONE) {
            return;
        }

        if (mode == VisualConnection.ScaleMode.LOCK_RELATIVELY) {
            Point2D dC1 = subtract(newC1, oldC1);
            Point2D dC2 = subtract(newC2, oldC2);

            int n = controlPoints.size();
            int i = 0;
            for (ControlPoint cp : controlPoints) {
                Point2D delta;
                if (i < n / 2) {
                    delta = dC1;
                } else {
                    if (i > (n - 1) / 2) {
                        delta = dC2;
                    } else {
                        delta = multiply(add(dC1, dC2), 0.5);
                    }
                }
                cp.setPosition(add(cp.getPosition(), delta));
                i++;
            }
            return;
        }

        Point2D v0 = subtract(oldC2, oldC1);
        if (v0.distanceSq(0, 0) < THRESHOLD) {
            v0.setLocation(0.001, 0);
        }
        Point2D up0 = getUpVector(mode, v0);
        Point2D v = subtract(newC2, newC1);
        if (v.distanceSq(0, 0) < THRESHOLD) {
            v.setLocation(0.001, 0);
        }
        Point2D up = getUpVector(mode, v);
        for (ControlPoint cp : controlPoints) {
            Point2D p = subtract(cp.getPosition(), oldC1);
            Point2D dp = changeBasis(p, v0, up0);
            Point2D mP1 = multiply(v, dp.getX());
            Point2D mP2 = multiply(up, dp.getY());
            cp.setPosition(add(add(mP1, mP2), newC1));
        }
    }

    private Point2D getUpVector(VisualConnection.ScaleMode mode, Point2D v0) {
        switch (mode) {
        case SCALE:
            return rotate90CCW(v0);
        case STRETCH:
            return normalize(rotate90CCW(v0));
        case ADAPTIVE:
            return reduce(rotate90CCW(v0));
        default:
            throw new RuntimeException("Unexpected value of scale mode");
        }
    }

}
