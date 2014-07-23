package org.workcraft.plugins.circuit.renderers;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.LinkedHashMap;
import java.util.Map;


public interface ComponentRenderingResult {
	public enum RenderType {
		BOX("Box"),
		GATE("Gate");

		private final String name;

		private RenderType(String name) {
			this.name = name;
		}

		static public Map<String, RenderType> getChoice() {
			LinkedHashMap<String, RenderType> choice = new LinkedHashMap<String, RenderType>();
			for (RenderType item : RenderType.values()) {
				choice.put(item.name, item);
			}
			return choice;
		}
	}

	Rectangle2D boundingBox();
	Map<String, Point2D> contactPositions();
	void draw(Graphics2D graphics);
}
