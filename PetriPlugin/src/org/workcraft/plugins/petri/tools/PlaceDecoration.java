package org.workcraft.plugins.petri.tools;

import java.awt.Color;

import org.workcraft.gui.tools.Decoration;

public interface PlaceDecoration extends Decoration {
    int getTokens();
    Color getTokenColor();
}
