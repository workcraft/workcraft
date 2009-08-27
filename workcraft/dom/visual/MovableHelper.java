package org.workcraft.dom.visual;

import java.awt.geom.AffineTransform;

public class MovableHelper {
	public static void translate(Movable m, double dx, double dy)
	{
		m.applyTransform(AffineTransform.getTranslateInstance(dx, dy));
	}
}
