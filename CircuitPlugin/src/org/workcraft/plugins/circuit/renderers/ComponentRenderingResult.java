package org.workcraft.plugins.circuit.renderers;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Map;


public interface ComponentRenderingResult {
	public enum RenderType {
		BOX, GATE, BUFFER, C_ELEMENT
	}

	Rectangle2D boundingBox();
	Map<String, Point2D> contactPositions();
	void draw(Graphics2D graphics);
}
