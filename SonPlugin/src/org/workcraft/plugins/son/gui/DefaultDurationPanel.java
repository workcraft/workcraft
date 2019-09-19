package org.workcraft.plugins.son.gui;

import org.workcraft.plugins.son.util.Interval;

import javax.swing.*;
import javax.swing.text.AbstractDocument;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class DefaultDurationPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private JLabel durationLabel;
    private JTextField min;
    private JTextField max;
    private boolean validDuration = true;

    private static final int labelheight = 20;
    private static final int labelwidth = 35;

    protected Font font = new Font("Arial", Font.PLAIN, 12);

    public DefaultDurationPanel() {
        this(new Interval(0, 0));
    }

    public DefaultDurationPanel(Interval duration) {
        durationLabel = new JLabel();
        durationLabel.setText("Default duration:");
        durationLabel.setFont(font);

        min = new JTextField();
        min.setText(duration.minToString());
        min.setPreferredSize(new Dimension(labelwidth, labelheight));
        ((AbstractDocument) min.getDocument()).setDocumentFilter(new TimeInputFilter());

        JLabel dash = new JLabel();
        dash.setText("-");

        max = new JTextField();
        max.setText(duration.maxToString());
        max.setPreferredSize(new Dimension(labelwidth, labelheight));
        ((AbstractDocument) max.getDocument()).setDocumentFilter(new TimeInputFilter());

        add(durationLabel);
        add(min);
        add(dash);
        add(max);

        min.addFocusListener(new FocusListener() {
            @Override
            public void focusLost(FocusEvent e) {
                autoComplete(min);
                if (!isValid(getDefaultDuration())) {
                    validDuration = false;
                } else {
                    validDuration = true;
                }
            }

            @Override
            public void focusGained(FocusEvent e) {
                min.selectAll();
            }
        });

        min.addKeyListener(new KeyListener() {

            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    requestFocus();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }

        });

        max.addFocusListener(new FocusListener() {
            @Override
            public void focusLost(FocusEvent e) {

                autoComplete(max);
                if (!isValid(getDefaultDuration())) {
                    validDuration = false;
                } else {
                    validDuration = true;
                }
            }

            @Override
            public void focusGained(FocusEvent e) {
                max.selectAll();
            }
        });

        max.addKeyListener(new KeyListener() {

            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    requestFocus();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }

        });
    }

    private void autoComplete(JTextField field) {
        String text = field.getText();
        int length = text.length();

        if (length < 4) {
            while (length < 4) {
                StringBuffer sb = new StringBuffer();
                sb.append("0").append(text);
                text = sb.toString();
                field.setText(text);
                length = text.length();
            }
        }
    }

    private boolean isValid(Interval value) {
        int start = value.getMin();
        int end = value.getMax();

        if (start <= end) {
            return true;
        }
        return false;
    }

    public Interval getDefaultDuration() {
        int minValue = Interval.getInteger(min.getText());
        int maxValue = Interval.getInteger(max.getText());
        return new Interval(minValue, maxValue);
    }

    public boolean isValidDuration() {
        return validDuration;
    }

    public JTextField getMin() {
        return min;
    }

    public JTextField getMax() {
        return max;
    }

    public JLabel getDurationLabel() {
        return durationLabel;
    }

    public void setIsEnable(boolean b) {
        min.setEnabled(b);
        max.setEnabled(b);
    }

}
