package org.workcraft.util;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.font.LineMetrics;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;

import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.UserAgentAdapter;
import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.util.XMLResourceDescriptor;
import org.w3c.dom.Document;
import org.workcraft.gui.graph.tools.GraphEditor;
import org.workcraft.plugins.shared.CommonVisualSettings;

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
		Point q = parent.getLocationOnScreen();

		frame.setLocation (((parentSize.width - mySize.width)/2) + q.x, ((parentSize.height - mySize.height)/2) + q.y);
	}

	public static BufferedImage loadImageFromResource(String path) throws IOException {
		URL res = ClassLoader.getSystemResource(path);
		if(res==null) {
			throw new IOException("Resource not found: "+path);
		}
		return ImageIO.read(res);
	}

	public static ImageIcon createIconFromImage(String path) {
		URL res = ClassLoader.getSystemResource(path);
		if(res==null) {
			System.err.println ("Missing icon: " + path);
			return null;
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

	public static ImageIcon createIconFromSVG(String path) {

		try {

			System.setProperty("org.apache.batik.warn_destination", "false");
			Document document;

			String parser = XMLResourceDescriptor.getXMLParserClassName();
			SAXSVGDocumentFactory f = new SAXSVGDocumentFactory(parser);

			document = f.createDocument(ClassLoader.getSystemResource(path).toString());

			UserAgentAdapter userAgentAdapter = new UserAgentAdapter();
			BridgeContext bridgeContext = new BridgeContext(userAgentAdapter);
			GVTBuilder builder = new GVTBuilder();

			GraphicsNode graphicsNode =	builder.build(bridgeContext, document);

			double size = Math.max(bridgeContext.getDocumentSize().getHeight(), bridgeContext.getDocumentSize().getWidth());
			int iconSize = CommonVisualSettings.getIconSize();

			BufferedImage bufferedImage = new BufferedImage(iconSize, iconSize, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g2d = (Graphics2D) bufferedImage.getGraphics();

			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
			g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

			double scale = (iconSize - 1) / size;
			g2d.scale(scale, scale);
			g2d.translate(0.5, 0.5);

			graphicsNode.paint(g2d);
			g2d.dispose();
			return new ImageIcon(bufferedImage);
		}
		catch (Throwable e) {
			System.err.println ("Failed to load SVG file " + path);
			System.err.println(e);
			return null;
		}
	}

	public static JButton createIconButton(Icon icon, String toolTip) {
		JButton result = new JButton(icon);
		result.setToolTipText(toolTip);
		result.setMargin(new Insets(0,0,0,0));
		int iconSize = CommonVisualSettings.getIconSize();
		Insets insets = result.getInsets();
		int minSize = iconSize+Math.max(insets.left+insets.right, insets.top+insets.bottom);
		result.setPreferredSize(new Dimension(minSize, minSize));
		return result;
	}
}