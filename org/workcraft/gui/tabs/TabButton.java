/*
*
* Copyright 2008,2009 Newcastle University
*
* This file is part of Workcraft.
*
* Workcraft is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* Workcraft is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with Workcraft.  If not, see <http://www.gnu.org/licenses/>.
*
*/

package org.workcraft.gui.tabs;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.border.Border;

import org.workcraft.gui.actions.ScriptedAction;
import org.workcraft.gui.actions.ScriptedActionListener;

public class TabButton extends JLabel implements MouseListener {
	private static final long serialVersionUID = 1L;
	private ScriptedActionListener actionListener;
	private ScriptedAction action;

	Border mouseOutBorder, mouseOverBorder;

	public TabButton(String label, String toolTipText, ScriptedAction action, ScriptedActionListener actionListener) {
		super(label);
		setVerticalAlignment(JLabel.CENTER);
		setFont(getFont().deriveFont(Font.PLAIN));//.deriveFont(AffineTransform.getScaleInstance(0.8, 0.8)));
		setOpaque(false);
		setForeground(Color.GRAY);
		addMouseListener(this);
		setToolTipText(toolTipText);

		mouseOutBorder = BorderFactory.createEmptyBorder(1, 1, 1, 1);
		mouseOverBorder = BorderFactory.createLineBorder(Color.GRAY);

		this.action = action;
		this.actionListener = actionListener;

		//this.setBorder(mouseOutBorder);
	}

	public void mouseClicked(MouseEvent e) {
		actionListener.actionPerformed(action);
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
