package org.workcraft.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class DocumentPlaceholder extends JPanel {
	static private BufferedImage logoImage;
	@Override
	public void paint(Graphics g) {
		super.paint(g);

		BufferedImage logo = getImage();

		if (logo != null) {
			int w = logo.getWidth();
			int h = logo.getHeight();

			int x = (getWidth() - w)/2;
			int y = (getHeight() - h)/2;

			g.drawImage(logo, x, y, null);
		}
	}

	public DocumentPlaceholder() {
		super();
		setBackground(new Color(255,255,255));
		setLayout(null);

	}

	private BufferedImage getImage() {
		if (logoImage == null)
		{
			try {
				logoImage = ImageIO.read(new File("./images/logo.png"));
			} catch (IOException e) {
				logoImage = null;
			}
		}
		return logoImage;
	}

}
