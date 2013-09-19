package org.workcraft.plugins.son.elements;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.KeyEvent;
import java.awt.font.GlyphVector;
import java.awt.geom.Ellipse2D;
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
import org.workcraft.plugins.son.SONSettings;
import org.workcraft.serialisation.xml.NoAutoSerialisation;



@DisplayName("Condition")
@Hotkey(KeyEvent.VK_B)
@SVGIcon("images/icons/svg/place_empty.svg")
public class VisualCondition extends VisualComponent{

	protected static double singleTokenSize = CommonVisualSettings.getSize() / 1.9;
	private Color tokenColor = CommonVisualSettings.getForegroundColor();
	private boolean displayName = false;

	public VisualCondition(Condition condition){
		super(condition);
		addPropertyDeclarations();
	}

	private void addPropertyDeclarations() {
		addPropertyDeclaration(new PropertyDeclaration (this, "Token", "hasToken", "setToken", boolean.class));
	}

	@Override
	public void draw(DrawRequest r)
	{
		Graphics2D g = r.getGraphics();

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

		Condition p = (Condition)getReferencedComponent();

		drawToken(p.hasToken(), singleTokenSize, Coloriser.colorise(getTokenColor(), r.getDecoration().getColorisation()), g);
		drawName(p.hasToken(), g, size, strokeWidth);

		drawLabelInLocalSpace(r);
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

	public void drawName(boolean token, Graphics2D g, double size, double strokeWidth){
		if (SONSettings.getDisplayName()) {
			GlyphVector glyphVector=null;
			Rectangle2D labelBB=null;

			Font labelFont = new Font("Sans-serif", Font.PLAIN, 1).deriveFont(0.4f);
			glyphVector = labelFont.createGlyphVector(g.getFontRenderContext(), getName());

			labelBB = glyphVector.getVisualBounds();
			Point2D labelPosition = new Point2D.Double(labelBB.getCenterX(), labelBB.getCenterY());
			if(!token) {
				g.drawGlyphVector(glyphVector, -(float)labelPosition.getX(), -(float)labelPosition.getY());
			} else {
				g.drawGlyphVector(glyphVector, -this.getLabelPositioning().dx, -this.getLabelPositioning().dy);
			}
		}
	}

	@NoAutoSerialisation
	public String getName(){
		return ((Condition)getReferencedComponent()).getName();
	}

	@NoAutoSerialisation
	public void setName(String name){
		((Condition)getReferencedComponent()).setName(name);
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
		return ((Condition)getReferencedComponent()).hasToken();
	}

	public void setToken(boolean b) {
		((Condition)getReferencedComponent()).setToken(b);
	}

	public Color getTokenColor() {
		return tokenColor;
	}

	public void setTokenColor(Color tokenColor) {
		this.tokenColor = tokenColor;
	}

	public Color getForegroundColor() {
		return ((Condition)getReferencedComponent()).getForegroundColor();
	}

	public void setForegroundColor(Color foregroundColor) {
		((Condition)getReferencedComponent()).setForegroundColor(foregroundColor);
	}

	public void setFillColor(Color fillColor){
		((Condition)getReferencedComponent()).setFillColor(fillColor);
	}

	public Color getFillColor(){
		return ((Condition)getReferencedComponent()).getFillColor();
	}

	public void setLabel(String label){
		super.setLabel(label);
		((Condition)getReferencedComponent()).setLabel(label);
	}

	public String getLabel(){
		super.getLabel();
		return ((Condition)getReferencedComponent()).getLabel();
	}

}
