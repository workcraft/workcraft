/*
*
* Copyright 2008,2009 Newcastle University
*
* This file is part of Workcraft.
*
* Workcraft is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* Workcraft is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with Workcraft.  If not, see <http://www.gnu.org/licenses/>.
*
*/

package org.workcraft.dom.visual;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

import org.workcraft.dom.Node;


class DrawMan
{
	private DrawMan(){}

	private static void transformAndDraw(Graphics2D graphics, Movable node)
	{
		graphics.transform(node.getTransform());
		simpleDraw(graphics, node);
	}

	public static void draw(Graphics2D graphics, Node node)
	{
		if (node instanceof Hidable && ((Hidable)node).isHidden())
			return;

		AffineTransform oldTransform = graphics.getTransform();
		if(node instanceof Movable)
			transformAndDraw(graphics, (Movable)node);
		else
			simpleDraw(graphics, node);
		graphics.setTransform(oldTransform);
	}

	private static void simpleDraw(Graphics2D graphics, Node node)
	{
		AffineTransform oldTransform = graphics.getTransform();
		if(node instanceof Drawable)
			((Drawable)node).draw(graphics);
		graphics.setTransform(oldTransform);

		for(Node n : node.getChildren())
			draw(graphics, n);
	}
}