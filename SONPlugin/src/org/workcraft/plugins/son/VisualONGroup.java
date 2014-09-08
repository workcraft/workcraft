package org.workcraft.plugins.son;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;

import org.workcraft.dom.visual.BoundingBoxHelper;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.Positioning;
import org.workcraft.dom.visual.VisualComment;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualPage;
import org.workcraft.gui.Coloriser;
import org.workcraft.plugins.son.connections.VisualSONConnection;
import org.workcraft.plugins.son.elements.VisualBlock;
import org.workcraft.plugins.son.elements.VisualCondition;
import org.workcraft.plugins.son.elements.VisualEvent;
import org.workcraft.util.Hierarchy;

public class VisualONGroup extends VisualPage{

	private static final float strokeWidth = 0.03f;
	private Positioning labelPositioning = Positioning.TOP_RIGHT;
	private RenderedGroupText groupLabelRenderedText = new RenderedGroupText("", labelFont, labelPositioning, getGroupLabelOffset());

	private ONGroup mathGroup = null;

	public VisualONGroup(ONGroup mathGroup)	{
		super(mathGroup);
		this.mathGroup = mathGroup;
		removePropertyDeclarationByName("Fill color");
		removePropertyDeclarationByName("Label positioning");
		removePropertyDeclarationByName("Is collapsed");
	}

	public Point2D getGroupLabelOffset() {
		Rectangle2D bb = getInternalBoundingBoxInLocalSpace();
	    double xOffset = (labelPositioning.xSign<0) ? bb.getMinX() : (labelPositioning.xSign>0) ? bb.getMaxX() : bb.getCenterX();
        double yOffset = (labelPositioning.ySign<0) ? bb.getMinY() : (labelPositioning.ySign>0) ? bb.getMaxY() : bb.getCenterY();
        return new Point2D.Double(xOffset, yOffset);
	}

	@Override
	protected void cacheLabelRenderedText(DrawRequest r) {
		if (groupLabelRenderedText.isDifferent(getLabel(), labelFont, labelPositioning, getGroupLabelOffset())) {
			groupLabelRenderedText = new RenderedGroupText(getLabel(), labelFont, labelPositioning, getGroupLabelOffset());
		}
	}

	@Override
	protected void drawLabelInLocalSpace(DrawRequest r) {
		if (getLabelVisibility()) {
			cacheLabelRenderedText(r);
			Graphics2D g = r.getGraphics();
			groupLabelRenderedText.draw(g);
		}
	}

	@Override
	public void draw(DrawRequest r)
	{
		for (VisualComponent component: Hierarchy.getChildrenOfType(this, VisualComponent.class)) {
			component.cacheRenderedText(r);
		}

		Graphics2D g = r.getGraphics();
		Color colorisation = r.getDecoration().getColorisation();

		Rectangle2D bb = getInternalBoundingBoxInLocalSpace();

		if (bb != null && getParent() != null)
		{


			//draw label
			g.setColor(Coloriser.colorise(Color.WHITE, colorisation));
			g.fill(getLabelBB());
			//g.setStroke(new BasicStroke(strokeWidth));
			g.setStroke(new BasicStroke( strokeWidth-0.005f , BasicStroke.CAP_BUTT,BasicStroke.JOIN_ROUND,
			3.0f, new float[]{  0.1f ,  0.05f,}, 0f));
			g.setColor(Coloriser.colorise(getLabelColor(), colorisation));
			g.draw(getLabelBB());
			drawLabelInLocalSpace(r);

			//draw group
			g.setColor(Coloriser.colorise(this.getForegroundColor(), colorisation));
			g.setStroke(new BasicStroke(strokeWidth));

			bb.setRect(bb.getX() - margin, bb.getY() - margin, bb.getWidth() + 2*margin, bb.getHeight() + 2*margin);

			g.draw(bb);
			drawNameInLocalSpace(r);
		}

	}

	private Rectangle2D getLabelBB() {
		Rectangle2D bb = getInternalBoundingBoxInLocalSpace();
		Rectangle2D labelBB = null;
		labelBB = BoundingBoxHelper.expand(groupLabelRenderedText.getBoundingBox(), 0.4, 0.2);
		return new Rectangle2D.Double(bb.getMaxX() - labelBB.getWidth() + margin, bb.getMinY() - labelBB.getHeight() - margin, labelBB.getWidth(), labelBB.getHeight());
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
