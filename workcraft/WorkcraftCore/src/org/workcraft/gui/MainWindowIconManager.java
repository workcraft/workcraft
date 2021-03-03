package org.workcraft.gui;

import org.workcraft.utils.GuiUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.InvocationTargetException;

public class MainWindowIconManager {

    private static final class IconUpdater extends WindowAdapter {
        private final MainWindow window;
        private final Image inactiveIcon;
        private final Image activeIcon;

        private IconUpdater(MainWindow window, Image inactiveIcon, Image activeIcon) {
            this.window = window;
            this.inactiveIcon = inactiveIcon;
            this.activeIcon = activeIcon;
        }

        @Override
        public void windowDeactivated(WindowEvent e) {
            window.setIconImage(inactiveIcon);
        }

        @Override
        public void windowActivated(WindowEvent e) {
            window.setIconImage(activeIcon);
        }
    }

    public static void apply(final MainWindow window) {
        Thread thread = new Thread(() -> {
            ImageIcon activeSvg = GuiUtils.createIconFromSVG("images/icon.svg");
            ImageIcon inactiveSvg = GuiUtils.createIconFromSVG("images/icon-inactive.svg");
            final Image activeIcon = activeSvg.getImage();
            final Image inactiveIcon = inactiveSvg.getImage();
            try {
                SwingUtilities.invokeAndWait(() -> {
                    window.setIconImage(window.isActive() ? activeIcon : inactiveIcon);
                    window.addWindowListener(new IconUpdater(window, inactiveIcon, activeIcon));
                });
            } catch (InterruptedException | InvocationTargetException e) {
                e.printStackTrace();
            }
        });
        thread.start();
    }

}
