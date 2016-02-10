package org.workcraft.dom.visual;

import java.awt.Graphics2D;

import org.workcraft.gui.graph.tools.Decoration;

public interface DrawRequest {
    Graphics2D getGraphics();
    Decoration getDecoration();
    VisualModel getModel();
}
