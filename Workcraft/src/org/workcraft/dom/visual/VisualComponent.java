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

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;

import org.workcraft.dom.math.MathNode;
import org.workcraft.gui.Coloriser;
import org.workcraft.gui.graph.tools.Decoration;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.observation.ObservableState;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.observation.StateEvent;
import org.workcraft.observation.StateObserver;
import org.workcraft.plugins.shared.CommonVisualSettings;

public abstract class VisualComponent extends VisualTransformableNode implements Drawable, DependentNode {
	public static final Font labelFont = new Font("Sans-serif", Font.PLAIN, 1).deriveFont(0.5f);
	public static final Font nameFont = new Font("Sans-serif", Font.ITALIC, 1).deriveFont(0.5f);

	private MathNode refNode = null;
	protected double size = CommonVisualSettings.getBaseSize();
	protected double strokeWidth = CommonVisualSettings.getStrokeWidth();
	private Color foregroundColor = CommonVisualSettings.getBorderColor();
	private Color fillColor = CommonVisualSettings.getFillColor();

	private String label = "";
	private Positioning labelPositioning = CommonVisualSettings.getLabelPositioning();
	private RenderedText labelRenderedText = new RenderedText("", labelFont);
	private Color labelColor = CommonVisualSettings.getLabelColor();

	private Positioning namePositioning = CommonVisualSettings.getNamePositioning();
	private RenderedText nameRenderedText = new RenderedText("", nameFont);
	private Color nameColor = CommonVisualSettings.getNameColor();

	public VisualComponent(MathNode refNode) {
		super();
		this.refNode = refNode;

		if (refNode instanceof ObservableState)
			((ObservableState) refNode).addObserver(new StateObserver() {
				public void notify(StateEvent e) {
					observableStateImpl.sendNotification(e);
				}
			});

		addPropertyDeclarations();
	}

	private void addPropertyDeclarations() {
		addPropertyDeclaration(new PropertyDeclaration<VisualComponent, Color>(
				this, "Foreground color", Color.class) {
			protected void setter(VisualComponent object, Color value) {
				object.setForegroundColor(value);
			}
			protected Color getter(VisualComponent object) {
				return object.getForegroundColor();
			}
		});

		addPropertyDeclaration(new PropertyDeclaration<VisualComponent, Color>(
				this, "Fill color", Color.class) {
			protected void setter(VisualComponent object, Color value) {
				object.setFillColor(value);
			}
			protected Color getter(VisualComponent object) {
				return object.getFillColor();
			}
		});

		addPropertyDeclaration(new PropertyDeclaration<VisualComponent, String>(
				this, "Label", String.class) {
			protected void setter(VisualComponent object, String value) {
				object.setLabel(value);
			}
			protected String getter(VisualComponent object) {
				return object.getLabel();
			}
		});

		addPropertyDeclaration(new PropertyDeclaration<VisualComponent, Positioning>(
				this, "Label positioning", Positioning.class, Positioning.getChoice()) {
			protected void setter(VisualComponent object, Positioning value) {
				object.setLabelPositioning(value);
			}
			protected Positioning getter(VisualComponent object) {
				return object.getLabelPositioning();
			}
		});

		addPropertyDeclaration(new PropertyDeclaration<VisualComponent, Color>(
				this, "Label color", Color.class) {
			protected void setter(VisualComponent object, Color value) {
				object.setLabelColor(value);
			}
			protected Color getter(VisualComponent object) {
				return object.getLabelColor();
			}
		});

		addPropertyDeclaration(new PropertyDeclaration<VisualComponent, Positioning>(
				this, "Name positioning", Positioning.class, Positioning.getChoice()) {
			protected void setter(VisualComponent object, Positioning value) {
				object.setNamePositioning(value);
			}
			protected Positioning getter(VisualComponent object) {
				return object.getNamePositioning();
			}
		});

		addPropertyDeclaration(new PropertyDeclaration<VisualComponent, Color>(
				this, "Name color", Color.class) {
			protected void setter(VisualComponent object, Color value) {
				object.setNameColor(value);
			}
			protected Color getter(VisualComponent object) {
				return object.getNameColor();
			}
		});
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
		sendNotification(new PropertyChangedEvent(this, "label"));
	}

	public Positioning getLabelPositioning() {
		return labelPositioning;
	}

