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
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.KeyEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.Hotkey;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.gui.Coloriser;
import org.workcraft.gui.graph.tools.Decoration;
import org.workcraft.gui.propertyeditor.Getter;
import org.workcraft.gui.propertyeditor.SafePropertyDeclaration;
import org.workcraft.gui.propertyeditor.Setter;
import org.workcraft.plugins.petri.tools.PlaceDecoration;
import org.workcraft.plugins.shared.CommonVisualSettings;

@DisplayName("Place")
@Hotkey(KeyEvent.VK_P)
@SVGIcon("images/icons/svg/place.svg")
public class VisualPlace extends VisualComponent {

	protected static double singleTokenSize = CommonVisualSettings.getBaseSize() / 1.9;
	protected static double multipleTokenSeparation = CommonVisualSettings.getStrokeWidth() / 8;
	protected Color tokenColor = CommonVisualSettings.getBorderColor();

	public VisualPlace(Place place) {
		super(place);
		addPropertyDeclarations();
	}

	private void addPropertyDeclarations() {
		addPropertyDeclaration(new SafePropertyDeclaration<VisualPlace, Integer>(
				this, "Tokens",
				new Getter<VisualPlace, Integer>() {
					@Override
					public Integer eval(VisualPlace object) {
						return object.getReferencedPlace().getTokens();
					}
				},
				new Setter<VisualPlace, Integer>() {
					@Override
					public void eval(VisualPlace object, Integer value) {
						object.getReferencedPlace().setTokens(value);
					}
				},
				Integer.class));


		addPropertyDeclaration(new SafePropertyDeclaration<VisualPlace, Color>(
				this, "Token color",
				new Getter<VisualPlace, Color>() {
					@Override
					public Color eval(VisualPlace object) {
						return object.getTokenColor();
					}
				},
				new Setter<VisualPlace, Color>() {
					@Override
					public void eval(VisualPlace object, Color value) {
						object.setTokenColor(value);
					}
				},
				Color.class));

		addPropertyDeclaration(new SafePropertyDeclaration<VisualPlace, Integer>(
				this, "Capacity",
				new Getter<VisualPlace, Integer>() {
					@Override
					public Integer eval(VisualPlace object) {
						return object.getReferencedPlace().getCapacity();
					}
				},
				new Setter<VisualPlace, Integer>() {
					@Override
					public void eval(VisualPlace object, Integer value) {
						object.getReferencedPlace().setCapacity(value);
					}
				},
				Integer.class));
	}

	@Override
	public void draw(DrawRequest r)	{
		Graphics2D g = r.getGraphics();
		Decoration d = r.getDecoration();

		double xy = -size / 2 + strokeWidth / 2;
		double wh = size - strokeWidth;
		Shape shape = new Ellipse2D.Double(xy, xy, wh, wh);

		Color borderColor = Coloriser.colorise(getForegroundColor(), d.getColorisation());
		Color fillColor = Coloriser.colorise(getFillColor(), d.getColorisation());
		g.setColor(fillColor);
		g.fill(shape);
		g.setColor(borderColor);
		g.setStroke(new BasicStroke((float)strokeWidth));
		g.draw(shape);

		Place p = (Place)getReferencedComponent();
		int tokens = p.getTokens();
		if (d instanceof PlaceDecoration) {
			tokens = ((PlaceDecoration)d).getTokens();
		}
		drawCapacity(r, p.getCapacity());
		drawTokens(r, tokens, singleTokenSize, multipleTokenSeparation, size, strokeWidth, getTokenColor());
		drawLabelInLocalSpace(r);
	}

	public void drawCapacity(DrawRequest r, int capacity) {
		if (capacity != 1) {
			Graphics2D g = r.getGraphics();
			Decoration d = r.getDecoration();
			String capacityString = Integer.toString(capacity);
			Font superFont = g.getFont().deriveFont((float)CommonVisualSettings.getBaseSize()/2);
			Rectangle2D rect = superFont.getStringBounds(capacityString, g.getFontRenderContext());
			g.setFont(superFont);
			g.setColor(Coloriser.colorise(getTokenColor(), d.getColorisation()));
			g.drawString(capacityString, (float)(size/3), (float)(size/3 + rect.getHeight()));
		}
	}

	public static void drawTokens(DrawRequest r, int tokens, double size, double separation,
			double diameter, double borderWidth, Color color) {
		Graphics2D g = r.getGraphics();
		Decoration d = r.getDecoration();
		Shape shape;
		if (tokens == 1) {
			shape = new Ellipse2D.Double(-size / 2, -size / 2,	size, size);
			g.setColor(Coloriser.colorise(color, d.getColorisation()));
			g.fill(shape);
		} else {
			if (tokens > 1 && tokens < 8) {
				double alpha = Math.PI / tokens;
				if (tokens == 7) alpha = Math.PI / 6;
				double radius = (diameter / 2 - borderWidth - separation) / (1 + 1 / Math.sin(alpha));
				double step = radius / Math.sin(alpha);
				radius -= separation;
				for(int i = 0; i < tokens; i++) 	{
					if (i == 6) {
						shape = new Ellipse2D.Double( -radius, -radius, radius * 2, radius * 2);
					} else {
						shape = new Ellipse2D.Double(
								-step * Math.sin(i * alpha * 2) - radius,
								-step * Math.cos(i * alpha * 2) - radius,
								radius * 2,	radius * 2);
					}
					g.setColor(Coloriser.colorise(color, d.getColorisation()));
					g.fill(shape);
				}
			} else if (tokens > 7)	{
				String tokenString = Integer.toString(tokens);
				Font superFont = g.getFont().deriveFont((float)CommonVisualSettings.getBaseSize()/2);
				Rectangle2D rect = superFont.getStringBounds(tokenString, g.getFontRenderContext());
				g.setFont(superFont);
				g.setColor(Coloriser.colorise(color, d.getColorisation()));
				g.drawString(tokenString, (float)(-rect.getCenterX()), (float)(-rect.getCenterY()));
			}
		}
	}

	@Override
	public boolean hitTestInLocalSpace(Point2D pointInLocalSpace) {
		return pointInLocalSpace.distanceSq(0, 0) < size * size / 4;
	}

	public Place getReferencedPlace() {
		return (Place)getReferencedComponent();
	}

	public Color getTokenColor() {
		return tokenColor;
	}

	public void setTokenColor(Color tokenColor) {
		this.tokenColor = tokenColor;
	}
}
