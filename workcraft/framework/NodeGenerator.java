package org.workcraft.framework;

import java.awt.geom.Point2D;

import javax.swing.Icon;

import org.workcraft.dom.visual.VisualModel;
import org.workcraft.framework.exceptions.NodeCreationException;

public interface NodeGenerator {
	public Icon getIcon();
	public String getLabel();
	public String getText();
	public void generate(VisualModel model, Point2D where) throws NodeCreationException;
	public int getHotKeyCode();
}
