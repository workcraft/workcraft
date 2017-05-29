package org.workcraft.gui.layouts;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;

import javax.swing.JLabel;

public class SimpleFlowLayout implements LayoutManager {
    @SuppressWarnings("serial")
    public static class LineBreak extends JLabel {
        public int gap;
        public LineBreak(int gap) {
            super();
            setVisible(false);
            this.gap = gap;
        }

        public LineBreak() {
            this(0);
        }
    }

    //public static final LineBreak BR = new LineBreak();

    private int hgap;
    private int vgap;
    private boolean applyLayout;

    public SimpleFlowLayout() {
        this(5, 5);
    }

    public SimpleFlowLayout(int hgap, int vgap) {
        this.hgap = hgap;
        this.vgap = vgap;
    }

    public int getHgap() {
        return hgap;
    }

    public void setHgap(int hgap) {
        this.hgap = hgap;
    }

    public int getVgap() {
        return vgap;
    }

    public void setVgap(int vgap) {
        this.vgap = vgap;
    }

    public void addLayoutComponent(String name, Component comp) {
    }

    public void removeLayoutComponent(Component comp) {
    }

    public Dimension preferredLayoutSize(Container target) {
        synchronized (target.getTreeLock()) {
            applyLayout = false;
            return doLayout(target);
        }
    }

    public Dimension minimumLayoutSize(Container target) {
        return new Dimension(0, 0);
    }

    public void layoutContainer(Container target) {
        applyLayout = true;
        doLayout(target);
    }

    private Dimension doLayout(Container target) {
        synchronized (target.getTreeLock()) {
            Insets insets = target.getInsets();
            int maxwidth = target.getWidth() - (insets.left + insets.right + hgap * 2);
            int nmembers = target.getComponentCount();

            int x = insets.left, y = insets.top + vgap;
            int rowh = 0;

            for (int i = 0; i < nmembers; i++) {
                Component m = target.getComponent(i);

                if (m instanceof LineBreak) {
                    x = insets.left;
                    y += vgap + rowh + ((LineBreak) m).gap;
                    rowh = 0;
                    continue;
                }

                if (m.isVisible()) {
                    Dimension d = m.getPreferredSize();

                    if (applyLayout) {
                        m.setSize(d.width, d.height);
                    }

                    if (x > insets.left) {
                        x += hgap;
                    }

                    if (x + d.width >= maxwidth) {
                        x = insets.left;
                        y += vgap + rowh;
                        rowh = d.height;
                    } else {
                        rowh = Math.max(rowh, d.height);
                    }

                    if (applyLayout) {
                        m.setLocation(x, y);
                    }

                    x += d.width;
                }
            }

            return new Dimension(maxwidth, y + rowh + (rowh != 0 ? vgap : 0));
        }
    }
}
