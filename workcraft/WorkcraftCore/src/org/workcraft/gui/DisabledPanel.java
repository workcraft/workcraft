package org.workcraft.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

import javax.swing.JPanel;

public class DisabledPanel extends JPanel {

    private final Font font = Font.getFont(Font.DIALOG);

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        g.setColor(Color.LIGHT_GRAY);
        g.setFont(font);
        g.drawString("N/A", getWidth() / 2, getHeight() / 2);
    }

}
