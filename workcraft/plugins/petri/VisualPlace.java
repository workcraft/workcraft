package org.workcraft.plugins.petri;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.KeyEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import javax.swing.JLabel;
import javax.swing.JPopupMenu;

import org.w3c.dom.Element;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.framework.plugins.HotKeyDeclaration;
import org.workcraft.gui.Coloriser;
import org.workcraft.gui.actions.ScriptedAction;
import org.workcraft.gui.actions.ScriptedActionListener;
import org.workcraft.gui.actions.ScriptedActionMenuItem;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;

@HotKeyDeclaration(KeyEvent.VK_P)
public class VisualPlace extends VisualComponent {
	class AddTokenAction extends ScriptedAction {
		private int placeID;
		public AddTokenAction(int placeID) {
			super();
			this.placeID = placeID;
		}
		public String getScript() {
			return "p=model.getComponentByID("+placeID+");\np.setTokens(p.getTokens()+1);\nmodel.fireNodePropertyChanged(\"Tokens\", p);";
		}
		public String getUndoScript() {
			return "p=model.getComponentByID("+placeID+");\np.setTokens(p.getTokens()-1);\n";
		}
		public String getRedoScript() {
			return getScript();
		}
		public String getText() {
			return "Add token";
		}
	}

	class RemoveTokenAction extends ScriptedAction {
		private int placeID;

		public RemoveTokenAction(Place place) {
			super();
			this.placeID = place.getID();
				if (place.getTokens() < 1)
					setEnabled(false);
		}
		public String getScript() {
				return "p=model.getComponentByID("+placeID+");p.setTokens(p.getTokens()-1);\nmodel.fireNodePropertyChanged(\"Tokens\", p);";
		}
		public String getUndoScript() {
				return "p=model.getComponentByID("+placeID+");\np.setTokens(p.getTokens()+1);\n";
		}
		public String getRedoScript() {
			return getScript();
		}
		public String getText() {
			return "Remove token";
		}
	}
	protected static double size = 1;
	protected static float strokeWidth = 0.1f;
	protected static double singleTokenSize = size / 1.9;
	protected static double multipleTokenSeparation = strokeWidth / 8;

	protected static Color defaultBorderColor = Color.BLACK;
	protected static Color defaultFillColor = Color.WHITE;
	protected static Color defaultTokenColor = Color.BLACK;

	protected Color userBorderColor = defaultBorderColor;
	protected Color userFillColor = defaultFillColor;
	protected Color userTokenColor = defaultTokenColor;

	public Place getPlace() {
		return (Place)getReferencedComponent();
	}

	public int getTokens() {
		return getPlace().getTokens();
	}

	public void setTokens(int tokens) {
		getPlace().setTokens(tokens);
	}

	public VisualPlace(Place place) {
		super(place);
		addPropertyDeclarations();
	}

	public VisualPlace(Place place, Element xmlElement) {
		super(place, xmlElement);
		addPropertyDeclarations();
	}

	private void addPropertyDeclarations() {
		addPropertyDeclaration(new PropertyDeclaration ("Tokens", "getTokens", "setTokens", int.class));
	}

	public static void drawTokens(int tokens, double singleTokenSize, double multipleTokenSeparation,
			double diameter, double borderWidth, Color tokenColor,	Graphics2D g) {
		Shape shape;
		if (tokens == 1)
		{
			shape = new Ellipse2D.Double(
					-singleTokenSize / 2,
					-singleTokenSize / 2,
					singleTokenSize,
					singleTokenSize);

			g.setColor(tokenColor);
			g.fill(shape);
		}
		else
			if (tokens > 1 && tokens < 8)
			{
				double al = Math.PI / tokens;
				if (tokens == 7) al = Math.PI / 6;

				double r = (diameter / 2 - borderWidth - multipleTokenSeparation) / (1 + 1 / Math.sin(al));
				double R = r / Math.sin(al);

				r -= multipleTokenSeparation;

				for(int i = 0; i < tokens; i++)
				{
					if (i == 6)
						shape = new Ellipse2D.Double( -r, -r, r * 2, r * 2);
					else
						shape = new Ellipse2D.Double(
								-R * Math.sin(i * al * 2) - r,
								-R * Math.cos(i * al * 2) - r,
								r * 2,
								r * 2);

					g.setColor(tokenColor);
					g.fill(shape);
				}
			}
			else if (tokens > 7)
			{
				String out = Integer.toString(tokens);
				Font superFont = g.getFont().deriveFont((float)size/2);

				Rectangle2D rect = superFont.getStringBounds(out, g.getFontRenderContext());
				g.setFont(superFont);
				g.setColor(tokenColor);
				g.drawString(Integer.toString(tokens), (float)(-rect.getCenterX()), (float)(-rect.getCenterY()));
			}
	}

	@Override
	protected void drawInLocalSpace(Graphics2D g)
	{
		Shape shape = new Ellipse2D.Double(
				-size / 2 + strokeWidth / 2,
				-size / 2 + strokeWidth / 2,
				size - strokeWidth,
				size - strokeWidth);

		g.setColor(Coloriser.colorise(userFillColor, getColorisation()));
		g.fill(shape);
		g.setColor(Coloriser.colorise(userBorderColor, getColorisation()));
		g.setStroke(new BasicStroke(strokeWidth));
		g.draw(shape);

		Place p = (Place)getReferencedComponent();

		drawTokens(p.getTokens(), singleTokenSize, multipleTokenSeparation, size, strokeWidth, Coloriser.colorise(userTokenColor, getColorisation()), g);
	}

	public Rectangle2D getBoundingBoxInLocalSpace() {
		return new Rectangle2D.Double(-size/2, -size/2, size, size);	}


	public int hitTestInLocalSpace(Point2D pointInLocalSpace) {
		if (pointInLocalSpace.distanceSq(0, 0) < size*size/4)
			return 1;
		else
			return 0;
	}

	public Rectangle2D getBoundingBox() {
		return new Rectangle2D.Double(-size/2, -size/2, size, size);
	}

	@Override
	public JPopupMenu createPopupMenu(ScriptedActionListener actionListener) {
		JPopupMenu popup = new JPopupMenu();

		ScriptedActionMenuItem addToken = new ScriptedActionMenuItem(new AddTokenAction(getReferencedComponent().getID()));
		addToken.addScriptedActionListener(actionListener);

		ScriptedActionMenuItem removeToken = new ScriptedActionMenuItem(new RemoveTokenAction((Place)getReferencedComponent()));
		removeToken.addScriptedActionListener(actionListener);

		popup.add(new JLabel ("Place"));
		popup.addSeparator();
		popup.add(addToken);
		popup.add(removeToken);

		return popup;
	}

	public Place getReferencedPlace() {
		return (Place)getReferencedComponent();
	}
}
