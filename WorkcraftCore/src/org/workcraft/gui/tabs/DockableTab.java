package org.workcraft.gui.tabs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.workcraft.gui.DockableWindow;
import org.workcraft.gui.DockableWindowContentPanel;
import org.workcraft.gui.DockableWindowContentPanel.ViewAction;
import org.workcraft.gui.actions.ScriptedActionListener;

@SuppressWarnings("serial")
public class DockableTab extends JPanel {

    public static final int MAX_TITLE_LENGTH = 64;

    private JLabel label;

    public DockableTab(DockableWindow dockableWindow, ScriptedActionListener actionListener) {
        super();
        setOpaque(false);
        setLayout(new BorderLayout());
        setFocusable(false);

        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.X_AXIS));
        buttonsPanel.setOpaque(false);
        buttonsPanel.setFocusable(false);

        String title = dockableWindow.getTabText();
        String trimmedTitle;
        if (title.length() > MAX_TITLE_LENGTH) {
            int i1 = MAX_TITLE_LENGTH / 2;
            int i2 = title.length() - (MAX_TITLE_LENGTH - i1);
            trimmedTitle = title.substring(0, i1) + "..." + title.substring(i2);
        } else {
            trimmedTitle = title;
        }

        label = new JLabel(trimmedTitle);
        label.setFocusable(false);
        label.setOpaque(false);

        TabButton closeButton = null;
        if ((dockableWindow.getOptions() & DockableWindowContentPanel.MAXIMIZE_BUTTON) != 0) {
            ViewAction action = new ViewAction(dockableWindow.getID(), ViewAction.MAXIMIZE_ACTION);
            TabButton maxButton = new TabButton("\u2191", "Maximize window", action, actionListener);
            buttonsPanel.add(maxButton);
            buttonsPanel.add(Box.createRigidArea(new Dimension(2, 0)));
        }

        if ((dockableWindow.getOptions() & DockableWindowContentPanel.CLOSE_BUTTON) != 0) {
            ViewAction action = new ViewAction(dockableWindow.getID(), ViewAction.CLOSE_ACTION);
            closeButton = new TabButton("\u00d7", "Close window", action, actionListener);
            buttonsPanel.add(closeButton);
        }

        Dimension x = label.getPreferredSize();
        Dimension y = (closeButton != null) ? closeButton.getPreferredSize() : x;

        this.add(label, BorderLayout.CENTER);
        this.add(buttonsPanel, BorderLayout.EAST);

        setPreferredSize(new Dimension(x.width + y.width + 30, Math.max(y.height, x.height) + 4));
    }

    private JLabel getLabel() {
        if (label == null) {
            label = new JLabel();
        }
        return label;
    }

    @Override
    public void setForeground(Color fg) {
        getLabel().setForeground(fg);
    }

    @Override
    public Color getForeground() {
        return getLabel().getForeground();
    }

}
