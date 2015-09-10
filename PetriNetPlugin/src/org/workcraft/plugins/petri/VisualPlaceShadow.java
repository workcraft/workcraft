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
import org.workcraft.gui.Coloriser;
import org.workcraft.gui.graph.tools.Decoration;
import org.workcraft.observation.StateEvent;
import org.workcraft.observation.StateObserver;

@DisplayName("Terminal")
@SVGIcon("images/icons/svg/terminal.svg")
public class VisualPlaceShadow extends VisualComponent implements StateObserver {

	public VisualPlaceShadow(Place place) {
		super(place);
		place.addObserver(this);
		removePropertyDeclarations();
	}

	private void removePropertyDeclarations() {
		removePropertyDeclarationByName(VisualComponent.PROPERTY_LABEL);
		removePropertyDeclarationByName(VisualComponent.PROPERTY_LABEL_COLOR);
		removePropertyDeclarationByName(VisualComponent.PROPERTY_LABEL_POSITIONING);
	}

	public Shape getShape() {
		double wh = size / 2.0;
		double xy = - wh / 2.0;
		return new Ellipse2D.Double(xy, xy, wh, wh);
	}

	@Override
	public void draw(DrawRequest r)	{
		Graphics2D g = r.getGraphics();
		Decoration d = r.getDecoration();
		cacheRenderedText(r);  // needed to better estimate the bounding box
		Color colorisation =d.getColorisation();
		g.setColor(Coloriser.colorise(getForegroundColor(), colorisation));
		if (colorisation != null) {
			g.setStroke(new BasicStroke((float)strokeWidth/2.0f));
			g.draw(getShape());
		}
		drawNameInLocalSpace(r);
	}

	@Override
	public Rectangle2D getInternalBoundingBoxInLocalSpace() {
		return getShape().getBounds2D();
	}

	public Place getReferencedPlace() {
		return (Place)getReferencedComponent();
	}

	@Override
	public void notify(StateEvent e) {
	}

}
