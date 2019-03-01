package org.workcraft.shared;

import java.awt.Color;

public class ColorGenerator {
    private final Color[] colors;
    private int index = 0;
    private Color color = Color.BLACK;

    public ColorGenerator(Color[] tokenColors) {
        this.colors = tokenColors;
    }

    public Color getColor() {
        return color;
    }

    public Color updateColor() {
        if ((colors != null) && (colors.length > 0)) {
            index++;
            if (index >= colors.length) {
                index = 0;
            }
            color = colors[index];
        }
        return color;
    }

}
