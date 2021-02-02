package org.workcraft.gui.controls;

import javax.swing.*;

public class SpeedSlider extends JSlider {

    private static final int SLIDER_RANGE = 10;
    private static final double BASE_SPEED = 300;
    private static final double INCREMENT_SPEED = 10;

    public SpeedSlider() {
        this(SLIDER_RANGE, 0);
    }

    public SpeedSlider(int range, int value) {
        super(-range, range, value);
        setToolTipText("Speed");
        setMajorTickSpacing(range);
        setMinorTickSpacing(1);
        setPaintTicks(true);
    }

    public int getDelay() {
        double power = -2.0 * getValue() / (getMaximum() - getMinimum());
        return (int) (BASE_SPEED * Math.pow(INCREMENT_SPEED, power));
    }

}
