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
import java.util.Collection;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.Hotkey;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.dom.Container;
import org.workcraft.dom.DefaultGroupImpl;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.BoundingBoxHelper;
import org.workcraft.dom.visual.CustomTouchable;
import org.workcraft.dom.visual.Distributable;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.Stylable;
import org.workcraft.dom.visual.Touchable;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.dom.visual.VisualTransformableNode;
import org.workcraft.gui.Coloriser;
import org.workcraft.gui.graph.tools.Decoration;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.observation.HierarchyObserver;
import org.workcraft.observation.ObservableHierarchy;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.observation.StateEvent;
import org.workcraft.observation.StateObserver;
import org.workcraft.plugins.petri.tools.PlaceDecoration;
import org.workcraft.plugins.shared.CommonVisualSettings;
import org.workcraft.util.Hierarchy;

@DisplayName("Place")
@Hotkey(KeyEvent.VK_P)
@SVGIcon("images/icons/svg/place.svg")
public class VisualPlace extends VisualComponent implements Container, Distributable, CustomTouchable,
		StateObserver, ObservableHierarchy {

	public static final String PROPERTY_TOKEN_COLOR = "Token color";
	public static final String PROPERTY_DISTRIBUTED = "Distributed";

	protected static double singleTokenSize = CommonVisualSettings.getBaseSize() / 1.9;
	protected static double multipleTokenSeparation = CommonVisualSettings.getStrokeWidth() / 8;
	protected Color tokenColor = CommonVisualSettings.getBorderColor();

	protected DefaultGroupImpl groupImpl = new DefaultGroupImpl(this);
	private boolean distributed = true;

	public VisualPlace(Place place) {
		super(place);
		addPropertyDeclarations();
	}

	private void addPropertyDeclarations() {
		addPropertyDeclaration(new PropertyDeclaration<VisualPlace, Integer>(
				this, Place.PROPERTY_TOKENS, Integer.class, true, true, true) {
			public void setter(VisualPlace object, Integer value) {
				object.getReferencedPlace().setTokens(value);
			}
			public Integer getter(VisualPlace object) {
				return object.getReferencedPlace().getTokens();
			}
		});

		addPropertyDeclaration(new PropertyDeclaration<VisualPlace, Color>(
				this, PROPERTY_TOKEN_COLOR, Color.class, true, true, true) {
			public void setter(VisualPlace object, Color value) {
				object.setTokenColor(value);
			}
			public Color getter(VisualPlace object) {
				return object.getTokenColor();
			}
		});

		addPropertyDeclaration(new PropertyDeclaration<VisualPlace, Integer>(
				this, Place.PROPERTY_CAPACITY, Integer.class, true, true, true) {
			public void setter(VisualPlace object, Integer value) {
				object.getReferencedPlace().setCapacity(value);
			}
			public Integer getter(VisualPlace object) {
				return object.getReferencedPlace().getCapacity();
			}
		});

		addPropertyDeclaration(new PropertyDeclaration<VisualPlace, Boolean>(
				this, PROPERTY_DISTRIBUTED, Boolean.class, true, true, true) {
			public void setter(VisualPlace object, Boolean value) {
				object.setIsDistributed(value);
			}
			public Boolean getter(VisualPlace object) {
				return object.getIsDistributed();
			}
		});
	}

	@Override
	public void draw(DrawRequest r)	{
		Graphics2D g = r.getGraphics();
		Decoration d = r.getDecoration();

		double xy = -size / 2 + strokeWidth / 2;
		double wh = size - strokeWidth;
		Shape shape = new Ellipse2D.Double(xy, xy, wh, wh);

		g.setColor(Coloriser.colorise(getFillColor(), d.getBackground()));
		g.fill(shape);
		g.setColor(Coloriser.colorise(getForegroundColor(), d.getColorisation()));
		g.setStroke(new BasicStroke((float)strokeWidth));
		g.draw(shape);

		Place place = (Place)getReferencedComponent();
		int tokenCount = place.getTokens();
		Color tokenColor = getTokenColor();
		if (d instanceof PlaceDecoration) {
			tokenCount = ((PlaceDecoration)d).getTokens();
			tokenColor = ((PlaceDecoration)d).getTokenColor();
		}
		drawCapacity(r, place.getCapacity());
		drawTokens(r, tokenCount, singleTokenSize, multipleTokenSeparation, size, strokeWidth, tokenColor);

		drawLabelInLocalSpace(r);
		drawNameInLocalSpace(r);
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
		sendNotification(new PropertyChangedEvent(this, PROPERTY_TOKEN_COLOR));
	}

	@Override
	public void copyStyle(Stylable src) {
		super.copyStyle(src);
		if (src instanceof VisualPlace) {
			VisualPlace srcPlace = (VisualPlace)src;
			getReferencedPlace().setCapacity(srcPlace.getReferencedPlace().getCapacity());
			getReferencedPlace().setTokens(srcPlace.getReferencedPlace().getTokens());
			setTokenColor(srcPlace.getTokenColor());
		}
	}

	public static void drawTokens(DrawRequest r, int count, double size, double separation,
			double diameter, double borderWidth, Color color) {
		Graphics2D g = r.getGraphics();
		Decoration d = r.getDecoration();
		Shape shape;
		if (count == 1) {
			shape = new Ellipse2D.Double(-size / 2, -size / 2,	size, size);
			g.setColor(Coloriser.colorise(color, d.getColorisation()));
			g.fill(shape);
		} else {
			if (count > 1 && count < 8) {
				double alpha = Math.PI / count;
				if (count == 7) alpha = Math.PI / 6;
				double radius = (diameter / 2 - borderWidth - separation) / (1 + 1 / Math.sin(alpha));
				double step = radius / Math.sin(alpha);
				radius -= separation;
				for(int i = 0; i < count; i++) 	{
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
			} else if (count > 7)	{
				String tokenString = Integer.toString(count);
				Font superFont = g.getFont().deriveFont((float)CommonVisualSettings.getBaseSize()/2);
				Rectangle2D rect = superFont.getStringBounds(tokenString, g.getFontRenderContext());
				g.setFont(superFont);
				g.setColor(Coloriser.colorise(color, d.getColorisation()));
				g.drawString(tokenString, (float)(-rect.getCenterX()), (float)(-rect.getCenterY()));
			}
		}
	}


	@Override
	public Collection<Node> getChildren() {
		return groupImpl.getChildren();
	}

	@Override
	public void add(Node node) {
		groupImpl.add(node);
		if (node instanceof VisualPlaceShadow) {
			((VisualPlaceShadow) node).addObserver(this);
		}
	}

	@Override
	public void remove(Node node) {
		if (node instanceof VisualPlaceShadow) {
		}
		groupImpl.remove(node);
	}

	@Override
	public void add(Collection<Node> nodes) {
		groupImpl.add(nodes);
		for (Node node : nodes) {
			if (node instanceof VisualPlaceShadow) {
				((VisualPlaceShadow) node).addObserver(this);
			}
		}
	}

	@Override
	public void remove(Collection<Node> nodes) {
		for (Node n : nodes) {
			remove(n);
		}
	}

	@Override
	public void reparent(Collection<Node> nodes, Container newParent) {
		groupImpl.reparent(nodes, newParent);
	}

	@Override
	public void reparent(Collection<Node> nodes) {
		groupImpl.reparent(nodes);
	}

	@Override
	public void addObserver(HierarchyObserver obs) {
		groupImpl.addObserver(obs);
	}

	@Override
	public void removeObserver(HierarchyObserver obs) {
		groupImpl.removeObserver(obs);
	}

	@Override
	public void removeAllObservers() {
		groupImpl.removeAllObservers();
	}

	@Override
	public void setIsDistributed(boolean value) {
		distributed = value;
	}

	@Override
	public boolean getIsDistributed() {
		return distributed;
	}


	public Rectangle2D getOutlineBoxInLocalSpace() {
		return super.getInternalBoundingBoxInLocalSpace();
	}

	@Override
    public final Rectangle2D getOutlineBox() {
    	return transformToParentSpace(getOutlineBoxInLocalSpace());
    }


	@Override
	public Rectangle2D getBoundingBoxInLocalSpace() {
		Rectangle2D bb = super.getInternalBoundingBoxInLocalSpace();
		if (getIsDistributed()) {
			Collection<Touchable> children = Hierarchy.getChildrenOfType(this, Touchable.class);
			bb = BoundingBoxHelper.union(bb, BoundingBoxHelper.mergeBoundingBoxes(children));
		}
		return bb;
	}

	@Override
	public Node customHitTest(Point2D point) {
		Point2D pointInLocalSpace = getParentToLocalTransform().transform(point, null);
		for (Node node : getChildren()) {
			if (node instanceof VisualNode) {
				VisualTransformableNode vn = (VisualTransformableNode)node;
				if (vn.hitTest(pointInLocalSpace)) {
					return vn;
				}
			}
		}
		return (hitTest(point) ? this : null);
	}

	@Override
	public void notify(StateEvent e) {
	}

}
