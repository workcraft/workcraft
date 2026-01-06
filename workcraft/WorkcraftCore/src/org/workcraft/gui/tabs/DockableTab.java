package org.workcraft.gui.tabs;

import org.workcraft.Framework;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.actions.Action;

import javax.swing.*;
import java.awt.*;

public class DockableTab extends JPanel {

    public DockableTab(PanelDockable panelDockable) {
        super();
        setOpaque(false);
        setLayout(new BorderLayout());
        setFocusable(false);

        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.X_AXIS));
        buttonsPanel.setOpaque(false);
        buttonsPanel.setFocusable(false);

        JLabel label = new JLabel(panelDockable.getTabText());
        label.setFocusable(false);
        label.setOpaque(false);
        Font font = label.getFont();
        label.setFont(font.deriveFont(font.getStyle() | Font.BOLD));

        ContentPanel contentPanel = panelDockable.getComponent();
        MainWindow mainWindow = Framework.getInstance().getMainWindow();
        int extraSpace = 0;
        if ((contentPanel.getOptions() & ContentPanel.MAXIMIZE_BUTTON) != 0) {
            TabButton maxButton = new TabButton("Maximize window",
                    new Action("\u2191", () -> mainWindow.togglePanelDockableMaximized(panelDockable)));

            buttonsPanel.add(maxButton);
            extraSpace = maxButton.getPreferredSize().width;
        }

        if ((contentPanel.getOptions() & ContentPanel.CLOSE_BUTTON) != 0) {
            TabButton closeButton = new TabButton("Close window",
                    new Action("\u00d7", () -> mainWindow.closePanelDockable(panelDockable)));

            buttonsPanel.add(Box.createRigidArea(new Dimension(extraSpace / 2, 0)));
            buttonsPanel.add(closeButton);
            extraSpace = closeButton.getPreferredSize().width;
        }
        buttonsPanel.add(Box.createRigidArea(new Dimension(2 * extraSpace, 0)), 0);

        add(label, BorderLayout.CENTER);
        add(buttonsPanel, BorderLayout.EAST);
    }

}
