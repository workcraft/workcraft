package org.workcraft.util;

import java.awt.Color;
import java.awt.color.ColorSpace;
import java.util.ArrayList;
import java.util.Collections;

public class CieColorUtils {

    public static float[] convertFromRgbToXyz(float[] value) {
        return ColorSpace.getInstance(ColorSpace.CS_CIEXYZ).fromRGB(value);
    }

    public static float[] convertFromXyzToRgb(float[] value) {
        return ColorSpace.getInstance(ColorSpace.CS_CIEXYZ).toRGB(value);
    }


    public static float[] convertFromRgbToLab(float[] value) {
        return convertFromXyzToLab(convertFromRgbToXyz(value));
    }

    public static float[] convertFromLabToRgb(float[] value) {
        return convertFromXyzToRgb(convertFromLabToXyz(value));
    }


    public static float[] convertFromXyzToLab(float[] value) {
        float l = f(value[1]);
        float L = 116.0f * l - 16.0f;
        float a = 500.0f * (f(value[0]) - l);
        float b = 200.0f * (l - f(value[2]));
        return new float[] {(float) L, (float) a, (float) b};
    }

    public static float[] convertFromLabToXyz(float[] value) {
    	float i = (value[0] + 16.0f) / 116.0f;
    	float X = fInv(i + value[1] / 500.0f);
    	float Y = fInv(i);
    	float Z = fInv(i - value[2] / 200.0f);
        return new float[] {(float) X, (float) Y, (float) Z};
    }

    private static float f(float x) {
        if (x > 216.0f / 24389.0f) {
            return (float) Math.cbrt(x);
        } else {
            return (841.0f / 108.0f) * x + 4.0f / 29.0f;
        }
    }

    private static float fInv(float x) {
        if (x > 6.0f / 29.0f) {
            return x * x * x;
        } else {
            return (108.0f / 841.0f) * (x - 4.0f / 29.0f);
        }
    }


    public static Color getXyzColor(float x, float y, float z) {
    	float[] rgb = convertFromXyzToRgb(new float[] {x, y, z});
    	return new Color(rgb[0], rgb[1], rgb[2]);
    }

    public static Color getLabColor(float L, float a, float b) {
    	float[] rgb = convertFromLabToRgb(new float[] {100.0f * L, 128.0f - 255.0f * a, 128.0f - 255.0f * b});
    	return new Color(rgb[0], rgb[1], rgb[2]);
    }

    public static Color[] getLabPalette(int LCount, int aCount, int bCount, float minL, float maxL) {
    	ArrayList<Color> palette = new ArrayList<>();
    	if ((maxL > minL) && (LCount > 0) && (aCount > 0) && (bCount > 0)) {
    		float dL = (maxL - minL) / LCount;
    		float da = 1.0f / aCount;
    		float db = 1.0f / bCount;
    		for (float L = maxL; L >= minL; L -= dL) {
    			for (float a = 0.0f; a <= 1.0f; a += da) {
    				for (float b = 0.0f; b <= 1.0f; b += db) {
    					palette.add(CieColorUtils.getLabColor(L, a, b));
    				}
    			}
    		}
    	}
   		Collections.shuffle(palette);
    	return palette.toArray(new Color[palette.size()]);
	}

    public static Color[] getLabHeightmapPalette(int LCount, float minL, float maxL, float a, float b) {
    	ArrayList<Color> palette = new ArrayList<>();
    	if ((maxL > minL) && (LCount > 0)) {
    		float dL = (maxL - minL) / LCount;
    		for (float L = maxL; L >= minL; L -= dL) {
    			palette.add(CieColorUtils.getLabColor(L, a, b));
    		}
    	}
    	return palette.toArray(new Color[palette.size()]);
	}

}
