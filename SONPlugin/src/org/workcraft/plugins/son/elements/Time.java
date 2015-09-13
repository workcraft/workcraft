package org.workcraft.plugins.son.elements;


import org.workcraft.dom.Node;
import org.workcraft.plugins.son.Interval;

public interface Time extends Node{
	public void setStartTime (Interval value);
	public Interval getStartTime();

	public void setEndTime (Interval value);
	public Interval getEndTime();

	public void setDuration(Interval value);
	public Interval getDuration();
}
