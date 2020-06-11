package org.workcraft.gui.tabs;

import org.workcraft.Framework;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.actions.Action;

import javax.swing.*;
import java.awt.*;

@SuppressWarnings("serial")
public class DockableTab extends JPanel {

    public static final int MAX_TITLE_LENGTH = 50;

    public DockableTab(DockableWindow dockableWindow) {
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

        JLabel label = new JLabel(trimmedTitle);
        label.setFocusable(false);
        label.setOpaque(false);

        ContentPanel contentPanel = dockableWindow.getComponent();
        MainWindow mainWindow = Framework.getInstance().getMainWindow();
        if ((contentPanel.getOptions() & ContentPanel.MAXIMIZE_BUTTON) != 0) {
            TabButton maxButton = new TabButton("Maximize window",
                    new Action("\u2191", () -> mainWindow.toggleDockableWindowMaximized(dockableWindow)));

            buttonsPanel.add(maxButton);
            buttonsPanel.add(Box.createRigidArea(new Dimension(2, 0)));
        }

        TabButton closeButton = null;
        if ((contentPanel.getOptions() & ContentPanel.CLOSE_BUTTON) != 0) {
            closeButton = new TabButton("Close window",
                    new Action("\u00d7", () -> mainWindow.closeDockableWindow(dockableWindow)));

            buttonsPanel.add(closeButton);
        }

        Dimension x = label.getPreferredSize();
        Dimension y = (closeButton != null) ? closeButton.getPreferredSize() : x;

        add(label, BorderLayout.CENTER);
        add(buttonsPanel, BorderLayout.EAST);

        setPreferredSize(new Dimension(x.width + y.width + 30, Math.max(y.height, x.height) + 4));
    }

}
