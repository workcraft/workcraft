package org.workcraft.plugins.son.elements;

import java.awt.Color;
import java.awt.event.KeyEvent;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.Hotkey;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.plugins.petri.VisualTransition;

@Hotkey(KeyEvent.VK_E)
@DisplayName ("Event")
@SVGIcon("images/icons/svg/transition.svg")

public class VisualEvent extends VisualTransition{

	public VisualEvent(Event event) {
		super(event);
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
