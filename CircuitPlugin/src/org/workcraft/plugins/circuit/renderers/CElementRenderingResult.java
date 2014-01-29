package org.workcraft.plugins.circuit.renderers;

import java.awt.geom.Point2D;

public interface CElementRenderingResult extends ComponentRenderingResult {
	Point2D getLabelPosition();
	Point2D getPlusPosition();
	Point2D getMinusPosition();
}
