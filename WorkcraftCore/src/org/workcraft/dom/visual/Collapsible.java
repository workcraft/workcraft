package org.workcraft.dom.visual;

public interface Collapsible {
	public void setIsCollapsed(boolean isCollapsed);
	public boolean getIsCollapsed();

	public void setIsCurrentLevelInside(boolean isInside);
	public boolean isCurrentLevelInside();
}
