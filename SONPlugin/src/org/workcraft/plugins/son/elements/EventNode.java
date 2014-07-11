package org.workcraft.plugins.son.elements;

import java.awt.Color;

import org.workcraft.dom.Node;

public interface EventNode extends Node{

	public boolean isFaulty();

	public String getLabel();

	public void setFillColor (Color fillColor);

	public void setForegroundColor(Color foregroundColor);
}
