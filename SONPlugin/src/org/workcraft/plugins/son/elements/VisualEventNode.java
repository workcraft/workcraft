package org.workcraft.plugins.son.elements;

import java.awt.Color;

import org.workcraft.dom.visual.DrawRequest;


public interface VisualEventNode {

	public void setFillColor(Color color);

	public void setForegroundColor(Color foregroundColor);

	public EventNode getMathEventNode();

	public void drawFault(DrawRequest r);
}
