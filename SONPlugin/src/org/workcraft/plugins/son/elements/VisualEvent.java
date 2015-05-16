package org.workcraft.plugins.son.elements;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.font.GlyphVector;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.Hotkey;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.Positioning;
import org.workcraft.dom.visual.RenderedText;
import org.workcraft.dom.visual.Stylable;
import org.workcraft.gui.Coloriser;
import org.workcraft.gui.graph.tools.Decoration;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.plugins.petri.VisualTransition;
import org.workcraft.plugins.son.SONSettings;
import org.workcraft.plugins.son.tools.ErrTracingDisable;


@Hotkey(KeyEvent.VK_E)
@DisplayName ("Event")
@SVGIcon("images/icons/svg/transition.svg")

public class VisualEvent extends VisualTransition implements VisualTransitionNode{
	//private boolean displayName = false;

	private Font font = new Font("Sans-serif", Font.PLAIN, 1).deriveFont(0.45f);
	private Color durationColor = SONSettings.getErrLabelColor();
	private Positioning durationLabelPositioning = SONSettings.getDurationLabelPositioning();
	private RenderedText durationRenderedText = new RenderedText("", font, durationLabelPositioning, new Point2D.Double(0.0,0.0));

	public VisualEvent(Event event) {
		super(event);
		addPropertyDeclarations();
	}

	private void addPropertyDeclarations() {
		addPropertyDeclaration(new PropertyDeclaration<VisualEvent, Boolean>(
				this, "Fault", Boolean.class, true, true, true) {
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
		super.draw(r);
		drawFault(r);
		drawDurationInLocalSpace(r);
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

	private void cahceDurationRenderedText(DrawRequest r) {
		String duration = "d: "+ this.getDuration();
		//double o = 0.8 * size;

		Point2D offset = getOffset(durationLabelPositioning);
		if (durationLabelPositioning.ySign<0) {
			offset.setLocation(offset.getX(), offset.getY()-0.6);
		} else {
			offset.setLocation(offset.getX(), offset.getY()+0.6);
		}

		if (durationRenderedText.isDifferent(duration, font, durationLabelPositioning, offset)) {
			durationRenderedText = new RenderedText(duration, font, durationLabelPositioning, offset);
		}
	}

	protected void drawDurationInLocalSpace(DrawRequest r) {
		if (SONSettings.getTimeVisibility()) {
			cahceDurationRenderedText(r);
			Graphics2D g = r.getGraphics();
			Decoration d = r.getDecoration();
			g.setColor(Coloriser.colorise(durationColor, d.getColorisation()));
			durationRenderedText.draw(g);
		}
	}

	@Override
	public void cacheRenderedText(DrawRequest r) {
		super.cacheRenderedText(r);
		cahceDurationRenderedText(r);
	}

	public String getDuration(){
		return ((Event)getReferencedComponent()).getDuration();
	}

	public void setDuration(String time){
		((Event)getReferencedComponent()).setDuration(time);
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

	@Override
	public void copyStyle(Stylable src) {
		super.copyStyle(src);
		if (src instanceof VisualEvent) {
			VisualEvent srcComponent = (VisualEvent)src;
			setFaulty(srcComponent.isFaulty());
		}
	}

}
