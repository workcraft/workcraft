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
import java.awt.font.GlyphVector;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;

import org.workcraft.dom.math.MathNode;
import org.workcraft.gui.Coloriser;
import org.workcraft.gui.graph.tools.Decoration;
import org.workcraft.gui.propertyeditor.Getter;
import org.workcraft.gui.propertyeditor.SafePropertyDeclaration;
import org.workcraft.gui.propertyeditor.Setter;
import org.workcraft.observation.ObservableState;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.observation.StateEvent;
import org.workcraft.observation.StateObserver;
import org.workcraft.plugins.shared.CommonVisualSettings;

public abstract class VisualComponent extends VisualTransformableNode implements Drawable, DependentNode {
	private MathNode refNode = null;

	protected double size = CommonVisualSettings.getBaseSize();
	protected double strokeWidth = CommonVisualSettings.getStrokeWidth();

	public final Font labelFont = new Font("Sans-serif", Font.PLAIN, 1).deriveFont(0.5f);
	private GlyphVector labelGlyphs = null;
	private String glyphedLabel = null;
	private String label = "";
	private Positioning labelPositioning = CommonVisualSettings.getTextPositioning();
	private Point2D labelPosition = null;
	private Rectangle2D labelBoundingBox = null;
	private Color labelColor = CommonVisualSettings.getBorderColor();

	private Color foregroundColor = CommonVisualSettings.getBorderColor();
	private Color fillColor = CommonVisualSettings.getFillColor();

	private static final Font referenceFont = new Font("Sans-serif", Font.PLAIN, 1).deriveFont(0.5f);
	private Positioning referencePositioning = CommonVisualSettings.getTextPositioning();
	private Color referenceColor = CommonVisualSettings.getBorderColor();
	private RenderedText referenceText = new RenderedText(referenceFont, "");

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

