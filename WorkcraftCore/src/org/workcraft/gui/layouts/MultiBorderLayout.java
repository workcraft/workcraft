package org.workcraft.gui.layouts;

import static java.util.Arrays.asList;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

/**
 * Extends BorderLayout with multiple components in the northList, southList,
 * eastList, westList and centerList. Layout is used for correct docking of
 * multiple toolbars.
 *
 * @author Stanislav Lapitsky (basic idea)
 * @author Wanja Gayk (major rework, layout change)
 * @version 2.0
 */
public class MultiBorderLayout extends BorderLayout {

    private static final long serialVersionUID = 2L;

    private interface DimensionGetter {
        Dimension get(Component in);
    }

    private static final DimensionGetter getMinimumSize = new DimensionGetter() {
        @Override
        public Dimension get(final Component in) {
            return in.getMinimumSize();
        }
    };

    private static final DimensionGetter getPreferredSize = new DimensionGetter() {
        @Override
        public Dimension get(final Component in) {
            return in.getPreferredSize();
        }
    };

    private static final DimensionGetter getMaximumSize = new DimensionGetter() {
        @Override
        public Dimension get(final Component in) {
            return in.getMaximumSize();
        }
    };

    /** List of the north region components. */
    private final List<Component> northList = new ArrayList<>();

    /** List of the south List region components. */
    private final List<Component> southList = new ArrayList<>();

    /** List of the west region components. */
    private final List<Component> westList = new ArrayList<>();

    /** List of the east region components. */
    private final List<Component> eastList = new ArrayList<>();

    /** List of the center region components. */
    private final List<Component> centerList = new ArrayList<>();

    /** Constructs default layout instance. */
    public MultiBorderLayout() {
    }

    /**
     * Constructs new layout instance with defined parameters.
     *
     * @param hgap
     *            the horizontal gap.
     * @param vgap
     *            the vertical gap.
     */
    public MultiBorderLayout(final int hgap, final int vgap) {
        super(hgap, vgap);
    }

    /*
     * The method is deprecated but it's necessary to override it because the
     * current class extends BorderLayout to provide multiple components (e.g.
     * toolbars)
     */
    @SuppressWarnings("deprecation")
    @Override
    public void addLayoutComponent(final String name, final Component comp) {
        synchronized (comp.getTreeLock()) {
            // Assign the component to one of the regions.
            // Special case: treat null the same as "Center".
            if (name == null || CENTER.equals(name)) {
                centerList.add(comp);
            } else if (NORTH.equals(name)) {
                northList.add(comp);
            } else if (SOUTH.equals(name)) {
                southList.add(comp);
            } else if (WEST.equals(name)) {
                westList.add(comp);
            } else if (EAST.equals(name)) {
                eastList.add(comp);
            } else {
                throw new IllegalArgumentException("cannot add to layout: unknown constraint: " + name);
            }
        }
    }

    @Override
    public void removeLayoutComponent(final Component comp) {
        synchronized (comp.getTreeLock()) {
            northList.remove(comp);
            southList.remove(comp);
            westList.remove(comp);
            eastList.remove(comp);
            centerList.remove(comp);
        }
    }

    @Override
    public Dimension minimumLayoutSize(final Container target) {
        return layoutSize(target, getMinimumSize);
    }

    @Override
    public Dimension preferredLayoutSize(final Container target) {
        return layoutSize(target, getPreferredSize);
    }

    @Override
    public Dimension maximumLayoutSize(Container target) {
        return layoutSize(target, getMaximumSize);
    }

    private Dimension layoutSize(final Container target, final DimensionGetter getter) {
        synchronized (target.getTreeLock()) {
            final Dimension northSize = sumHorizontal(northList, getter);
            final Dimension southSize = sumHorizontal(southList, getter);
            final Dimension westSize = sumVertical(westList, getter);
            final Dimension eastSize = sumVertical(eastList, getter);
            final Dimension centerSize = sumCenter(centerList, getter);

            final Dimension dim = new Dimension(
                    max(northSize.width, southSize.width, westSize.width + centerSize.width + eastSize.width),
                    northSize.height + max(westSize.height, centerSize.height, eastSize.height) + southSize.height);

            final Insets insets = target.getInsets();
            dim.width += insets.left + insets.right;
            dim.height += insets.top + insets.bottom;
            return dim;
        }
    }

