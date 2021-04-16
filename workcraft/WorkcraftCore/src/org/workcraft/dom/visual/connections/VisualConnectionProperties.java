package org.workcraft.dom.visual.connections;

import java.awt.Color;
import java.awt.Stroke;
import java.awt.geom.Point2D;

import org.workcraft.dom.visual.Touchable;
import org.workcraft.dom.visual.connections.VisualConnection.ScaleMode;

public interface VisualConnectionProperties {
    Stroke getStroke();
    Color getDrawColor();

    double getArrowWidth();
    double getArrowLength();
    boolean hasArrow();

    double getBubbleSize();
    boolean hasBubble();

    Point2D getFirstCenter();
    Touchable getFirstShape();

    Point2D getSecondCenter();
    Touchable getSecondShape();
    ScaleMode getScaleMode();

    boolean isTokenColorPropagator();
    boolean isSelfLoop();
}
