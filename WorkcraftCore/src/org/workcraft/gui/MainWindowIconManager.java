package org.workcraft.gui;

import java.awt.Color;
import java.awt.Image;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.InvocationTargetException;

import javax.swing.SwingUtilities;

import org.workcraft.util.GUI;

public class MainWindowIconManager {
    static void apply(final MainWindow window) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final Image activeIcon = GUI.createIconFromSVG("images/icon.svg", 32, 32, Color.WHITE).getImage();
                final Image inactiveIcon = GUI.createIconFromSVG("images/icon-inactive.svg", 32, 32, Color.WHITE).getImage();

                try {
                    SwingUtilities.invokeAndWait(new Runnable() {
                        @Override
                        public void run() {
                            window.setIconImage(window.isActive() ? activeIcon : inactiveIcon);
                            window.addWindowListener(new WindowAdapter() {
                                @Override
                                public void windowDeactivated(WindowEvent e) {
                                    window.setIconImage(inactiveIcon);
                                }
                                @Override
                                public void windowActivated(WindowEvent e) {
                                    window.setIconImage(activeIcon);
                                }
                            });
                        }
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
        ).start();
    }
}
