package org.workcraft.plugins.stg;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.KeyEvent;
import java.awt.font.GlyphVector;
import java.awt.geom.Rectangle2D;
import java.util.LinkedHashMap;

import org.w3c.dom.Element;
import org.workcraft.framework.plugins.HotKeyDeclaration;
import org.workcraft.gui.Coloriser;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.plugins.petri.Transition;
import org.workcraft.plugins.petri.VisualTransition;

@HotKeyDeclaration(KeyEvent.VK_T)
public class VisualSignalTransition extends VisualTransition {
	private final static double size = 1;
	private final static float strokeWidth = 0.1f;

	private static Color inputsColor = Color.RED.darker();
	private static Color dummiesColor = Color.BLACK;
	private static Color outputsColor = Color.BLUE.darker();
	private static Color internalsColor = Color.GREEN.darker();
	private static Color defaultFillColor = Color.WHITE;
	private static Font font = new Font("Sans-serif", Font.PLAIN, 1).deriveFont(0.75f);

	private Color userFillColor = defaultFillColor;

	private String text = null;
	private GlyphVector glyphVector = null;
	private Rectangle2D textBB = null;
	private Rectangle2D emptyBB = new Rectangle2D.Double(-size/2, -size/2, size, size);
	private float textX, textY;

	public VisualSignalTransition(SignalTransition transition, Element xmlElement) {
		super(transition, xmlElement);
		addPropertyDeclarations();

		updateText();
	}

	public VisualSignalTransition(Transition transition) {
		super(transition);
		addPropertyDeclarations();

		updateText();
	}

	private void addPropertyDeclarations() {
		LinkedHashMap<String, Object> types = new LinkedHashMap<String, Object>();
		types.put("Input", SignalTransition.Type.INPUT);
		types.put("Output", SignalTransition.Type.OUTPUT);
		types.put("Internal", SignalTransition.Type.INTERNAL);
		types.put("Dummy", SignalTransition.Type.DUMMY);

		LinkedHashMap<String, Object> directions = new LinkedHashMap<String, Object>();
		directions.put("+", SignalTransition.Direction.PLUS);
		directions.put("-", SignalTransition.Direction.MINUS);
		directions.put("", SignalTransition.Direction.TOGGLE);

		addPropertyDeclaration(new PropertyDeclaration("Signal name", "getSignalName", "setSignalName", String.class));
		addPropertyDeclaration(new PropertyDeclaration("Transition", "getDirection", "setDirection", SignalTransition.Direction.class, directions));
		addPropertyDeclaration(new PropertyDeclaration("Signal type", "getType", "setType", SignalTransition.Type.class, types));
	}

	@Override
	public void draw(Graphics2D g) {
		drawLabelInLocalSpace(g);


		// some debug info
		int postv = getPostset().size();
		int postm = getReferencedTransition().getPostset().size();
		int prev = getPreset().size();
		int prem = getReferencedTransition().getPreset().size();

		if (postv!=postm||prev!=prem) {

			g.setColor(Color.red);
		    Font font = new Font("Courier", Font.PLAIN, 1);
		    g.setFont(font);

			String str = (postv!=postm)?("POST("+postv+","+postm+")"):"";
			str+=(prev!=prem)?("PRE("+prev+","+prem+")"):"";

			g.drawString("ERROR:"+str, 1, 0);

		}


		if (text == null) {
			Shape shape = new Rectangle2D.Double(
					-size / 2 + strokeWidth / 2,
					-size / 2 + strokeWidth / 2,
					size - strokeWidth,
					size - strokeWidth);

			g.setColor(Coloriser.colorise(userFillColor, getColorisation()));
			g.fill(shape);


			g.setColor(Coloriser.colorise(getColor(), getColorisation()));
			g.setStroke(new BasicStroke(strokeWidth));
			g.draw(shape);
		} else {
			g.setColor(Coloriser.colorise(getColor(), getColorisation()));
			g.setFont(font);

			if (textBB == null) {
				glyphVector = font.createGlyphVector(g.getFontRenderContext(), text);
				textBB = glyphVector.getVisualBounds();
				textBB.setRect(textBB.getX() - 0.075, textBB.getY() - 0.075, textBB.getWidth() + 0.15, textBB.getHeight() + 0.15);



				textX = (float)-textBB.getCenterX();
				textY = (float)-textBB.getCenterY();

				textBB.setRect(textBB.getX() - textBB.getCenterX(), textBB.getY() - textBB.getCenterY(), textBB.getWidth(), textBB.getHeight());
				firePropertyChanged("shape");
			}

			//g.setColor(Coloriser.colorise(userFillColor, getColorisation()));
		//	g.fill(textBB);

			g.setColor(Coloriser.colorise(getColor(), getColorisation()));
			g.drawGlyphVector(glyphVector, textX, textY);
		}
	}

	@Override
	public Rectangle2D getBoundingBoxInLocalSpace() {
		if (textBB == null)
			return emptyBB;
		else
			return textBB;
	}

	private String getText() {
		String text = getSignalName();
		if (text.isEmpty())
			return text;

		switch (getDirection()) {
		case PLUS:
			return text+"+";
		case MINUS:
			return text+"-";
		}

		return text;
	}

	private Color getColor() {
		if (getType() == SignalTransition.Type.DUMMY)
			return dummiesColor;
		if (getType() == SignalTransition.Type.INTERNAL)
			return internalsColor;
		if (getType() == SignalTransition.Type.INPUT)
			return inputsColor;
		if (getType() == SignalTransition.Type.OUTPUT)
			return outputsColor;
		return Color.BLACK;
	}

	private void updateText() {
		if (getSignalName().isEmpty()) {
			text = null;
		} else {
			text = getText();
		}

		glyphVector = null;
		textBB = null;
	}

	public SignalTransition getReferencedTransition() {
		return (SignalTransition)getReferencedComponent();
	}

	public SignalTransition.Type getType() {
		return getReferencedTransition().getType();
	}

	public void setType(SignalTransition.Type type) {
		getReferencedTransition().setType(type);
	}

	public SignalTransition.Direction getDirection() {
		return getReferencedTransition().getDirection();
	}

	public void setDirection(SignalTransition.Direction direction) {
		getReferencedTransition().setDirection(direction);
		updateText();
	}

	public String getSignalName() {
		return getReferencedTransition().getSignalName();
	}

	public void setSignalName(String name) {
		getReferencedTransition().setSignalName(name);
		updateText();
	}
}
