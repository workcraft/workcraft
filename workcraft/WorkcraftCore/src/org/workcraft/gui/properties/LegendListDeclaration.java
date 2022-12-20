package org.workcraft.gui.properties;

import java.awt.*;

public class LegendListDeclaration extends PropertyDeclaration<LegendList> {

    private final LegendList legendList = new LegendList();

    public LegendListDeclaration() {
        this(null);
    }

    public LegendListDeclaration(String name) {
        super(LegendList.class, name, value -> { }, () -> null);
    }

    public LegendListDeclaration addLegend(String title, Color background) {
        return addLegend(title, null, null, background, null);
    }

    public LegendListDeclaration addLegend(String title, String tooltip, Color foreground, Color background, Runnable runnable) {
        legendList.add(new Legend(title, tooltip, foreground, background, runnable));
        return this;
    }

    @Override
    public final LegendList getValue() {
        return legendList;
    }

    @Override
    public boolean isSpan() {
        return getName() == null;
    }

}