    @Override
    public void layoutContainer(final Container target) {
        synchronized (target.getTreeLock()) {
            final Insets insets = target.getInsets();

            final Rectangle availableBounds = new Rectangle(insets.left, insets.top,
                    target.getWidth() - insets.left - insets.right, target.getHeight() - insets.top - insets.bottom);

            // TODO: this is only using the preferred size, shrink components
            // to their minimum size, if preferred Size doesn't fit!

            final Dimension northSize = sumHorizontal(northList, getPreferredSize);
            final Dimension southSize = sumHorizontal(southList, getPreferredSize);
            final Dimension westSize = sumVertical(westList, getPreferredSize);
            final Dimension eastSize = sumVertical(eastList, getPreferredSize);

            int left, right, top, bottom;

            left = availableBounds.x;
            top = availableBounds.y;
            for (final Component c : northList) {
                if (c.isVisible()) {
                    final Dimension d = sumHorizontal(asList(c), getPreferredSize);
                    c.setBounds(left, top, d.width, d.height);
                    left += d.width;
                }
            }

            left = availableBounds.x;
            bottom = availableBounds.y + availableBounds.height;
            for (final Component c : southList) {
                if (c.isVisible()) {
                    final Dimension d = sumHorizontal(asList(c), getPreferredSize);
                    c.setBounds(left, bottom - d.height, d.width, d.height);
                    left += d.width;
                }
            }

            left = availableBounds.x;
            top = availableBounds.y + northSize.height;
            for (final Component c : westList) {
                if (c.isVisible()) {
                    final Dimension d = sumVertical(asList(c), getPreferredSize);
                    c.setBounds(left, top, d.width, d.height);
                    top += d.height;
                }
            }

            right = availableBounds.x + availableBounds.width;
            top = availableBounds.y + northSize.height;
            for (final Component c : eastList) {
                if (c.isVisible()) {
                    final Dimension d = sumVertical(asList(c), getPreferredSize);
                    c.setBounds(right - d.width, top, d.width, d.height);
                    top += d.height;
                }
            }

            top = availableBounds.x + northSize.height;
            left = availableBounds.y + westSize.width;
            right = availableBounds.x + availableBounds.width - eastSize.width;
            bottom = availableBounds.y + availableBounds.height - southSize.height;
            for (final Component c : centerList) {
                if (c.isVisible()) {
                    c.setBounds(left, top, right - left, bottom - top);
                }
            }
        }
    }

    private Dimension sumHorizontal(final Iterable<Component> components, final DimensionGetter getter) {
        final Dimension dim = new Dimension();
        for (final Component c : components) {
            if (c.isVisible()) {
                final Dimension d = getter.get(c);
                dim.width += d.width + getHgap();
                dim.height = Math.max(d.height, dim.height);
            }
        }
        return dim;
    }

    private Dimension sumVertical(final Iterable<Component> components, final DimensionGetter getter) {
        final Dimension dim = new Dimension();
        for (final Component c : components) {
            if (c.isVisible()) {
                final Dimension d = getter.get(c);
                dim.width = Math.max(d.width, dim.width);
                dim.height += d.height + getVgap();
            }
        }
        return dim;
    }

    @SuppressWarnings("static-method")
    private Dimension sumCenter(final Iterable<Component> components, final DimensionGetter getter) {
        final Dimension dim = new Dimension();
        for (final Component c : components) {
            if (c.isVisible()) {
                final Dimension d = getter.get(c);
                dim.width += d.width;
                dim.height = Math.max(d.height, dim.height);
            }
        }
        return dim;
    }

    private static int max(final int... values) {
        int max = 0;
        for (int value : values) {
            max = Math.max(max, value);
        }
        return max;
    }

}