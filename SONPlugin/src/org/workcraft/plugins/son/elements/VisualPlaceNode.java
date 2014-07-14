package org.workcraft.plugins.son.elements;

import org.workcraft.dom.Node;


public interface VisualPlaceNode extends Node{

	public void setInterface(String value);

	public String getInterface();

	public boolean isMarked();

	public void setMarked(boolean b);
}
