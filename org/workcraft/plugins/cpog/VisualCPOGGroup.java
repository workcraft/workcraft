package org.workcraft.plugins.cpog;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

import org.workcraft.dom.visual.BoundingBoxHelper;
import org.workcraft.dom.visual.VisualGroup;
import org.workcraft.gui.Coloriser;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.util.Hierarchy;

public class VisualCPOGGroup extends VisualGroup
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
	private static final float minVariableWidth = 1f;
	private final static Font labelFont = new Font("Sans-serif", Font.PLAIN, 1).deriveFont(0.5f);

	private Rectangle2D labelBB = null;
	private Rectangle2D encodingBB = null;

	private String label = "";
	private Encoding encoding = new Encoding();

	public VisualCPOGGroup()
	{
		System.out.println("creating VisualCpogGroup");
		addPropertyDeclaration(new PropertyDeclaration(this, "Label", "getLabel", "setLabel", String.class));
		addPropertyDeclaration(new PropertyDeclaration(this, "Encoding", "getEncoding", "setEncoding", Encoding.class));
	}

	@Override
	public Rectangle2D getBoundingBoxInLocalSpace()
	{
		Rectangle2D bb = getContentsBoundingBox();

		// Increase bb by the label height (to include the latter into the bb)
		if(labelBB != null)
			bb.add(bb.getMinX(), bb.getMinY() - labelBB.getHeight());

		// Increase bb by the encoding height (to include the latter into the bb)
		if(encodingBB != null)
			bb.add(bb.getMinX(), bb.getMaxY() + encodingBB.getHeight());



		return bb;
	}

	private Rectangle2D getContentsBoundingBox() {
		Rectangle2D bb = null;

		for(VisualVertex v : Hierarchy.getChildrenOfType(this, VisualVertex.class))
			bb = BoundingBoxHelper.union(bb, v.getBoundingBoxWithLabel());

		bb.setRect(bb.getMinX() - frameDepth, bb.getMinY() - frameDepth,
				   bb.getWidth() + 2.0 * frameDepth, bb.getHeight() + 2.0 * frameDepth);

		return bb;
	}

	@Override
	public void draw(Graphics2D g)
	{
		Rectangle2D bb = getContentsBoundingBox();

		if (bb != null && getParent() != null)
		{
			g.setColor(Coloriser.colorise(Color.WHITE, getColorisation()));
			g.fill(bb);
			g.setColor(Coloriser.colorise(Color.BLACK, getColorisation()));
			g.setStroke(new BasicStroke(strokeWidth));
			g.draw(bb);

			// draw label

			GlyphVector glyphs = labelFont.createGlyphVector(g.getFontRenderContext(), label);

			labelBB = BoundingBoxHelper.expand(glyphs.getLogicalBounds(), 0.4, 0.2);

			Point2D labelPosition = new Point2D.Double(bb.getMaxX() - labelBB.getMaxX(), bb.getMinY() - labelBB.getMaxY());

			g.setColor(Coloriser.colorise(Color.WHITE, getColorisation()));
			g.fill(getLabelBB());
			g.setColor(Coloriser.colorise(Color.BLACK, getColorisation()));
			g.drawGlyphVector(glyphs, (float) labelPosition.getX(), (float) labelPosition.getY());
			g.draw(getLabelBB());

			// draw encoding

			encodingBB = null;

			Set<Variable> sortedVariables = new TreeSet<Variable>(new ReverseComparator());
			sortedVariables.addAll(encoding.getStates().keySet());



			for(Variable var : sortedVariables)
			{
				String text = var.getLabel();

				glyphs = labelFont.createGlyphVector(g.getFontRenderContext(), text);

				bb = glyphs.getLogicalBounds();
				bb = BoundingBoxHelper.expand(bb, 0.4, 0.2);

				if (bb.getWidth() < minVariableWidth) bb = BoundingBoxHelper.expand(bb, minVariableWidth - bb.getWidth(), 0);

				Point2D labelPosition = new Point2D.Double(bb.getMaxX() - labelBB.getMaxX(), bb.getMinY() - labelBB.getMaxY());

				g.setColor(Coloriser.colorise(Color.WHITE, getColorisation()));
				g.fill(getLabelBB());
				g.setColor(Coloriser.colorise(Color.BLACK, getColorisation()));
				g.drawGlyphVector(glyphs, (float) labelPosition.getX(), (float) labelPosition.getY());
				g.draw(getLabelBB());
				//= encoding.getState(var).toString();

			}
		}
	}

	@Override
	public boolean hitTestInLocalSpace(Point2D p)
	{
		return
			getContentsBoundingBox().contains(p) ||
			getLabelBB().contains(p);
	}

	private Rectangle2D getLabelBB() {
		Rectangle2D bb = getContentsBoundingBox();
		return new Rectangle2D.Double(bb.getMaxX() - labelBB.getWidth(), bb.getMinY() - labelBB.getHeight(), labelBB.getWidth(), labelBB.getHeight());
	}

	public void setLabel(String label)
	{
		this.label = label;
		sendNotification(new PropertyChangedEvent(this, "label"));
	}

	public String getLabel()
	{
		return label;
	}

	public void setEncoding(Encoding encoding)
	{
		this.encoding = encoding;
	}

	public Encoding getEncoding()
	{
		return encoding;
	}
}
