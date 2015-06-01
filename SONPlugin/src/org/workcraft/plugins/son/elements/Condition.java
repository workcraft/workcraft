package org.workcraft.plugins.son.elements;

import java.awt.Color;

import org.workcraft.annotations.VisualClass;
import org.workcraft.observation.PropertyChangedEvent;


@VisualClass (org.workcraft.plugins.son.elements.VisualCondition.class)
public class Condition extends PlaceNode{

	private String statTime = "0000-9999";
	private String endTime = "0000-9999";
	private boolean initialState = false;
	private boolean finalState = false;
	protected Color startTimeColor = Color.BLACK;
	protected Color endTimeColor = Color.BLACK;

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

	public boolean isInitial() {
		return initialState;
	}

	public void setInitial(boolean value) {
		initialState = value;
		sendNotification(new PropertyChangedEvent(this, "initial"));
	}

	public boolean isFinal() {
		return finalState;
	}

	public void setFinal(boolean value) {
		finalState = value;
		sendNotification(new PropertyChangedEvent(this, "final"));
	}


	public Color getStartTimeColor(){
		return startTimeColor;
	}

	public void setStartTimeColor(Color value){
		startTimeColor = value;
	}

	public Color getEndTimeColor(){
		return endTimeColor;
	}

	public void setEndTimeColor(Color value){
		endTimeColor = value;
	}
}
