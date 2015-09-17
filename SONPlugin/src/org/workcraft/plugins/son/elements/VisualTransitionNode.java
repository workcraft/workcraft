package org.workcraft.plugins.son.elements;

import java.awt.Color;

import org.workcraft.dom.visual.DrawRequest;


public interface VisualTransitionNode{

	public void setFillColor(Color color);

	public void setForegroundColor(Color foregroundColor);

	public TransitionNode getMathTransitionNode();

	public void drawFault(DrawRequest r);

	public boolean isFaulty();

}
