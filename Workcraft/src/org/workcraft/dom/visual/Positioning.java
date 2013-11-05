package org.workcraft.dom.visual;

import java.util.LinkedHashMap;
import java.util.Map;

public enum Positioning {
	TOP("Top", 0.0, -1.3),
	BOTTOM("Bottom", 0.0, 1.3),
	LEFT("Left", -1.3, 0.0),
	RIGHT("Right", 1.3, 0.0),
	CENTER("Center", 0.0, 0.0),
	TOP_LEFT("Top-Left", -1.1, -1.1),
	TOP_RIGHT("Top-Right", 1.1, -1.1),
	BOTTOM_LEFT("Bottom-Left", -1.1, 1.1),
	BOTTOM_RIGHT("Bottom-Right", 1.1, 1.1);

	public final String name;
	public final double xOffset;
	public final double yOffset;
	public final int xSign;
	public final int ySign;

	private Positioning(String name, double xOffset, double yOffset) {
		this.name = name;
		this.xOffset = xOffset;
		this.yOffset = yOffset;
		this.xSign = (xOffset == 0.0) ? 0 : (xOffset > 0.0) ? 1 : -1;
		this.ySign = (yOffset == 0.0) ? 0 : (yOffset > 0.0) ? 1 : -1;
	}

	static public Map<String, Positioning> getChoice() {
		LinkedHashMap<String, Positioning> choice = new LinkedHashMap<String, Positioning>();
		for (Positioning item : Positioning.values()) {
			choice.put(item.name, item);
		}
		return choice;
	}

}
