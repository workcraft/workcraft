package org.workcraft.plugins.son.elements;

import java.awt.Color;


public interface TransitionNode extends Time{

	public boolean isFaulty();

	public void setFaulty(boolean fault);

	public void setLabel(String label);

	public String getLabel();

	public void setFillColor (Color fillColor);

	public Color getFillColor();

	public void setForegroundColor(Color foregroundColor);

	public Color getForegroundColor();
}
