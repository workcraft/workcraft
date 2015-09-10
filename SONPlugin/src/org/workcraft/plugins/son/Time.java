package org.workcraft.plugins.son;


import org.workcraft.dom.Node;

public interface Time extends Node{
	public void setStartTime (String value);
	public String getStartTime();

	public void setEndTime (String value);
	public String getEndTime();

	public void setDuration(String value);
	public String getDuration();
}
