package org.workcraft.plugins.son.elements;

import java.awt.Color;

import org.workcraft.annotations.VisualClass;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.plugins.petri.Transition;
import org.workcraft.plugins.shared.CommonVisualSettings;

@VisualClass (org.workcraft.plugins.son.elements.VisualEvent.class)
public class Event extends Transition implements TransitionNode{

	private Color foregroundColor=CommonVisualSettings.getBorderColor();
	private Color fillColor = CommonVisualSettings.getFillColor();
	private String label="";
	private Boolean faulty = false;
	private String duration = "0000-9999";

	@Override
	public void setLabel(String label){
		this.label=label;
		sendNotification(new PropertyChangedEvent(this, "label"));
	}

	@Override
	public String getLabel(){
		return label;
	}

	@Override
	public Color getForegroundColor() {
		return foregroundColor;
	}

	@Override
	public void setFaulty(boolean fault){
		this.faulty = fault;
		sendNotification( new PropertyChangedEvent(this, "fault") );
	}

	@Override
	public boolean isFaulty(){
		return faulty;
	}

	@Override
	public void setForegroundColor(Color foregroundColor) {
		this.foregroundColor = foregroundColor;
		sendNotification(new PropertyChangedEvent(this, "foregroundColor"));
	}

	@Override
	public void setFillColor (Color fillColor){
		this.fillColor = fillColor;
		sendNotification(new PropertyChangedEvent(this, "fillColor"));
	}

	@Override
	public Color getFillColor() {
		return fillColor;
	}

	public void setDuration(String duration){
		this.duration = duration;
		sendNotification( new PropertyChangedEvent(this, "duration") );
	}

	public String getDuration(){
		return duration;
	}

}
