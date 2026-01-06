package org.workcraft.gui.panels;

import org.workcraft.utils.GuiUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class DocumentPlaceholderPanel extends JPanel {

    private final BufferedImage logoImage;

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

    public DocumentPlaceholderPanel() {
        super();
        logoImage = getImage();
        setBackground(new Color(255, 255, 255));
        setLayout(null);
    }

    private BufferedImage getImage() {
        try {
            return GuiUtils.loadImageFromResource("images/logo.png");
        } catch (IOException ignored) {
        }
        return null;
    }

}
