package org.workcraft.util;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.font.LineMetrics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;

import org.workcraft.gui.graph.tools.GraphEditor;

public class GUI {

	public static JPanel createLabeledComponent (JComponent component, String labelText) {
		JPanel result = new JPanel (new FlowLayout(FlowLayout.LEFT, 3, 0));
		result.add(new JLabel(labelText));
		result.add(component);
		return result;
	}

	public static void centerFrameToParent(Window frame, Window parent) {
		Dimension parentSize = parent.getSize();
		frame.setSize(parentSize.width / 2, parentSize.height / 2);
		Dimension mySize = frame.getSize();
		parent.getLocationOnScreen();

		frame.setLocation (((parentSize.width - mySize.width)/2) + 0, ((parentSize.height - mySize.height)/2) + 0);
	}

	public static BufferedImage loadImageFromResource(String path) throws IOException {
		URL res = ClassLoader.getSystemResource(path);
		if(res==null) {
			throw new IOException("Resource not found: "+path);
		}
		return ImageIO.read(res);
	}

	public static ImageIcon loadIconFromResource(String path) throws IOException {
		URL res = ClassLoader.getSystemResource(path);
		if(res==null) {
			throw new IOException("Resource not found: "+path);
		}
		return new ImageIcon(res);
	}

	public static void drawEditorMessage(GraphEditor editor, Graphics2D g, Color color, String message) {
		g.setFont(UIManager.getFont("Button.font"));
		Rectangle r = g.getFont().getStringBounds(message, g.getFontRenderContext()).getBounds();
		r.x = editor.getWidth()/2 - r.width/2;
		r.y = editor.getHeight() - 20 - r.height;
		g.setColor(new Color(240, 240, 240, 192));
		g.fillRoundRect(r.x-10, r.y-10, r.width+20, r.height+20, 5, 5);
		g.setColor(new Color(224, 224, 224));
		g.drawRoundRect(r.x-10, r.y-10, r.width+20, r.height+20, 5, 5);
		g.setColor(color);
		LineMetrics lm = g.getFont().getLineMetrics(message, g.getFontRenderContext());
		g.drawString (message, r.x, r.y+r.height-(int)(lm.getDescent()));
	}
}
