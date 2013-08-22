package org.workcraft.plugins.son.elements;

import java.awt.Color;

import org.workcraft.annotations.VisualClass;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.plugins.petri.Transition;
import org.workcraft.plugins.shared.CommonVisualSettings;

@VisualClass (org.workcraft.plugins.son.elements.VisualEvent.class)
public class Event extends Transition{

	private Color foregroundColor=CommonVisualSettings.getForegroundColor();
	private Color fillColor = CommonVisualSettings.getFillColor();
	private String label="";

	public enum State {
		UNCHECKED,
		ERROR_RELATION,
		ERROR_CYCLE,
		OK
	}

	private State state = State.UNCHECKED;

	public void setLabel(String label){
		this.label=label;
		sendNotification(new PropertyChangedEvent(this, "label"));
	}

	public String getLabel(){
		return label;
	}

	public Color getForegroundColor() {
		return foregroundColor;
	}

	public void setForegroundColor(Color foregroundColor) {
		this.foregroundColor = foregroundColor;
		sendNotification(new PropertyChangedEvent(this, "foregroundColor"));
	}

	public void setFillColor (Color fillColor){
		this.fillColor = fillColor;
		sendNotification(new PropertyChangedEvent(this, "fillColor"));
	}

	public Color getFillColor() {
		return fillColor;
	}

	public State getState() {
		return state;
	}

	public void setState(State state) {
		this.state = state;
	}

}
