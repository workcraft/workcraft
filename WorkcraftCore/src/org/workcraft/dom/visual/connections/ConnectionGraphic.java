package org.workcraft.dom.visual.connections;

import java.awt.geom.Rectangle2D;
import java.util.List;

import org.workcraft.dom.Node;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.Drawable;
import org.workcraft.dom.visual.Touchable;

public interface ConnectionGraphic extends Node, Drawable, Touchable, ParametricCurve {
    void draw(DrawRequest r);
    Rectangle2D getBoundingBox();
    PartialCurveInfo getCurveInfo();

    void componentsTransformChanging();
    void componentsTransformChanged();

    void controlPointsChanged();
    void invalidate();

    void setDefaultControlPoints();
    List<ControlPoint> getControlPoints();
}
