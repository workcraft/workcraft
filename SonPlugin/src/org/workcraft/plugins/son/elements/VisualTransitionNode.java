package org.workcraft.plugins.son.elements;

import java.awt.Color;

import org.workcraft.dom.visual.DrawRequest;

public interface VisualTransitionNode {

    void setFillColor(Color color);

    void setForegroundColor(Color foregroundColor);

    TransitionNode getMathTransitionNode();

    void drawFault(DrawRequest r);

    boolean isFaulty();

}
