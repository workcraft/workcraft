package org.workcraft.gui;

import java.awt.Color;

public class Coloriser {
	private static float comp1[] = new float[4];
	private static float comp2[] = new float[4];
	private static float comp3[] = new float[4];

	public static Color colorise (Color originalColor, Color colorisation) {
		if (colorisation == null)
			return originalColor;

		originalColor.getComponents(comp1);
		colorisation.getComponents(comp2);

		comp3[0] = Math.min( comp1[0] * 0.75f + comp2[0] * 0.75f, 1.0f) ;
		comp3[1] = Math.min( comp1[1] * 0.75f + comp2[1] * 0.75f, 1.0f) ;
		comp3[2] = Math.min( comp1[2] * 0.75f + comp2[2] * 0.75f, 1.0f) ;
		comp3[3] = comp1[3];

		return new Color(comp3[0], comp3[1], comp3[2], comp3[3]);
	}
}
