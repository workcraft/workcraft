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
                final Image active = GUI.createIconFromSVG("images/icons/svg/place.svg", 32, 32, Color.WHITE).getImage();
                final Image inactive = GUI.createIconFromSVG("images/icons/svg/place_empty.svg", 32, 32, Color.WHITE).getImage();

                try {
                    SwingUtilities.invokeAndWait(new Runnable() {
                        @Override
                        public void run() {
                            window.setIconImage(window.isActive() ? active : inactive);
                            window.addWindowListener(new WindowAdapter() {
                                @Override
                                public void windowDeactivated(WindowEvent e) {
                                    window.setIconImage(inactive);
                                }
                                @Override
                                public void windowActivated(WindowEvent e) {
                                    window.setIconImage(active);
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
