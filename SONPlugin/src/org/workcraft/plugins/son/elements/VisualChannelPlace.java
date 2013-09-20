package org.workcraft.plugins.son.elements;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
//import java.awt.event.KeyEvent;
import java.awt.font.GlyphVector;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.workcraft.annotations.DisplayName;
//import org.workcraft.annotations.Hotkey;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.gui.Coloriser;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.plugins.shared.CommonVisualSettings;
import org.workcraft.plugins.son.SONSettings;

@DisplayName("ChannelPlace")
//@Hotkey(KeyEvent.VK_P)
@SVGIcon("images/icons/svg/channel-place.svg")
public class VisualChannelPlace extends VisualComponent {

	protected static double singleTokenSize = CommonVisualSettings.getSize() / 1.9;
	private Color tokenColor = CommonVisualSettings.getForegroundColor();
	private boolean displayName = false;

	public VisualChannelPlace(ChannelPlace cplace) {
		super(cplace);
		addPropertyDeclarations();
	}

	private void addPropertyDeclarations() {
		addPropertyDeclaration(new PropertyDeclaration (this, "Token", "hasToken", "setToken", boolean.class));
	}

	@Override
	public void draw(DrawRequest r){
		Graphics2D g = r.getGraphics();

		drawLabelInLocalSpace(r);

		double size = CommonVisualSettings.getSize()*1.2;
		double strokeWidth = CommonVisualSettings.getStrokeWidth()*2.0;

		Shape shape = new Ellipse2D.Double(
				-size / 2 + strokeWidth / 2,
				-size / 2 + strokeWidth / 2,
				size - strokeWidth,
				size - strokeWidth);

		g.setColor(Coloriser.colorise(getFillColor(), r.getDecoration().getColorisation()));
		g.fill(shape);
		g.setColor(Coloriser.colorise(getForegroundColor(), r.getDecoration().getColorisation()));
		g.setStroke(new BasicStroke((float)strokeWidth));
		g.draw(shape);

		ChannelPlace c = (ChannelPlace)getReferencedComponent();
		drawToken(c.hasToken(), singleTokenSize, Coloriser.colorise(getTokenColor(), r.getDecoration().getColorisation()), g);
		drawName(r);
	}

	public static void drawToken (boolean b, double singleTokenSize, Color tokenColor,	Graphics2D g) {
		if(b){
		Shape shape;
			shape = new Ellipse2D.Double(
					-singleTokenSize / 2,
					-singleTokenSize / 2,
					singleTokenSize,
					singleTokenSize);
			g.setColor(tokenColor);
			g.fill(shape);
		}
	}

	public void drawName(DrawRequest r){
		if (SONSettings.getDisplayName()) {
			Graphics2D g = r.getGraphics();
			GlyphVector glyphVector=null;
			Rectangle2D labelBB=null;

			Font labelFont = new Font("Sans-serif", Font.PLAIN, 1).deriveFont(0.4f);
			String name = r.getModel().getMathModel().getNodeReference(getReferencedComponent());
			if (name != null) {
				glyphVector = labelFont.createGlyphVector(g.getFontRenderContext(), name);

				labelBB = glyphVector.getVisualBounds();
				Point2D labelPosition = new Point2D.Double(labelBB.getCenterX(), labelBB.getCenterY());
				if(!this.hasToken()) {
					g.drawGlyphVector(glyphVector, -(float)labelPosition.getX(), -(float)labelPosition.getY());
				} else {
					g.drawGlyphVector(glyphVector, -this.getLabelPositioning().dx, -this.getLabelPositioning().dy);
				}
			}
		}
	}

	public void setDisplayName(boolean showName){
		this.displayName = showName;
	}

	public boolean isDisplayName(){
		return displayName;
	}

	public boolean hitTestInLocalSpace(Point2D pointInLocalSpace)
	{
		return pointInLocalSpace.distanceSq(0, 0) < size * size / 4;
	}

	public boolean hasToken() {
		return ((ChannelPlace)getReferencedComponent()).hasToken();
	}

	public void setToken(boolean b) {
		((ChannelPlace)getReferencedComponent()).setToken(b);
	}

	public Color getTokenColor() {
		return tokenColor;
	}

	public void setTokenColor(Color tokenColor) {
		this.tokenColor = tokenColor;
	}

	public Color getForegroundColor() {
		return ((ChannelPlace)getReferencedComponent()).getForegroundColor();
	}

	public void setForegroundColor(Color foregroundColor) {
		((ChannelPlace)getReferencedComponent()).setForegroundColor(foregroundColor);
	}

	public void setFillColor(Color fillColor){
		((ChannelPlace)getReferencedComponent()).setFillColor(fillColor);
	}

	public Color getFillColor(){
		return ((ChannelPlace)getReferencedComponent()).getFillColor();
	}

	public void setLabel(String label){
		super.setLabel(label);
		((ChannelPlace)getReferencedComponent()).setLabel(label);
	}

	public String getLabel(){
		super.getLabel();
		return ((ChannelPlace)getReferencedComponent()).getLabel();
	}
}
