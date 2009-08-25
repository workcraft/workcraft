package org.workcraft.dom.visual;

import java.awt.Color;

import org.workcraft.dom.HierarchyNode;

public interface Colorisable extends HierarchyNode{
	 public void setColorisation (Color color);
	 public Color getColorisation ();
	 public void clearColorisation();
}