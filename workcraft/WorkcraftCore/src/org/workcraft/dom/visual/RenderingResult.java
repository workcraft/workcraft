package org.workcraft.dom.visual;

import java.awt.*;
import java.awt.geom.Rectangle2D;

public interface RenderingResult {
    void draw(Graphics2D g);
    Rectangle2D getBoundingBox();
}
