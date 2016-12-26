package org.workcraft.gui.tabs;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.border.Border;

import org.workcraft.gui.actions.Action;
import org.workcraft.gui.actions.ScriptedActionListener;

public class TabButton extends JLabel implements MouseListener {
    private static final long serialVersionUID = 1L;
    private final ScriptedActionListener actionListener;
    private final Action action;

    Border mouseOutBorder, mouseOverBorder;

    public TabButton(String label, String toolTipText, Action action, ScriptedActionListener actionListener) {
        super(label);
        setVerticalAlignment(JLabel.CENTER);
        setFont(getFont().deriveFont(Font.PLAIN)); //.deriveFont(AffineTransform.getScaleInstance(0.8, 0.8)));
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
        setForeground(new Color(200, 0, 0));
        //    this.setBorder(mouseOverBorder);
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
