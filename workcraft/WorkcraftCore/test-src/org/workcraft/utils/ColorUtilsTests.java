package org.workcraft.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.awt.*;

class ColorUtilsTests {

    @Test
    void fadeTest() {
        Color color = ColorUtils.getRandomColor();
        Assertions.assertEquals(Color.BLACK, ColorUtils.fade(color, 0.0));
        Assertions.assertEquals(color, ColorUtils.fade(color, 1.0));
    }

    @Test
    void coloriseTest() {
        Color color = ColorUtils.getRandomColor();
        Assertions.assertEquals(color, ColorUtils.colorise(color, null));
        Assertions.assertEquals(Color.BLACK, ColorUtils.colorise(Color.BLACK, Color.BLACK));
        Assertions.assertEquals(Color.WHITE, ColorUtils.colorise(Color.RED, Color.WHITE));
        Assertions.assertEquals(new Color(204, 255, 0), ColorUtils.colorise(Color.RED, Color.GREEN));
    }

    @Test
    void mixTest() {
        Assertions.assertEquals(Color.BLACK, ColorUtils.mix(Color.BLACK, Color.BLACK));
        Assertions.assertEquals(Color.GRAY, ColorUtils.mix(Color.BLACK, Color.WHITE));
        Assertions.assertEquals(new Color(85, 85, 85), ColorUtils.mix(Color.RED, Color.GREEN, Color.BLUE));
        Assertions.assertEquals(new Color(128, 128, 0), ColorUtils.mix(Color.RED, Color.GREEN));
    }

    @Test
    void labColorTest() {
        Assertions.assertEquals(new Color(174, 168, 188),
                ColorUtils.getLabColor(0.7f, 0.5f, 0.5f));
    }

    @Test
    void getHsbPaletteTest() {
        float[] hs = new float[1];
        float[] ss = new float[2];
        float[] bs = new float[3];
        Assertions.assertEquals(6, ColorUtils.getHsbPalette(hs, ss, bs).length);
    }

    @Test
    void hexRGBTest() {
        Assertions.assertEquals("#ffffff", ColorUtils.getHexRGB(Color.WHITE));
        Assertions.assertEquals("#ff0000", ColorUtils.getHexRGB(Color.RED));
        Assertions.assertEquals("#00ff00", ColorUtils.getHexRGB(Color.GREEN));
        Assertions.assertEquals("#0000ff", ColorUtils.getHexRGB(Color.BLUE));
    }

    @Test
    void hexARGBTest() {
        Assertions.assertEquals("#ffffffff", ColorUtils.getHexARGB(Color.WHITE));
        Assertions.assertEquals("#ffff0000", ColorUtils.getHexARGB(Color.RED));
        Assertions.assertEquals("#ff00ff00", ColorUtils.getHexARGB(Color.GREEN));
        Assertions.assertEquals("#ff0000ff", ColorUtils.getHexARGB(Color.BLUE));
    }

}
