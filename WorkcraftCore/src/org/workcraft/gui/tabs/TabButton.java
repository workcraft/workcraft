package org.workcraft.gui.tabs;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JLabel;

import org.workcraft.gui.actions.Action;
import org.workcraft.gui.actions.ScriptedActionListener;

public class TabButton extends JLabel implements MouseListener {
    private static final long serialVersionUID = 1L;
    private final ScriptedActionListener actionListener;
    private final Action action;

    public TabButton(String label, String toolTipText, Action action, ScriptedActionListener actionListener) {
        super(label);
        setVerticalAlignment(JLabel.CENTER);
        setFont(getFont().deriveFont(Font.PLAIN)); //.deriveFont(AffineTransform.getScaleInstance(0.8, 0.8)));
        setOpaque(false);
        setForeground(Color.GRAY);
        addMouseListener(this);
        setToolTipText(toolTipText);

        this.action = action;
        this.actionListener = actionListener;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        actionListener.actionPerformed(action);
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        setForeground(new Color(200, 0, 0));
    }

    @Override
    public void mouseExited(MouseEvent e) {
        setForeground(Color.GRAY);
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

}
