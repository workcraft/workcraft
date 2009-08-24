package org.workcraft.dom.visual;

import java.awt.Color;

public interface Colorisable extends HierarchyNode{
	 public void setColorisation (Color color);
	 public Color getColorisation ();
	 public void clearColorisation();
}