package org.workcraft.gui.layouts;

import org.workcraft.exceptions.NotSupportedException;

import java.awt.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serial;
import java.io.Serializable;
import java.util.function.Function;

public class SmartFlowLayout implements LayoutManager, Serializable {
    public static final int LEFT     = 0;
    public static final int CENTER   = 1;
    public static final int RIGHT    = 2;
    public static final int LEADING  = 3;
    public static final int TRAILING = 4;
    private static final int MAX_DIMENSION = 1_000_000;

    private int align;          // This is for 1.1 serialization compatibility
    private int newAlign;       // This is the one we actually use
    private int hgap;
    private int vgap;

    private boolean alignOnBaseline;

    private boolean applyLayout;

    @Serial
    private static final long serialVersionUID = -7262534875583282631L;

    public SmartFlowLayout() {
        this(CENTER, 5, 5);
    }

    public SmartFlowLayout(int align) {
        this(align, 5, 5);
    }

    public SmartFlowLayout(int align, int hgap, int vgap) {
        this.hgap = hgap;
        this.vgap = vgap;
        setAlignment(align);
    }

    public int getAlignment() {
        return newAlign;
    }

    public void setAlignment(int align) {
        this.newAlign = align;

        switch (align) {
            case LEADING -> this.align = LEFT;
            case TRAILING -> this.align = RIGHT;
            default -> this.align = align;
        }
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

    public void setAlignOnBaseline(boolean alignOnBaseline) {
        this.alignOnBaseline = alignOnBaseline;
    }

    public boolean getAlignOnBaseline() {
        return alignOnBaseline;
    }

    @Override
    public void addLayoutComponent(String name, Component comp) {
    }

    @Override
    public void removeLayoutComponent(Component comp) {
    }

    @Override
    public Dimension preferredLayoutSize(Container target) {
        synchronized (target.getTreeLock()) {
            applyLayout = false;
            return doLayout(target);
        }
    }

    @Override
    public Dimension minimumLayoutSize(Container target) {
        return preferredLayoutSize(target);
    }

    private void moveComponents(Container target, int x, int y, int remainingWidth, int height,
            int rowStart, int rowEnd, boolean ltr) {

        switch (newAlign) {
            case LEFT -> x += ltr ? 0 : remainingWidth;
            case CENTER -> x += remainingWidth / 2;
            case RIGHT -> x += ltr ? remainingWidth : 0;
            case TRAILING -> x += remainingWidth;
            default -> { }
        }
        for (int i = rowStart; i < rowEnd; i++) {
            Component m = target.getComponent(i);
            if (m.isVisible()) {
                int cy = y + (height - m.getHeight()) / 2;
                if (ltr) {
                    m.setLocation(x, cy);
                } else {
                    m.setLocation(target.getWidth() - x - m.getWidth(), cy);
                }
                x += m.getWidth() + hgap;
            }
        }
    }

    @Override
    public void layoutContainer(Container target) {
        applyLayout = true;
        doLayout(target);
    }

    private Dimension doLayout(Container target) {
        synchronized (target.getTreeLock()) {
            boolean useBaseline = getAlignOnBaseline();
            if (useBaseline) {
                throw new NotSupportedException("BaseLine is not supported.");
            }

            Insets insets = target.getInsets();
            int maxwidth = target.getWidth() - (insets.left + insets.right + hgap * 2);
            int nmembers = target.getComponentCount();
            int x = 0;
            int y = insets.top + vgap;
            int rowh = 0;
            int start = 0;
            boolean ltr = target.getComponentOrientation().isLeftToRight();
            for (int i = 0; i < nmembers; i++) {
                Component m = target.getComponent(i);
                if (m.isVisible()) {
                    Dimension d = m.getPreferredSize();

                    if (applyLayout) {
                        m.setSize(d.width, d.height);
                    }

                    if (x > 0) {
                        x += hgap;
                    }
                    x += d.width;
                    rowh = Math.max(rowh, d.height);

                    if (x >= maxwidth) {
                        if (fit(target, insets.left + hgap, y, maxwidth, rowh, start, i + 1, ltr)) {
                            start = i + 1;
                            x = 0;
                            y += vgap + rowh;
                            rowh = 0;
                        } else {
                            int end = i;
                            if (start == end) {
                                end++;
                            }
                            stretch(target, insets.left + hgap, y, maxwidth, rowh, start, end, ltr);
                            start = end;
                            x = d.width;
                            y += vgap + rowh;
                            rowh = d.height;
                        }
                    }
                }
            }
            //moveComponents(target, insets.left + hgap, y, 0, rowh, start, nmembers, ltr);
            stretch(target, insets.left + hgap, y, maxwidth, rowh, start, nmembers, ltr);

            return new Dimension(maxwidth, y + rowh + (rowh != 0 ? vgap : 0));
        }
    }

    private void stretch(Container target, int x, int y, int width, int height, int start, int end, boolean ltr) {
        resize(target, start, end, width, c -> {
            Dimension size = c.getMaximumSize();
            return new Dimension(Math.min(MAX_DIMENSION, size.width), Math.min(MAX_DIMENSION, size.height));
        });
        moveComponents(target, x, y, 0, height, start, end, ltr);
    }

    private boolean fit(Container target, int x, int y, int width, int height, int start, int end, boolean ltr) {
        if (!resize(target, start, end, width, Component::getMinimumSize)) {
            return false;
        } else {
            if (applyLayout) {
                moveComponents(target, x, y, 0, height, start, end, ltr);
            }
            return true;
        }
    }

    private boolean resize(Container target, int start, int end, int width,
            Function<Component, Dimension> extremeProvider) {

        int totalFreedom = 0;
        int extremeWidth = 0;
        for (int i = start; i < end; i++) {
            Component component = target.getComponent(i);
            if (component.isVisible()) {
                Dimension extreme = extremeProvider.apply(component);
                Dimension pref = component.getPreferredSize();
                if (i > start) {
                    extremeWidth += hgap;
                }
                totalFreedom += extreme.width - pref.width;
                extremeWidth += extreme.width;
            }
        }

        int totalChange = width - (extremeWidth - totalFreedom);

        boolean failed;
        if ((double) totalChange / totalFreedom > 1.0) {
            failed = true;
            totalChange = totalFreedom;
        } else {
            failed = false;
        }

        for (int i = start; i < end; i++) {
            Component component = target.getComponent(i);
            if (component.isVisible() && totalFreedom != 0) {
                Dimension extreme = extremeProvider.apply(component);
                Dimension pref = component.getPreferredSize();

                int freedom = extreme.width - pref.width;

                int change = totalChange * freedom / totalFreedom;

                totalFreedom -= freedom;
                totalChange -= change;

                if (applyLayout) {
                    component.setSize(pref.width + change, pref.height);
                }
            }
        }

        return !failed;
    }

    //
    // the internal serial version which says which version was written
    // - 0 (default) for versions before the Java 2 platform, v1.2
    // - 1 for version >= Java 2 platform v1.2, which includes "newAlign" field
    //
    private static final int currentSerialVersion = 1;
    /**
     * This represent the <code>currentSerialVersion</code>
     * which is bein used.  It will be one of two values :
     * <code>0</code> versions before Java 2 platform v1.2.
     * <code>1</code> versions after  Java 2 platform v1.2.
     *
     * @serial
     * @since 1.2
     */
    private int serialVersionOnStream = currentSerialVersion;

    /**
     * Reads this object out of a serialization stream, handling
     * objects written by older versions of the class that didn't contain
     * all the fields we use now.
     */
    @Serial
    private void readObject(ObjectInputStream stream)
            throws IOException, ClassNotFoundException {

        stream.defaultReadObject();

        if (serialVersionOnStream < 1) {
            // "newAlign" field wasn't present, so use the old "align" field.
            setAlignment(this.align);
        }
        serialVersionOnStream = currentSerialVersion;
    }

    /**
     * Returns a string representation of this <code>FlowLayout</code>
     * object and its values.
     * @return     a string representation of this layout
     */
    @Override
    public String toString() {
        String str = "";
        switch (align) {
            case LEFT -> str = ",align=left";
            case CENTER -> str = ",align=center";
            case RIGHT -> str = ",align=right";
            case LEADING -> str = ",align=leading";
            case TRAILING -> str = ",align=trailing";
            default -> { }
        }
        return getClass().getName() + "[hgap=" + hgap + ",vgap=" + vgap + str + "]";
    }

}
