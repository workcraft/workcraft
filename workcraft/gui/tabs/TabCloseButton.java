package org.workcraft.gui.tabs;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.border.Border;

public class TabCloseButton extends JLabel implements MouseListener {
	private static final long serialVersionUID = 1L;

	Border mouseOutBorder, mouseOverBorder;

	public TabCloseButton() {
		super("X");
		setVerticalAlignment(JLabel.CENTER);
		setFont(getFont().deriveFont(Font.BOLD));
		setOpaque(false);
		setForeground(Color.GRAY);
		addMouseListener(this);
		setToolTipText("Close window");

		this.mouseOutBorder = BorderFactory.createEmptyBorder(1, 1, 1, 1);
		this.mouseOverBorder = BorderFactory.createLineBorder(Color.GRAY);

		//this.setBorder(mouseOutBorder);
	}

	public void mouseClicked(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {
		setForeground(new Color(200,0,0));
		//	this.setBorder(mouseOverBorder);
	}

	public void mouseExited(MouseEvent e) {
		setForeground(Color.GRAY);
		//this.setBorder(mouseOutBorder);

	}

	public void mousePressed(MouseEvent e) {

	}

	public void mouseReleased(MouseEvent e) {
	}

}
