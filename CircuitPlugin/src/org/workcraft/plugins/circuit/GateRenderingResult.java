package org.workcraft.plugins.circuit;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Map;

import org.workcraft.plugins.cpog.optimisation.BooleanVariable;


public  interface GateRenderingResult {
	Rectangle2D boundingBox();
	Map<BooleanVariable, Point2D> contactPositions();
	void draw(Graphics2D graphics);
}
