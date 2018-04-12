package org.workcraft.gui;

import java.awt.Color;
import java.util.Arrays;
import java.util.Collection;

public class Coloriser {
    private static float[] comp1 = new float[4];
    private static float[] comp2 = new float[4];
    private static float[] comp3 = new float[4];

    private static float blend(float col, float orig) {
        return col + (1.0f - col) * orig * 0.8f;
    }

    public static Color colorise(Color originalColor, Color colorisation) {
        if (colorisation == null) {
            return originalColor;
        }
        originalColor.getComponents(comp1);
        colorisation.getComponents(comp2);
        comp3[0] = blend(comp2[0], comp1[0]);
        comp3[1] = blend(comp2[1], comp1[1]);
        comp3[2] = blend(comp2[2], comp1[2]);
        comp3[3] = comp1[3];
        return new Color(comp3[0], comp3[1], comp3[2], comp3[3]);
    }

    public static Color mix(Collection<Color> colors) {
        comp3[0] = 0.0f;
        comp3[1] = 0.0f;
        comp3[2] = 0.0f;
        comp3[3] = 1.0f;
        if ((colors != null) && !colors.isEmpty()) {
            int count = colors.size();
            for (Color color: colors) {
                color.getComponents(comp1);
                comp3[0] += comp1[0] / count;
                comp3[1] += comp1[1] / count;
                comp3[2] += comp1[2] / count;
            }
        }
        if (comp3[0] > 1.0f) comp3[0] = 1.0f;
        if (comp3[1] > 1.0f) comp3[1] = 1.0f;
        if (comp3[2] > 1.0f) comp3[2] = 1.0f;
        return new Color(comp3[0], comp3[1], comp3[2], comp3[3]);
    }

    public static Color mix(Color... colors) {
        return mix(Arrays.asList(colors));
    }

}
