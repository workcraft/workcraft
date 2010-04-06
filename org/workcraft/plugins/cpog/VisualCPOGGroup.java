package org.workcraft.plugins.cpog;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.workcraft.dom.visual.BoundingBoxHelper;
import org.workcraft.dom.visual.VisualGroup;
import org.workcraft.gui.Coloriser;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.util.Hierarchy;

public class VisualCPOGGroup extends VisualGroup
{
	private static final float frameDepth = 0.25f;
	private static final float strokeWidth = 0.03f;

	private String label = "";
	private Encoding encoding = new Encoding();

	public VisualCPOGGroup()
	{
		addPropertyDeclaration(new PropertyDeclaration(this, "Label", "getLabel", "setLabel", String.class));
		addPropertyDeclaration(new PropertyDeclaration(this, "Encoding", "getEncoding", "setEncoding", Encoding.class));
	}

	@Override
	public Rectangle2D getBoundingBoxInLocalSpace()
	{
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
		Rectangle2D bb = getBoundingBoxInLocalSpace();

		if (bb != null && getParent() != null)
		{
			g.setColor(Coloriser.colorise(Color.BLACK, getColorisation()));
			g.setStroke(new BasicStroke(strokeWidth));
			g.draw(bb);
		}
	}

	@Override
	public boolean hitTestInLocalSpace(Point2D pointInLocalSpace)
	{
		return getBoundingBoxInLocalSpace().contains(pointInLocalSpace);
	}

	public void setLabel(String label)
	{
		this.label = label;
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
