package org.workcraft.gui.tabs;

import org.workcraft.gui.actions.Action;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class TabButton extends JLabel implements MouseListener {

    private final Action action;

    public TabButton(String toolTipText, Action action) {
        super(action.getTitle());
        setVerticalAlignment(JLabel.CENTER);
        setFont(getFont().deriveFont(Font.PLAIN));
        setOpaque(false);
        setForeground(Color.GRAY);
        addMouseListener(this);
        setToolTipText(toolTipText);
        this.action = action;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if ((action != null) && action.isEnabled()) {
            action.run();
        }
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
