package org.workcraft.plugins.son.elements;

import java.awt.Color;

import org.workcraft.annotations.VisualClass;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.plugins.petri.Transition;
import org.workcraft.plugins.shared.CommonVisualSettings;
import org.workcraft.plugins.son.Time;

@VisualClass (org.workcraft.plugins.son.elements.VisualEvent.class)
public class Event extends Transition implements TransitionNode, Time{

	private Color foregroundColor=CommonVisualSettings.getBorderColor();
	private Color fillColor = CommonVisualSettings.getFillColor();
	private String label="";
	private Boolean faulty = false;

	private String statTime = "0000-9999";
	private String endTime = "0000-9999";
	private String duration = "0";

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

	public void setStartTime(String duration){
		this.statTime = duration;
		sendNotification( new PropertyChangedEvent(this, "start time") );
	}

	public String getStartTime(){
		return statTime;
	}

	public void setEndTime(String endTime){
		this.endTime = endTime;
		sendNotification( new PropertyChangedEvent(this, "end time") );
	}

	public String getEndTime(){
		return endTime;
	}

	public void setDuration(String duration){
		this.duration = duration;
		sendNotification( new PropertyChangedEvent(this, "duration") );
	}

	public String getDuration(){
		return duration;
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
}
