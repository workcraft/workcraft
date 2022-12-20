package org.workcraft.gui.properties;

import java.awt.*;

public class Legend implements Runnable {

    private final String title;
    private final String tooltip;
    private final Color foreground;
    private final Color background;
    private final Runnable runnable;

    public Legend(String title, String tooltip, Color foreground, Color background, Runnable runnable) {
        this.title = title;
        this.tooltip = tooltip;
        this.foreground = foreground;
        this.background = background;
        this.runnable = runnable;
    }

    public String getTitle() {
        return title;
    }

    public String getTooltip() {
        return tooltip;
    }

    public Color getForeground() {
        return foreground;
    }

    public Color getBackground() {
        return background;
    }

    @Override
    public void run() {
        if (runnable != null) {
            runnable.run();
        }
    }

}
