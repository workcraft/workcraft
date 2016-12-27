package org.workcraft.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.swing.JPanel;

import org.workcraft.util.GUI;

@SuppressWarnings("serial")
public class DocumentPlaceholder extends JPanel {
    private static BufferedImage logoImage;
    @Override
    public void paint(Graphics g) {
        super.paint(g);

        if (logoImage != null) {
            int w = logoImage.getWidth();
            int h = logoImage.getHeight();

            int x = (getWidth() - w) / 2;
            int y = (getHeight() - h) / 2;

            g.drawImage(logoImage, x, y, null);
        }
    }

    public DocumentPlaceholder() {
        super();
        logoImage = getImage();
        setBackground(new Color(255, 255, 255));
        setLayout(null);

    }

    private BufferedImage getImage() {
        if (logoImage == null) {
            try {
                logoImage = GUI.loadImageFromResource("images/logo.png");
            } catch (IOException e) {
                logoImage = null;
            }
        }
        return logoImage;
    }

}
