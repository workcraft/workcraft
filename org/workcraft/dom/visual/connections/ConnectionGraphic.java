package org.workcraft.dom.visual.connections;

import java.awt.Graphics2D;

import org.w3c.dom.Element;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.Drawable;
import org.workcraft.dom.visual.Touchable;

public interface ConnectionGraphic extends Node, Drawable, Touchable, ParametricCurve {
	public void update();
	public void draw (Graphics2D g);

	public void writeToXML(Element element);
	public void readFromXML(Element element);
}