		setFillColor(CommonVisualSettings.getFillColor());
		setForegroundColor(CommonVisualSettings.getBorderColor());
		setLabelColor(CommonVisualSettings.getBorderColor());
		setLabelPositioning(CommonVisualSettings.getTextPositioning());
	}

	private void addPropertyDeclarations() {
		addPropertyDeclaration(new SafePropertyDeclaration<VisualComponent, String>(
				this, "Label",
				new Getter<VisualComponent, String>() {
					@Override
					public String eval(VisualComponent object) {
						return object.getLabel();
					}
				},
				new Setter<VisualComponent, String>() {
					@Override
					public void eval(VisualComponent object, String value) {
						object.setLabel(value);
					}
				},
				String.class));

		addPropertyDeclaration(new SafePropertyDeclaration<VisualComponent, Color>(
				this, "Label color",
				new Getter<VisualComponent, Color>() {
					@Override
					public Color eval(VisualComponent object) {
						return object.getLabelColor();
					}
				},
				new Setter<VisualComponent, Color>() {
					@Override
					public void eval(VisualComponent object, Color value) {
						object.setLabelColor(value);
					}
				},
				Color.class));

		addPropertyDeclaration(new SafePropertyDeclaration<VisualComponent, Color>(
				this, "Foreground color",
				new Getter<VisualComponent, Color>() {
					@Override
					public Color eval(VisualComponent object) {
						return object.getForegroundColor();
					}
				},
				new Setter<VisualComponent, Color>() {
					@Override
					public void eval(VisualComponent object, Color value) {
						object.setForegroundColor(value);
					}
				},
				Color.class));

		addPropertyDeclaration(new SafePropertyDeclaration<VisualComponent, Color>(
				this, "Fill color",
				new Getter<VisualComponent, Color>() {
					@Override
					public Color eval(VisualComponent object) {
						return object.getFillColor();
					}
				},
				new Setter<VisualComponent, Color>() {
					@Override
					public void eval(VisualComponent object, Color value) {
						object.setFillColor(value);
					}
				},
				Color.class));

		addPropertyDeclaration(new SafePropertyDeclaration<VisualComponent, Positioning>(
				this, "Label positioning",
				new Getter<VisualComponent, Positioning>() {
					@Override
					public Positioning eval(VisualComponent object) {
						return object.getLabelPositioning();
					}
				},
				new Setter<VisualComponent, Positioning>() {
					@Override
					public void eval(VisualComponent object, Positioning value) {
						object.setLabelPositioning(value);
					}
				},
				Positioning.class, Positioning.getChoice()));
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
		labelGlyphs = null;
		labelBoundingBox = null;
		sendNotification(new PropertyChangedEvent(this, "label"));
	}

	public Positioning getLabelPositioning() {
		return labelPositioning;
	}

	public void setLabelPositioning(Positioning labelPositioning) {
		this.labelPositioning = labelPositioning;
		labelGlyphs = null;
		labelBoundingBox = null;
		sendNotification(new PropertyChangedEvent(this, "label positioning"));
	}

	public Color getLabelColor() {
		return labelColor;
	}

	public void setLabelColor(Color labelColor) {
		this.labelColor = labelColor;
		sendNotification(new PropertyChangedEvent(this, "label color"));
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

	public void updateGlyph(Graphics2D g) {
		if (labelBoundingBox == null || labelGlyphs == null || glyphedLabel == null || glyphedLabel != label) {
			glyphedLabel = label;
			labelGlyphs = labelFont.createGlyphVector(g.getFontRenderContext(),	getLabel());
			Rectangle2D bb = new Rectangle2D.Double(-size / 2, -size / 2, size,	size);
			Rectangle2D gbb = new Rectangle2D.Double(0, 0, 0, 0);
			if (!getLabel().isEmpty()) {
				gbb = labelGlyphs.getLogicalBounds();
			}
			labelPosition = new Point2D.Double(
					bb.getCenterX()	- gbb.getCenterX() + 0.5 * labelPositioning.dx * (bb.getWidth() + gbb.getWidth() + 0.2),
					bb.getCenterY() - gbb.getCenterY() + 0.5 * labelPositioning.dy * (bb.getHeight() + gbb.getHeight() + 0.2));
			labelBoundingBox = new Rectangle2D.Double(
					gbb.getX() + labelPosition.getX(),
					gbb.getY() + labelPosition.getY(),
					gbb.getWidth(), gbb.getHeight());
		}
	}

	protected void drawLabelInLocalSpace(DrawRequest r) {
		Graphics2D g = r.getGraphics();
		updateGlyph(g);
		g.setColor(Coloriser.colorise(labelColor, r.getDecoration().getColorisation()));
		g.setFont(labelFont);
		g.drawString(label, (float) labelPosition.getX(), (float) labelPosition.getY());
	}

	protected void drawReferenceInLocalSpace(DrawRequest r) {
		String str = r.getModel().getMathModel().getNodeReference(getReferencedComponent());
		if (str == null || !str.equals(referenceText.text)) {
			referenceText = new RenderedText(referenceFont, str);
		}
		Graphics2D g = r.getGraphics();
		Decoration d = r.getDecoration();
		g.setColor(Coloriser.colorise(referenceColor, d.getColorisation()));
		// TODO: Implement proper label and reference visualisation (with code reuse)
		//referenceText.draw(g);
	}

	public Rectangle2D getBoundingBoxInLocalSpace() {
		Rectangle2D bb = new Rectangle2D.Double(-size / 2, -size / 2, size,	size);
		bb = BoundingBoxHelper.union(bb, labelBoundingBox);
		bb = BoundingBoxHelper.union(bb, referenceText.getBoundingBox());
		return bb;
	}

	public boolean hitTestInLocalSpace(Point2D pointInLocalSpace) {
		return Math.abs(pointInLocalSpace.getX()) <= size / 2
				&& Math.abs(pointInLocalSpace.getY()) <= size / 2;
	}
}