	public void setLabelPositioning(Positioning labelPositioning) {
		this.labelPositioning = labelPositioning;
		sendNotification(new PropertyChangedEvent(this, "label positioning"));
	}

	public Color getLabelColor() {
		return labelColor;
	}

	public void setLabelColor(Color labelColor) {
		this.labelColor = labelColor;
		sendNotification(new PropertyChangedEvent(this, "label color"));
	}

	public Positioning getNamePositioning() {
		return namePositioning;
	}

	public void setNamePositioning(Positioning referencePositioning) {
		this.namePositioning = referencePositioning;
		sendNotification(new PropertyChangedEvent(this, "reference positioning"));
	}

	public Color getNameColor() {
		return nameColor;
	}

	public void setNameColor(Color referenceColor) {
		this.nameColor = referenceColor;
		sendNotification(new PropertyChangedEvent(this, "reference color"));
	}

	public Color getForegroundColor() {
		return foregroundColor;
	}

	public void setForegroundColor(Color foregroundColor) {
		this.foregroundColor = foregroundColor;
		sendNotification(new PropertyChangedEvent(this, "foreground color"));
	}

	public Color getFillColor() {
		return fillColor;
	}

	public void setFillColor(Color fillColor) {
		this.fillColor = fillColor;
		sendNotification(new PropertyChangedEvent(this, "fill color"));
	}

	public MathNode getReferencedComponent() {
		return refNode;
	}

	@Override
	public Collection<MathNode> getMathReferences() {
		ArrayList<MathNode> result = new ArrayList<MathNode>();
		result.add(getReferencedComponent());
		return result;
	}

	@Override
	public Point2D getCenterInLocalSpace() {
		return new Point2D.Double(0, 0);
	}

	protected void drawLabelInLocalSpace(DrawRequest r) {
		if (CommonVisualSettings.getLabelVisibility()) {
			if (!label.equals(labelRenderedText.text) || labelFont != labelRenderedText.font) {
				labelRenderedText = new RenderedText(label, labelFont);
			}
			double x = 0.5 * labelPositioning.xOffset * size
					 + 0.5 * labelPositioning.xSign * labelRenderedText.getBoundingBox().getWidth();
			double y = 0.5 * labelPositioning.yOffset * size
					 + 0.5 * labelPositioning.ySign * labelRenderedText.getBoundingBox().getHeight();
			labelRenderedText.setCenter(x, y);
			Graphics2D g = r.getGraphics();
			Decoration d = r.getDecoration();
			g.setColor(Coloriser.colorise(labelColor, d.getColorisation()));
			labelRenderedText.draw(g);
		}
	}

	protected void drawNameInLocalSpace(DrawRequest r) {
		if (CommonVisualSettings.getNameVisibility()) {
			String name = r.getModel().getMathModel().getNodeReference(getReferencedComponent());
			if (name == null) {
				name = "";
			}
			if (!name.equals(nameRenderedText.text) || nameFont != nameRenderedText.font) {
				nameRenderedText = new RenderedText(name, nameFont);
			}
			double x = 0.5 * namePositioning.xOffset * size
					+ 0.5 * namePositioning.xSign * nameRenderedText.getBoundingBox().getWidth();
			double y = 0.5 * namePositioning.yOffset * size
					+ 0.5 * namePositioning.ySign * nameRenderedText.getBoundingBox().getHeight();
			nameRenderedText.setCenter(x, y);
			Graphics2D g = r.getGraphics();
			Decoration d = r.getDecoration();
			g.setColor(Coloriser.colorise(nameColor, d.getColorisation()));
			nameRenderedText.draw(g);
		}
	}

	@Override
	public Rectangle2D getBoundingBoxInLocalSpace() {
		Rectangle2D bb = new Rectangle2D.Double(-size / 2, -size / 2, size,	size);
		if (CommonVisualSettings.getLabelVisibility()) {
			bb = BoundingBoxHelper.union(bb, labelRenderedText.getBoundingBox());
		}
		if (CommonVisualSettings.getNameVisibility()) {
			bb = BoundingBoxHelper.union(bb, nameRenderedText.getBoundingBox());
		}
		return bb;
	}

	@Override
	public boolean hitTestInLocalSpace(Point2D pointInLocalSpace) {
		return Math.abs(pointInLocalSpace.getX()) <= size / 2
			&& Math.abs(pointInLocalSpace.getY()) <= size / 2;
	}
}