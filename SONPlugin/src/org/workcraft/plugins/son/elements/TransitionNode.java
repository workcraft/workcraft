package org.workcraft.plugins.son.elements;

import java.awt.Color;

import org.workcraft.dom.Node;

public interface TransitionNode extends Node{

	public boolean isFaulty();

	public void setFaulty(boolean fault);

	public void setLabel(String label);

	public String getLabel();

	public void setFillColor (Color fillColor);

	public Color getFillColor();

	public void setForegroundColor(Color foregroundColor);

	public Color getForegroundColor();
}
