package org.workcraft.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

import javax.swing.JPanel;

@SuppressWarnings("serial")
public class DisabledPanel extends JPanel {
	Font font;

	public DisabledPanel() {
		super();
		font = Font.getFont(Font.DIALOG);
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		g.setColor(Color.LIGHT_GRAY);
		g.drawRect(0, 0, getWidth()-1, getHeight()-1);

		g.setFont(font);
		g.drawString("N/A", getWidth()/2, getHeight()/2);

	}
}
