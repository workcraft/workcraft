package org.workcraft.plugins.son.elements;

import java.awt.Color;

import org.workcraft.dom.math.MathNode;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.plugins.shared.CommonVisualSettings;
import org.workcraft.plugins.son.Interval;

public class PlaceNode extends MathNode implements Time{

	private Color foregroundColor=CommonVisualSettings.getBorderColor();
	private Color fillColor = CommonVisualSettings.getFillColor();
	private String label = "";
	private int errors = 0;

	private Interval statTime = new Interval(0000, 9999);
	private Interval endTime = new Interval(0000, 9999);
	private Interval duration = new Interval(0000, 9999);

	protected Color durationColor = Color.BLACK;

	private boolean marked = false;
	private Color tokenColor = CommonVisualSettings.getBorderColor();

	public void setMarked(boolean token){
		this.marked=token;
		sendNotification( new PropertyChangedEvent(this, "marked") );
	}

	public boolean isMarked() {
		return marked;
	}

	public void setErrors(int errors){
		this.errors = errors;
		sendNotification( new PropertyChangedEvent(this, "errors") );
	}

	public int getErrors(){
		return errors;
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

	public void setLabel(String label){
		this.label=label;
		sendNotification(new PropertyChangedEvent(this, "label"));
	}

	public String getLabel(){
		return label;
	}

	public Color getTokenColor() {
		return tokenColor;
	}

	public void setStartTime(Interval duration){
		this.statTime = duration;
		sendNotification( new PropertyChangedEvent(this, "start time") );
	}

	public Interval getStartTime(){
		return statTime;
	}

	public void setEndTime(Interval endTime){
		this.endTime = endTime;
		sendNotification( new PropertyChangedEvent(this, "end time") );
	}

	public Interval getEndTime(){
		return endTime;
	}

	public void setDuration(Interval duration){
		this.duration = duration;
		sendNotification( new PropertyChangedEvent(this, "duration") );
	}

	public Interval getDuration(){
		return duration;
	}

	public Color getDurationColor(){
		return durationColor;
	}

	public void setDurationColor(Color value){
		this.durationColor = value;
	}

	public void setTokenColor(Color tokenColor) {
		this.tokenColor = tokenColor;
		sendNotification(new PropertyChangedEvent(this, "tokenColor"));
	}
}
