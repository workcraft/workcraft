package org.workcraft.plugins.son.elements;

import java.awt.Color;

import org.workcraft.annotations.VisualClass;
import org.workcraft.dom.math.MathNode;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.plugins.shared.CommonVisualSettings;

@VisualClass (org.workcraft.plugins.son.elements.VisualChannelPlace.class)
public class ChannelPlace extends MathNode {

	private String label="";
	private Color foregroundColor=CommonVisualSettings.getBorderColor();
	private Color fillColor = CommonVisualSettings.getFillColor();
	protected boolean token=false;
	private int errors = 0;

	public void setLabel(String label){
		this.label=label;
		sendNotification(new PropertyChangedEvent(this, "label"));
	}

	public String getLabel(){
		return label;
	}

	public boolean isMarked() {
		return token;
	}

	public void setErrors(int errors){
		this.errors = errors;
	}

	public int getErrors(){
		return errors;
	}

	public void setMarked(boolean token) {
		this.token=token;
		sendNotification( new PropertyChangedEvent(this, "marked") );
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

}
