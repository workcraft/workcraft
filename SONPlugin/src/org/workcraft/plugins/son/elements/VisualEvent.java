package org.workcraft.plugins.son.elements;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.KeyEvent;
import java.awt.font.GlyphVector;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.Hotkey;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.gui.Coloriser;
import org.workcraft.plugins.shared.CommonVisualSettings;
import org.workcraft.plugins.son.SONSettings;
import org.workcraft.serialisation.xml.NoAutoSerialisation;

@Hotkey(KeyEvent.VK_E)
@DisplayName ("Event")
@SVGIcon("images/icons/svg/transition.svg")

public class VisualEvent extends VisualComponent {
	private boolean displayName = false;

	public VisualEvent(Event event) {
		super(event);
	}

	@Override
	public void draw(DrawRequest r){
		Graphics2D g = r.getGraphics();
		Shape shape = new Rectangle2D.Double(
				-size / 2 + strokeWidth / 2,
				-size / 2 + strokeWidth / 2,
				size - strokeWidth,
				size - strokeWidth);
		g.setColor(Coloriser.colorise(Coloriser.colorise(getFillColor(), r.getDecoration().getBackground()), r.getDecoration().getColorisation()));
		g.fill(shape);
		g.setColor(Coloriser.colorise(Coloriser.colorise(getForegroundColor(), r.getDecoration().getBackground()), r.getDecoration().getColorisation()));
		g.setStroke(new BasicStroke((float)CommonVisualSettings.getStrokeWidth()));
		g.draw(shape);
		drawName(g, size, strokeWidth);
		drawLabelInLocalSpace(r);

	}

	public void drawName(Graphics2D g, double size, double strokeWidth){
		if (SONSettings.getDisplayName()) {
			GlyphVector glyphVector=null;
			Rectangle2D labelBB=null;

			Font labelFont = new Font("Sans-serif", Font.PLAIN, 1).deriveFont(0.4f);
			glyphVector = labelFont.createGlyphVector(g.getFontRenderContext(), getName());

			labelBB = glyphVector.getVisualBounds();
			Point2D labelPosition = new Point2D.Double(labelBB.getCenterX(), labelBB.getCenterY());
			g.drawGlyphVector(glyphVector, -(float)labelPosition.getX(), -(float)labelPosition.getY());
		}
	}

	@Override
	public boolean hitTestInLocalSpace(Point2D pointInLocalSpace)
	{
		return Math.abs(pointInLocalSpace.getX()) <= size / 2 && Math.abs(pointInLocalSpace.getY()) <= size / 2;
	}

	@NoAutoSerialisation
	public String getName(){
		return ((Event)getReferencedComponent()).getName();
	}

	@NoAutoSerialisation
	public void setName(String name){
		((Event)getReferencedComponent()).setName(name);
	}

	public void setDisplayName(boolean showName){
		this.displayName = showName;
	}

	public boolean isDisplayName(){
		return displayName;
	}

	public Event getReferencedEvent() {
		return (Event)getReferencedComponent();
	}

	public void setLabel(String label){
		super.setLabel(label);
		((Event)getReferencedComponent()).setLabel(label);
	}

	public String getLabel(){
		super.getLabel();
		return ((Event)getReferencedComponent()).getLabel();
	}

	public Color getForegroundColor() {
		return ((Event)getReferencedComponent()).getForegroundColor();
	}

	public void setForegroundColor(Color foregroundColor) {
		((Event)getReferencedComponent()).setForegroundColor(foregroundColor);
	}

	public void setFillColor(Color fillColor){
		((Event)getReferencedComponent()).setFillColor(fillColor);
	}

	public Color getFillColor(){
		return ((Event)getReferencedComponent()).getFillColor();
	}

}
