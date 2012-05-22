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
import java.util.LinkedHashMap;

import org.workcraft.dom.math.MathNode;
import org.workcraft.gui.Coloriser;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.observation.ObservableState;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.observation.StateEvent;
import org.workcraft.observation.StateObserver;
import org.workcraft.plugins.shared.CommonVisualSettings;

public abstract class VisualComponent extends VisualTransformableNode implements
		Drawable, DependentNode {
	private MathNode refNode = null;

	protected final static double size = CommonVisualSettings.getSize();
	protected final static double strokeWidth = CommonVisualSettings.getStrokeWidth();
	protected final static Font labelFont = new Font("Sans-serif", Font.PLAIN, 1).deriveFont(0.5f);

	private GlyphVector labelGlyphs = null;

	private String label = "";
	private Positioning labelPositioning = CommonVisualSettings.getLabelPositioning();
	private Point2D labelPosition = null;
	private Rectangle2D labelBoundingBox = null;
	private Color labelColor = CommonVisualSettings.getForegroundColor();

	private Color foregroundColor = CommonVisualSettings.getForegroundColor();
	private Color fillColor = CommonVisualSettings.getFillColor();

	private void addPropertyDeclarations() {
		addPropertyDeclaration(new PropertyDeclaration(this, "Label",
				"getLabel", "setLabel", String.class));
		addPropertyDeclaration(new PropertyDeclaration(this, "Label color",
				"getLabelColor", "setLabelColor", Color.class));
		addPropertyDeclaration(new PropertyDeclaration(this,
				"Foreground color", "getForegroundColor", "setForegroundColor",
				Color.class));
		addPropertyDeclaration(new PropertyDeclaration(this, "Fill color",
				"getFillColor", "setFillColor", Color.class));

		LinkedHashMap<String, Object> positions = new LinkedHashMap<String, Object>();
		for(Positioning lp : Positioning.values())
			positions.put(lp.name, lp);

		addPropertyDeclaration(new PropertyDeclaration(this, "Label positioning",
				"getLabelPositioning", "setLabelPositioning", Positioning.class, positions));

	}

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
		setForegroundColor(CommonVisualSettings.getForegroundColor());
		setLabelColor(CommonVisualSettings.getForegroundColor());
		setLabelPositioning(CommonVisualSettings.getLabelPositioning());
	}

	public VisualComponent() {
		addPropertyDeclarations();
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

	public Positioning getLabelPositioning()
	{
		return labelPositioning;
	}

	public void setLabelPositioning(Positioning labelPositioning)
	{
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
	}

	public Color getForegroundColor() {
		return foregroundColor;
	}

	public void setForegroundColor(Color foregroundColor) {
		this.foregroundColor = foregroundColor;
	}

	public Color getFillColor() {
		return fillColor;
	}

	public void setFillColor(Color fillColor) {
		this.fillColor = fillColor;
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
		updateGlyph(r.getGraphics());
		Graphics2D g = r.getGraphics();
		g.setColor(Coloriser.colorise(labelColor, r.getDecoration().getColorisation()));
		g.setFont(labelFont);
		g.drawString(label, (float) labelPosition.getX(), (float) labelPosition.getY());
	}

	private void updateGlyph(Graphics2D g) {
		if (labelBoundingBox == null || labelGlyphs == null) {
			labelGlyphs = labelFont.createGlyphVector(g.getFontRenderContext(), getLabel());
			Rectangle2D bb = new Rectangle2D.Double(-size / 2, -size / 2, size, size);
			Rectangle2D gbb = labelGlyphs.getLogicalBounds();
			labelPosition = new Point2D.Double(
					bb.getCenterX() - gbb.getCenterX() + 0.5 * labelPositioning.dx * (bb.getWidth() + gbb.getWidth() + 0.2),
					bb.getCenterY() - gbb.getCenterY() + 0.5 * labelPositioning.dy * (bb.getHeight() + gbb.getHeight() + 0.2));
			labelBoundingBox = new Rectangle2D.Double(gbb.getX() + labelPosition.getX(), gbb.getY() + labelPosition.getY(),
					gbb.getWidth(), gbb.getHeight());
		}
	}

	public GlyphVector getLabelGlyphs(Graphics2D g) {
		updateGlyph(g);
		return labelGlyphs;
	}

	public Rectangle2D getLabelBB(Graphics2D g) {
		return getLabelGlyphs(g).getVisualBounds();
	}

	public Rectangle2D getBoundingBoxInLocalSpace()
	{
		Rectangle2D bb = new Rectangle2D.Double(-size / 2, -size / 2, size, size);
		return BoundingBoxHelper.union(bb, labelBoundingBox);
	}

	public boolean hitTestInLocalSpace(Point2D pointInLocalSpace)
	{
		return Math.abs(pointInLocalSpace.getX()) <= size / 2 && Math.abs(pointInLocalSpace.getY()) <= size / 2;
	}

}