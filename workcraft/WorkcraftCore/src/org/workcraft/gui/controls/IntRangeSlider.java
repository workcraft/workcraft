package org.workcraft.gui.controls;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.basic.BasicSliderUI;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class IntRangeSlider extends JPanel {

    private final JSlider slider = new JSlider();
    private final MouseHandler mouseHandler = new MouseHandler();
    private final BoundedRangeModel model;

    private float scale;

    private final class MouseHandler extends MouseAdapter {
        public static final float MARGIN = 0.4f;
        private int cursorType;
        private int startPos;
        private int startValue;
        private int startExtent;

        @Override
        public void mousePressed(MouseEvent e) {
            if (isEnabled()) {
                startPos = e.getX();
                startValue = model.getValue();
                startExtent = model.getExtent();
            }
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            if (isEnabled()) {
                float mouseValue = getMinimum() + e.getX() * scale;
                float margin = getValue() == getSecondValue() ? -MARGIN : MARGIN;
                if (mouseValue < getValue() + margin) {
                    cursorType = Cursor.W_RESIZE_CURSOR;
                } else if (mouseValue > getSecondValue() - margin) {
                    cursorType = Cursor.E_RESIZE_CURSOR;
                } else {
                    cursorType = Cursor.MOVE_CURSOR;
                }
                setCursor(Cursor.getPredefinedCursor(cursorType));
            }
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            if (isEnabled()) {
                switch (cursorType) {
                    case Cursor.W_RESIZE_CURSOR -> {
                        setValue(startValue - 1);
                        repaint();
                    }
                    case Cursor.E_RESIZE_CURSOR -> {
                        setSecondValue(startValue + startExtent + 1);
                        repaint();
                    }
                    default -> { }
                }
            }
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            if (isEnabled()) {
                int offset = Math.round((e.getX() - startPos) * scale);
                switch (cursorType) {
                    case Cursor.W_RESIZE_CURSOR -> {
                        setValue(startValue + offset);
                        repaint();
                    }
                    case Cursor.E_RESIZE_CURSOR -> {
                        int secondValue = startValue + startExtent + offset;
                        setSecondValue(secondValue);
                        repaint();
                    }
                    case Cursor.MOVE_CURSOR -> {
                        setValue(startValue + offset);
                        setSecondValue(startValue + startExtent + offset);
                        repaint();
                    }
                    default -> { }
                }
            }
        }
    }

    public IntRangeSlider() {
        this(0, 9, 0, 9);
    }

    public IntRangeSlider(int min, int max, int value, int extent) {
        this(new DefaultBoundedRangeModel(value, extent, min, max));
    }

    public IntRangeSlider(BoundedRangeModel model) {
        this.model = model;
        setBorder(new EmptyBorder(1, 1, 1, 1));
        slider.setMinimum(model.getMinimum());
        slider.setMaximum(model.getMaximum());
        slider.setPaintTicks(false);
        slider.setPaintTrack(true);
        slider.setPaintLabels(true);
        slider.setMajorTickSpacing(1);

        addMouseListener(mouseHandler);
        addMouseMotionListener(mouseHandler);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                float extent = model.getMaximum() - model.getMinimum();
                Insets insets = getInsets();
                scale = extent / (getWidth() - insets.left - insets.right);
                repaint();
            }
        });

        model.addChangeListener(e -> {
            ChangeEvent e1 = new ChangeEvent(this);
            for (ChangeListener l : listenerList.getListeners(ChangeListener.class)) {
                l.stateChanged(e1);
            }
            repaint();
        });
    }

    public void setMaximum(int value) {
        model.setMaximum(value);
        slider.setMaximum(value);
    }

    public int getMaximum() {
        return model.getMaximum();
    }

    public void setMinimum(int value) {
        model.setMinimum(value);
        slider.setMinimum(value);
    }

    public int getMinimum() {
        return model.getMinimum();
    }

    public void setValue(int value) {
        value = adjustToRange(value, model.getMinimum(), model.getMaximum());
        int extent = adjustToRange(getSecondValue() - value, 0, model.getMaximum() - value);
        model.setRangeProperties(value, extent, model.getMinimum(), model.getMaximum(), false);
    }

    public int getValue() {
        return model.getValue();
    }

    public void setSecondValue(int value) {
        value = adjustToRange(value, model.getMinimum(), model.getMaximum());
        int extent = adjustToRange(value - model.getValue(), 0, value - model.getMinimum());
        model.setRangeProperties(value - extent, extent, model.getMinimum(), model.getMaximum(), false);
    }

    private int adjustToRange(int value, int min, int max) {
        if (value < min) return min;
        if (value > max) return max;
        return value;
    }

    public int getSecondValue() {
        return model.getValue() + model.getExtent();
    }

    public void addChangeListener(ChangeListener l) {
        listenerList.add(ChangeListener.class, l);
    }

    public void removeChangeListener(ChangeListener l) {
        listenerList.remove(ChangeListener.class, l);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        slider.setEnabled(enabled);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        slider.setMinimum(model.getMinimum());
        slider.setMaximum(model.getMaximum());
        slider.setBounds(getBounds());
        slider.setValue(model.getMinimum());

        BasicSliderUI ui = (BasicSliderUI) slider.getUI();
        if (slider.getPaintTrack()) {
            ui.paintTrack(g);
        }

        slider.setValue(getSecondValue());
        Rectangle clip = g.getClipBounds();
        int x = (int) ((model.getValue() - model.getMinimum()) / scale);
        Rectangle r = new Rectangle(x, 0, getWidth(), getHeight()).intersection(clip);
        g.setClip(r.x, r.y, r.width, r.height);
        slider.paint(g);
        g.setClip(clip.x, clip.y, clip.width, clip.height);

        if (slider.getPaintTicks()) {
            ui.paintTicks(g);
        }
        if (slider.getPaintLabels()) {
            ui.paintLabels(g);
        }

        slider.setValue(getValue());
        ui.paintThumb(g);
    }

    @Override
    public void setFont(Font font) {
        if (slider != null) {
            slider.setFont(font);
        }
    }

    @Override
    public Dimension getPreferredSize() {
        return slider.getPreferredSize();
    }

    @Override
    public void setPreferredSize(Dimension preferredSize) {
        slider.setPreferredSize(preferredSize);
    }

}
