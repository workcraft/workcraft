package org.workcraft.dom.visual;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;


class DrawMan
{
	private DrawMan(){}

	private static void transformAndDraw(Graphics2D graphics, Movable node)
	{
		graphics.transform(node.getTransform());
		simpleDraw(graphics, node);
	}

	public static void draw(Graphics2D graphics, HierarchyNode node)
	{
		AffineTransform oldTransform = graphics.getTransform();
		if(node instanceof Movable)
			transformAndDraw(graphics, (Movable)node);
		else
			simpleDraw(graphics, node);
		graphics.setTransform(oldTransform);
	}

	private static void simpleDraw(Graphics2D graphics, HierarchyNode node)
	{
		if(node instanceof Drawable)
			((Drawable)node).draw(graphics);
		for(HierarchyNode n : node.getChildren())
		{
			draw(graphics, n);
		}
	}
}