package org.workcraft.dom.visual.connections;

import org.workcraft.dom.Node;
import org.workcraft.dom.visual.Drawable;
import org.workcraft.dom.visual.Touchable;

import java.util.List;

public interface ConnectionGraphic extends Node, Drawable, Touchable, ParametricCurve {
    PartialCurveInfo getCurveInfo();

    void componentsTransformChanging();
    void componentsTransformChanged();

    void controlPointsChanged();
    void invalidate();

    void setDefaultControlPoints();
    List<ControlPoint> getControlPoints();
}
