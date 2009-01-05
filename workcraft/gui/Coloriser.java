package org.workcraft.gui;

import java.awt.Color;

public class Coloriser {
	private static float comp1[] = new float[4];
	private static float comp2[] = new float[4];
	private static float comp3[] = new float[4];

	public static Color colorise (Color originalColor, Color colorisation) {
		originalColor.getComponents(comp1);
		colorisation.getComponents(comp2);
		comp3[0] = (comp1[0] + comp2[0]) * 0.5f;
		comp3[1] = (comp1[1] + comp2[1]) * 0.5f;
		comp3[2] = (comp1[2] + comp2[2]) * 0.5f;
		comp3[3] = (comp1[3] + comp2[3]) * 0.5f;
		return new Color(comp3[0], comp3[1], comp3[2], comp3[3]);
	}
}
