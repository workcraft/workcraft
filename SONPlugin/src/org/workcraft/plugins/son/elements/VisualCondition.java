package org.workcraft.plugins.son.elements;

import java.awt.Color;
import java.awt.event.KeyEvent;


import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.Hotkey;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.plugins.petri.VisualPlace;



@DisplayName("Condition")
@Hotkey(KeyEvent.VK_B)
@SVGIcon("images/icons/svg/place_empty.svg")
public class VisualCondition extends VisualPlace{

	public VisualCondition(Condition condition){
		super(condition);
		//addPropertyDeclarations();
	}
	/*
	private void addPropertyDeclarations() {
		//addPropertyDeclaration(new PropertyDeclaration (this, "Token (Condition)", "getToken", "setToken", boolean.class));
	}

	@Override
	public void draw(DrawRequest r){
		Graphics2D g = r.getGraphics();
		super.draw(r);
		Condition c = (Condition)getReferencedComponent();
		//drawToken(c.getToken(), singleTokenSize, Coloriser.colorise(getTokenColor(), r.getDecoration().getColorisation()), g);

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

	@NoAutoSerialisation
	public boolean getToken() {
		return getCondition().getToken();
	}

	@NoAutoSerialisation
	public void setToken(boolean b) {
		getCondition().setToken(b);
	}
	*/
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

	public Condition getCondition(){
		return (Condition)getReferencedComponent();
	}
}
