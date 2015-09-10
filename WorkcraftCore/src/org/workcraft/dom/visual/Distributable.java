package org.workcraft.dom.visual;

import java.awt.geom.Rectangle2D;

public interface Distributable {
	public void setIsDistributed(boolean value);
	public boolean getIsDistributed();
	public Rectangle2D getOutlineBox();
}
