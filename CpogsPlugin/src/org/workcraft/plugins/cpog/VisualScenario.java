package org.workcraft.plugins.cpog;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.workcraft.dom.visual.BoundingBoxHelper;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.VisualGroup;
import org.workcraft.gui.Coloriser;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.plugins.cpog.optimisation.booleanvisitors.FormulaRenderingResult;
import org.workcraft.plugins.cpog.optimisation.booleanvisitors.FormulaToGraphics;
import org.workcraft.util.Hierarchy;

public class VisualScenario extends VisualGroup
{
	private static final class ReverseComparator implements Comparator<Variable>
	{
		@Override
		public int compare(Variable o1, Variable o2) {
			return -o1.compareTo(o2);
		}
	}

	private static final float frameDepth = 0.25f;
	private static final float strokeWidth = 0.03f;
	private static final float minVariableWidth = 0.7f;
	private static final float minVariableHeight = 0.85f;

	private Rectangle2D contentsBB = null;
	private Rectangle2D labelBB = null;
	private Rectangle2D encodingBB = null;

	private Map<Rectangle2D, Variable> variableBBs = new HashMap<Rectangle2D, Variable>();

	private String label = "";
	private Encoding encoding = new Encoding();

	private static Font labelFont;

	static {
		try {
			labelFont = Font.createFont(Font.TYPE1_FONT, ClassLoader.getSystemResourceAsStream("fonts/eurm10.pfb")).deriveFont(0.5f);
		} catch (FontFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public VisualScenario() {
		addPropertyDeclaration(new PropertyDeclaration<VisualScenario, String>(
				this, "Label", String.class) {
			public void setter(VisualScenario object, String value) {
				object.setLabel(value);
			}
			public String getter(VisualScenario object) {
				return object.getLabel();
			}
		});

		addPropertyDeclaration(new PropertyDeclaration<VisualScenario, Encoding>(
				this, "Encoding", Encoding.class) {
			public void setter(VisualScenario object, Encoding value) {
				object.setEncoding(value);
			}
			public Encoding getter(VisualScenario object) {
				return object.getEncoding();
			}
		});
	}

	@Override
	public Rectangle2D getBoundingBoxInLocalSpace() {
		Rectangle2D bb = getContentsBoundingBox();

		// Increase bb by the label height (to include the latter into the bb)
		if(labelBB != null) {
			bb.add(bb.getMinX(), bb.getMinY() - labelBB.getHeight());
		}
		// Increase bb by the encoding height (to include the latter into the bb)
		if(encodingBB != null) {
			bb.add(bb.getMinX(), bb.getMaxY() + encodingBB.getHeight());
		}

		return bb;
	}

	private Rectangle2D getContentsBoundingBox() {
		Rectangle2D bb = null;

		for(VisualVertex v : Hierarchy.getChildrenOfType(this, VisualVertex.class)) {
			bb = BoundingBoxHelper.union(bb, v.getBoundingBox());
		}
		for(VisualVariable v : Hierarchy.getChildrenOfType(this, VisualVariable.class)) {
			bb = BoundingBoxHelper.union(bb, v.getBoundingBox());
		}
		for(VisualArc a : Hierarchy.getChildrenOfType(this, VisualArc.class)) {
			bb = BoundingBoxHelper.union(bb, a.getLabelBoundingBox());
		}
		if (bb == null) bb = contentsBB;
		else
		bb.setRect(bb.getMinX() - frameDepth, bb.getMinY() - frameDepth,
				   bb.getWidth() + 2.0 * frameDepth, bb.getHeight() + 2.0 * frameDepth);

		if (bb == null) bb = new Rectangle2D.Double(0, 0, 1, 1);

		contentsBB = (Rectangle2D) bb.clone();

		return bb;
	}

	@Override
	public void draw(DrawRequest r) {
		Graphics2D g = r.getGraphics();
		Color colorisation = r.getDecoration().getColorisation();
		Color background = r.getDecoration().getBackground();

		Rectangle2D bb = getContentsBoundingBox();

		if (bb != null && getParent() != null)
		{
//			g.setColor(Coloriser.colorise(Color.WHITE, background));
//			g.fill(bb);
			g.setColor(Coloriser.colorise(Color.BLACK, colorisation));
			g.setStroke(new BasicStroke(strokeWidth));
			g.draw(bb);

			// draw label

			FormulaRenderingResult result = FormulaToGraphics.print(label, labelFont, g.getFontRenderContext());

			labelBB = BoundingBoxHelper.expand(result.boundingBox, 0.4, 0.2);

			Point2D labelPosition = new Point2D.Double(bb.getMaxX() - labelBB.getMaxX(), bb.getMinY() - labelBB.getMaxY());

			g.setColor(Coloriser.colorise(Color.WHITE, colorisation));
			g.fill(getLabelBB());
			g.setStroke(new BasicStroke(strokeWidth));
			g.setColor(Coloriser.colorise(Color.BLACK, colorisation));
			g.draw(getLabelBB());

			AffineTransform transform = g.getTransform();
			g.translate(labelPosition.getX(), labelPosition.getY());
			g.setColor(Coloriser.colorise(Color.BLACK, colorisation));
			result.draw(g);

			g.setTransform(transform);

			// draw encoding

			encodingBB = null;

			Set<Variable> sortedVariables = new TreeSet<Variable>(new ReverseComparator());
			sortedVariables.addAll(encoding.getStates().keySet());

			double right = bb.getMaxX();
			double top = bb.getMaxY();

			variableBBs.clear();

			boolean perfectMatch = true;

			for(Variable var : sortedVariables) if (!var.getState().matches(encoding.getState(var))) perfectMatch = false;

			for(Variable var : sortedVariables)
			{
				String text = var.getLabel();

				result = FormulaToGraphics.print(text, labelFont, g.getFontRenderContext());

				bb = result.boundingBox;
				bb = BoundingBoxHelper.expand(bb, 0.4, 0.2);

				if (bb.getWidth() < minVariableWidth) bb = BoundingBoxHelper.expand(bb, minVariableWidth - bb.getWidth(), 0);
				if (bb.getHeight() < minVariableHeight) bb = BoundingBoxHelper.expand(bb, 0, minVariableHeight - bb.getHeight());

				labelPosition = new Point2D.Double(right - bb.getMaxX(), top - bb.getMinY());

				double left = right - bb.getWidth();
				double bottom = top + bb.getHeight();

				Rectangle2D tmpBB = new Rectangle2D.Double(left, top, bb.getWidth(), bb.getHeight());

				encodingBB = BoundingBoxHelper.union(encodingBB, tmpBB);

				g.setColor(Coloriser.colorise(Color.WHITE, colorisation));
				g.fill(tmpBB);
				g.setStroke(new BasicStroke(strokeWidth));
				g.setColor(Coloriser.colorise(Color.BLACK, colorisation));
				g.draw(tmpBB);

				transform = g.getTransform();
				g.translate(labelPosition.getX(), labelPosition.getY());
				g.setColor(Coloriser.colorise(Color.BLACK, colorisation));
				result.draw(g);

				g.setTransform(transform);

				variableBBs.put(tmpBB, var);

				text = encoding.getState(var).toString();
				if (text.equals("?")) text = "\u2013";

				result = FormulaToGraphics.print(text, labelFont, g.getFontRenderContext());

				bb = result.boundingBox;
				bb = BoundingBoxHelper.expand(bb, tmpBB.getWidth() - bb.getWidth(), tmpBB.getHeight() - bb.getHeight());

				labelPosition = new Point2D.Double(right - bb.getMaxX(), bottom - bb.getMinY());

				tmpBB = new Rectangle2D.Double(left, bottom, bb.getWidth(), bb.getHeight());

				encodingBB = BoundingBoxHelper.union(encodingBB, tmpBB);

				g.setColor(Coloriser.colorise(Color.WHITE, colorisation));
				g.fill(tmpBB);
				g.setStroke(new BasicStroke(strokeWidth));
				g.setColor(Coloriser.colorise(Color.BLACK, colorisation));
				g.draw(tmpBB);

				transform = g.getTransform();
				g.translate(labelPosition.getX(), labelPosition.getY());

				Color color = Color.BLACK;
				if (!var.getState().matches(encoding.getState(var))) color = Color.RED;
				if (perfectMatch) color = Color.GREEN;
				g.setColor(Coloriser.colorise(color, colorisation));
				result.draw(g);

				g.setTransform(transform);

				variableBBs.put(tmpBB, var);

				right = left;
			}
		}
	}

	public Variable getVariableAt(Point2D p) {
		Point2D q = new Point2D.Double();
		getParentToLocalTransform().transform(p, q);
		for(Rectangle2D rect : variableBBs.keySet()) {
			if (rect.contains(q)) return variableBBs.get(rect);
		}
		return null;
	}

	@Override
	public boolean hitTestInLocalSpace(Point2D p) {
		return
			getContentsBoundingBox().contains(p) ||
			getLabelBB().contains(p) ||
			encodingBB.contains(p);
	}

	private Rectangle2D getLabelBB() {
		Rectangle2D bb = getContentsBoundingBox();
		return new Rectangle2D.Double(bb.getMaxX() - labelBB.getWidth(), bb.getMinY() - labelBB.getHeight(), labelBB.getWidth(), labelBB.getHeight());
	}

	public void setLabel(String label) {
		this.label = label;
		sendNotification(new PropertyChangedEvent(this, "label"));
	}

	public String getLabel() {
		return label;
	}

	public void setEncoding(Encoding encoding) {
		this.encoding = encoding;
		sendNotification(new PropertyChangedEvent(this, "encoding"));
	}

	public Encoding getEncoding() {
		return encoding;
	}
}
