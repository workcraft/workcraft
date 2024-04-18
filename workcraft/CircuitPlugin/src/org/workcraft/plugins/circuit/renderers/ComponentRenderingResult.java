package org.workcraft.plugins.circuit.renderers;

import org.workcraft.dom.visual.RenderingResult;

import java.awt.geom.Point2D;
import java.util.List;
import java.util.Map;

public interface ComponentRenderingResult extends RenderingResult {

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

    Map<String, List<Point2D>> getContactPositions();

}
