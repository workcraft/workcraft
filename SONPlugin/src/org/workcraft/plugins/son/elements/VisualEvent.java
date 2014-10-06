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
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.plugins.shared.CommonVisualSettings;
import org.workcraft.plugins.son.tools.ErrTracingDisable;


@Hotkey(KeyEvent.VK_E)
@DisplayName ("Event")
@SVGIcon("images/icons/svg/transition.svg")

public class VisualEvent extends VisualComponent implements VisualTransitionNode{
	//private boolean displayName = false;

	public VisualEvent(Event event) {
		super(event);
		addPropertyDeclarations();
	}

	private void addPropertyDeclarations() {
		addPropertyDeclaration(new PropertyDeclaration<VisualEvent, Boolean>(
				this, "Fault", Boolean.class) {
			public void setter(VisualEvent object, Boolean value) {
				((Event)getReferencedComponent()).setFaulty(value);
			}
			public Boolean getter(VisualEvent object) {
				return 	((Event)getReferencedComponent()).isFaulty();
			}
		});
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
		drawFault(r);
		drawLabelInLocalSpace(r);
		drawNameInLocalSpace(r);
	}

	public void drawFault(DrawRequest r){
		if (ErrTracingDisable.showErrorTracing()) {
			Graphics2D g = r.getGraphics();
			GlyphVector glyphVector=null;
			Rectangle2D labelBB=null;

			Font labelFont = new Font("Sans-serif", Font.PLAIN, 1).deriveFont(0.5f);

			if (isFaulty())
				glyphVector = labelFont.createGlyphVector(g.getFontRenderContext(), "1");
			else
				glyphVector = labelFont.createGlyphVector(g.getFontRenderContext(), "0");

			labelBB = glyphVector.getVisualBounds();
			Point2D bitPosition = new Point2D.Double(labelBB.getCenterX(), labelBB.getCenterY());
			g.drawGlyphVector(glyphVector, -(float)bitPosition.getX(), -(float)bitPosition.getY());
		}
	}

	@Override
	public boolean hitTestInLocalSpace(Point2D pointInLocalSpace)
	{
		return Math.abs(pointInLocalSpace.getX()) <= size / 2 && Math.abs(pointInLocalSpace.getY()) <= size / 2;
	}

	public Event getMathTransitionNode(){
		return (Event)this.getReferencedComponent();
	}

	public void setLabel(String label){
		super.setLabel(label);
		((Event)getReferencedComponent()).setLabel(label);
	}

	public String getLabel(){
		super.getLabel();
		return ((Event)getReferencedComponent()).getLabel();
	}

	public void setFaulty(Boolean fault){
		((Event)getReferencedComponent()).setFaulty(fault);
	}

	public boolean isFaulty(){
		return ((Event)getReferencedComponent()).isFaulty();
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
