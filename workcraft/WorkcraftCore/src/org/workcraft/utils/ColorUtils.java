package org.workcraft.utils;

import java.awt.*;
import java.awt.color.ColorSpace;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Random;

public class ColorUtils {

    private static final Random RANDOM = new Random();
    private static final float[] COMP_1 = new float[4];
    private static final float[] COMP_2 = new float[4];
    private static final float[] COMP_3 = new float[4];

    public static Color getRandomColor() {
        return new Color(RANDOM.nextInt(256), RANDOM.nextInt(256), RANDOM.nextInt(256));
    }

    public static Color fade(Color color, double factor) {
        int r = (int) (color.getRed() * factor);
        int g = (int) (color.getGreen() * factor);
        int b = (int) (color.getBlue() * factor);
        return new Color(r, g, b);
    }

    private static float blend(float col, float orig) {
        return col + (1.0f - col) * orig * 0.8f;
    }

    public static Color colorise(Color originalColor, Color colorisation) {
        if (colorisation == null) {
            return originalColor;
        }
        originalColor.getComponents(COMP_1);
        colorisation.getComponents(COMP_2);
        COMP_3[0] = blend(COMP_2[0], COMP_1[0]);
        COMP_3[1] = blend(COMP_2[1], COMP_1[1]);
        COMP_3[2] = blend(COMP_2[2], COMP_1[2]);
        COMP_3[3] = COMP_1[3];
        return new Color(COMP_3[0], COMP_3[1], COMP_3[2], COMP_3[3]);
    }

    public static Color mix(Collection<Color> colors) {
        COMP_3[0] = 0.0f;
        COMP_3[1] = 0.0f;
        COMP_3[2] = 0.0f;
        COMP_3[3] = 1.0f;
        if ((colors != null) && !colors.isEmpty()) {
            int count = colors.size();
            for (Color color: colors) {
                color.getComponents(COMP_1);
                COMP_3[0] += COMP_1[0] / count;
                COMP_3[1] += COMP_1[1] / count;
                COMP_3[2] += COMP_1[2] / count;
            }
        }
        if (COMP_3[0] > 1.0f) COMP_3[0] = 1.0f;
        if (COMP_3[1] > 1.0f) COMP_3[1] = 1.0f;
        if (COMP_3[2] > 1.0f) COMP_3[2] = 1.0f;
        return new Color(COMP_3[0], COMP_3[1], COMP_3[2], COMP_3[3]);
    }

    public static Color mix(Color... colors) {
        return mix(Arrays.asList(colors));
    }

    public static Color getLabColor(float l, float a, float b) {
        float[] lab = {100.0f * l, 128.0f - 255.0f * a, 128.0f - 255.0f * b};
        float[] xyz = convertFromLabToXyz(lab);
        float[] rgb = convertFromXyzToRgb(xyz);
        return new Color(rgb[0], rgb[1], rgb[2]);
    }

    public static float[] convertFromLabToXyz(float[] value) {
        float i = (value[0] + 16.0f) / 116.0f;
        float x = fInv(i + value[1] / 500.0f);
        float y = fInv(i);
        float z = fInv(i - value[2] / 200.0f);
        return new float[] {x, y, z};
    }

    private static float fInv(float x) {
        if (x > 6.0f / 29.0f) {
            return x * x * x;
        } else {
            return 108.0f / 841.0f * (x - 4.0f / 29.0f);
        }
    }

    private static float[] convertFromXyzToRgb(float[] value) {
        return ColorSpace.getInstance(ColorSpace.CS_CIEXYZ).toRGB(value);
    }

    public static Color[] getHsbPalette(float[] hs, float[] ss, float[] bs) {
        ArrayList<Color> palette = new ArrayList<>();
        for (float b: bs) {
            for (float s: ss) {
                for (float h: hs) {
                    Color color = Color.getHSBColor(h, s, b);
                    palette.add(color);
                }
            }
        }
        return palette.toArray(new Color[0]);
    }

    public static Boolean isOpaque(Color color) {
        return color.getAlpha() == 0xff;
    }

    public static String getHexRGB(Color color) {
        return String.format("#%06x",  color.getRGB() & 0xffffff);
    }

    public static String getHexARGB(Color color) {
        return String.format("#%08x", color.getRGB());
    }

}
