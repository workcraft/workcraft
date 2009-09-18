package org.workcraft.dom.visual;

import java.awt.Color;

import org.workcraft.dom.Node;

public interface Colorisable extends Node{
	 public void setColorisation (Color color);
	 public Color getColorisation ();
	 public void clearColorisation();
}