package org.workcraft.plugins.son.components;

import java.awt.Color;

import org.workcraft.annotations.VisualClass;
import org.workcraft.dom.math.MathNode;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.plugins.shared.CommonVisualSettings;

@VisualClass (org.workcraft.plugins.son.components.VisualEvent.class)
public class Event extends MathNode {

	private Color foregroundColor=CommonVisualSettings.getBorderColor();
	private Color fillColor = CommonVisualSettings.getFillColor();
	private String label="";
	private Boolean faulty = false;

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

	public void setFaulty(boolean fault){
		this.faulty = fault;
		sendNotification( new PropertyChangedEvent(this, "fault") );
	}

	public boolean isFaulty(){
		return faulty;
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

}
