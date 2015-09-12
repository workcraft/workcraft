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

package org.workcraft.plugins.petri;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualReplica;
import org.workcraft.gui.Coloriser;
import org.workcraft.gui.graph.tools.Decoration;

@DisplayName("Replica place")
@SVGIcon("images/icons/svg/terminal.svg")
public class VisualReplicaPlace extends VisualReplica {

	private double size = 0.5;
	private double strokeWidth = 0.1;

	public VisualReplicaPlace() {
		super();
	}

	public VisualReplicaPlace(VisualComponent master) {
		super();
		setMaster(master);
	}

	public Shape getShape() {
		double wh = size;
		double xy = - wh / 2.0;
		return new Ellipse2D.Double(xy, xy, wh, wh);
	}

	@Override
	public void draw(DrawRequest r)	{
		Graphics2D g = r.getGraphics();
		Decoration d = r.getDecoration();
		cacheRenderedText(r);  // needed to better estimate the bounding box
		Color colorisation = d.getColorisation();
		g.setColor(Coloriser.colorise(getForegroundColor(), colorisation));
		if (colorisation != null) {
			g.setStroke(new BasicStroke((float)strokeWidth));
			g.draw(getShape());
		}
		drawNameInLocalSpace(r);
	}

	public Place getReferencedPlace() {
		if (getMaster() instanceof VisualPlace) {
			VisualPlace visualPlace = (VisualPlace)getMaster();
			return visualPlace.getReferencedPlace();
		}
		return null;
	}

	@Override
    public Rectangle2D getInternalBoundingBoxInLocalSpace() {
        return getShape().getBounds2D();
    }

}
