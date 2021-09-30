package org.workcraft.plugins.circuit.renderers;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.Map;

public interface ComponentRenderingResult {

    enum RenderType {
        BOX("Box"),
        GATE("Gate");

        private final String name;

        RenderType(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    Rectangle2D boundingBox();
    Map<String, List<Point2D>> contactPositions();
    void draw(Graphics2D graphics);
}
