package org.workcraft.gui;

import java.awt.Color;
import java.awt.Image;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.InvocationTargetException;

import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;

import org.workcraft.dom.visual.SizeHelper;
import org.workcraft.util.GUI;

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

    static void apply(final MainWindow window) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                int size = SizeHelper.getIconSize();
                ImageIcon activeSvg = GUI.createIconFromSVG("images/icon.svg", size, size, Color.WHITE);
                ImageIcon inactiveSvg = GUI.createIconFromSVG("images/icon-inactive.svg", size, size, Color.WHITE);
                final Image activeIcon = activeSvg.getImage();
                final Image inactiveIcon = inactiveSvg.getImage();

                try {
                    SwingUtilities.invokeAndWait(new Runnable() {
                        @Override
                        public void run() {
                            window.setIconImage(window.isActive() ? activeIcon : inactiveIcon);
                            window.addWindowListener(new IconUpdater(window, inactiveIcon, activeIcon));
                        }
                    });
                } catch (InterruptedException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

}
