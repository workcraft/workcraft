package org.workcraft.plugins.son.elements;

import java.awt.Color;

public interface TransitionNode extends Time{

    boolean isFaulty();

    void setFaulty(boolean fault);

    void setLabel(String label);

    String getLabel();

    void setFillColor(Color fillColor);

    Color getFillColor();

    void setForegroundColor(Color foregroundColor);

    Color getForegroundColor();
}
