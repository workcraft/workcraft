package org.workcraft.plugins.son;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.GlyphVector;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;

import org.workcraft.dom.visual.BoundingBoxHelper;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.VisualComment;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualPage;
import org.workcraft.gui.Coloriser;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.plugins.son.connections.VisualSONConnection;
import org.workcraft.plugins.son.elements.VisualCondition;
import org.workcraft.plugins.son.elements.VisualEvent;
import org.workcraft.util.Hierarchy;

public class VisualONGroup extends VisualPage{

	private static final float strokeWidth = 0.03f;

	private GlyphVector glyphVector;
	private Rectangle2D labelBB = null;

	private ONGroup mathGroup = null;

	public VisualONGroup(ONGroup mathGroup)	{
		super(mathGroup);
		this.mathGroup = mathGroup;
		addPropertyDeclaration(new PropertyDeclaration<VisualONGroup, String>(
				this, "Label", String.class) {
			public void setter(VisualONGroup object, String value) {
				object.setLabel(value);
			}
			public String getter(VisualONGroup object) {
				return object.getLabel();
			}
		});

		addPropertyDeclaration(new PropertyDeclaration<VisualONGroup, Color>(
				this, "Foreground color", Color.class) {
			public void setter(VisualONGroup object, Color value) {
				object.setForegroundColor(value);
			}
			public Color getter(VisualONGroup object) {
				return object.getForegroundColor();
			}
		});
	}

	@Override
	public void draw(DrawRequest r)
	{
		Graphics2D g = r.getGraphics();
		Color colorisation = r.getDecoration().getColorisation();

//		Rectangle2D bb = getContentsBoundingBox();
		Rectangle2D bb = getInternalBoundingBoxInLocalSpace();

		if (bb != null && getParent() != null)
		{
//			g.setColor(Coloriser.colorise(fillColor, colorisation));
//			g.fill(bb);
			g.setColor(Coloriser.colorise(getForegroundColor(), colorisation));
			g.setStroke(new BasicStroke( 2 * strokeWidth , BasicStroke.CAP_BUTT,BasicStroke.JOIN_ROUND,
					3.0f, new float[]{ 2 * strokeWidth , 5 * strokeWidth,}, 0f));

			bb.setRect(bb.getX() - margin, bb.getY() - margin, bb.getWidth() + 2*margin, bb.getHeight() + 2*margin);

			g.draw(bb);

			// draw label
			Font labelFont = new Font("Calibri", Font.PLAIN, 1).deriveFont(0.65f);
			glyphVector = labelFont.createGlyphVector(g.getFontRenderContext(), getLabel());

			labelBB = glyphVector.getVisualBounds();

			labelBB = BoundingBoxHelper.expand(labelBB, 0.4, 0.2);

			Point2D labelPosition = new Point2D.Double(bb.getMaxX() - labelBB.getMaxX(), bb.getMinY() - labelBB.getMaxY());

			g.drawGlyphVector(glyphVector, (float)labelPosition.getX() , (float)labelPosition.getY());

		}
	}

	public void setLabel(String label)
	{
		this.getMathGroup().setLabel(label);
	}

	public String getLabel()
	{
		return this.getMathGroup().getLabel();
	}

	public void setForegroundColor(Color color){
		this.getMathGroup().setForegroundColor(color);
	}

	public Color getForegroundColor(){
		return this.getMathGroup().getForegroundColor();
	}

	public ONGroup getMathGroup(){
		return mathGroup;
	}

	public void setMathGroup(ONGroup mathGroup){
		this.mathGroup = mathGroup;
	}

	public Collection<VisualCondition> getVisualConditions(){

		return Hierarchy.getDescendantsOfType(this, VisualCondition.class);

	}

	public Collection<VisualEvent> getVisualEvents(){

		return Hierarchy.getDescendantsOfType(this, VisualEvent.class);

	}

	public Collection<VisualSONConnection> getVisualSONConnections(){

		return Hierarchy.getDescendantsOfType(this, VisualSONConnection.class);

	}

	public Collection<VisualPage> getVisualPages(){

		return Hierarchy.getDescendantsOfType(this, VisualPage.class);

	}

	public Collection<VisualBlock> getVisualBlocks(){

		return Hierarchy.getDescendantsOfType(this, VisualBlock.class);

	}

	public Collection<VisualComment> getVisualComment(){

		return Hierarchy.getDescendantsOfType(this, VisualComment.class);

	}

	public Collection<VisualComponent> getVisualComponents(){

		return Hierarchy.getDescendantsOfType(this, VisualComponent.class);

	}

}
