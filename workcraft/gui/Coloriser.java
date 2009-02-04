package org.workcraft.gui;

import java.awt.Color;

public class Coloriser {
	private static float comp1[] = new float[4];
	private static float comp2[] = new float[4];
	private static float comp3[] = new float[4];

	private static float blend (float col, float orig) {
		return col + (1.0f - col) * orig * 0.8f;
	}

	public static Color colorise (Color originalColor, Color colorisation) {
		if (colorisation == null)
			return originalColor;

		originalColor.getComponents(comp1);
		colorisation.getComponents(comp2);



		comp3[0] = blend (comp2[0], comp1[0]);
		comp3[1] = blend (comp2[1], comp1[1]);
		comp3[2] = blend (comp2[2], comp1[2]);
		comp3[3] = comp1[3];

		return new Color(comp3[0], comp3[1], comp3[2], comp3[3]);
	}
}
