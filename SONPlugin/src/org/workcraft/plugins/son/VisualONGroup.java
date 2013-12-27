package org.workcraft.plugins.son;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.GlyphVector;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.HashSet;

import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathGroup;
import org.workcraft.dom.visual.BoundingBoxHelper;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.VisualComment;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualGroup;
import org.workcraft.gui.Coloriser;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.plugins.son.connections.VisualSONConnection;
import org.workcraft.plugins.son.elements.VisualCondition;
import org.workcraft.plugins.son.elements.VisualEvent;
import org.workcraft.util.Hierarchy;

public class VisualONGroup extends VisualGroup{

	private static final float strokeWidth = 0.03f;
	private static final float frameDepth = 0.5f;

	private GlyphVector glyphVector;
	private Rectangle2D contentsBB = null;
	private Rectangle2D labelBB = null;

	private ONGroup mathGroup = null;
	private Color fillColor = Color.WHITE;


	public VisualONGroup(){
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

	public VisualONGroup(ONGroup mathGroup)	{
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
	public void add(Node node) {
		if(node instanceof VisualComponent){
			Node mathNode = ((VisualComponent)node).getReferencedComponent();
			MathGroup oldGroup = (MathGroup)mathNode.getParent();
			HashSet<Node> mathNodes = new HashSet<Node>();
			mathNodes.add(mathNode);
			oldGroup.reparent(mathNodes, mathGroup);
		}
		super.add(node);
	}

	@Override
	public void remove(Node node) {
		super.remove(node);
	}

	@Override
	public void add(Collection<Node> nodes){
		for(Node node : nodes)
			this.add(node);
	}

	@Override
	public void remove(Collection<Node> nodes){
		super.remove(nodes);
	}

	@Override
	public Rectangle2D getBoundingBoxInLocalSpace()
	{
		Rectangle2D bb = getContentsBoundingBox();

		// Increase bb by the label height (to include the latter into the bb)
		if(labelBB != null)
			bb.add(bb.getMinX(), bb.getMinY() - labelBB.getHeight());


		return bb;
	}

	private Rectangle2D getContentsBoundingBox(){
		Rectangle2D bb = null;

		for(VisualCondition v: Hierarchy.getChildrenOfType(this, VisualCondition.class))
			bb = BoundingBoxHelper.union(bb, v.getBoundingBox());

		for(VisualEvent v: Hierarchy.getChildrenOfType(this, VisualEvent.class))
			bb = BoundingBoxHelper.union(bb, v.getBoundingBox());

		for(VisualComment v: Hierarchy.getChildrenOfType(this, VisualComment.class))
			bb = BoundingBoxHelper.union(bb, v.getBoundingBox());

		for(VisualSONConnection v: Hierarchy.getChildrenOfType(this, VisualSONConnection.class))
			bb = BoundingBoxHelper.union(bb, v.getBoundingBox());

		if (bb == null) bb = contentsBB;
		else
		bb.setRect(bb.getMinX() - frameDepth, bb.getMinY() - frameDepth,
				   bb.getWidth() + 2.0 * frameDepth, bb.getHeight() + 2.0 * frameDepth);

		if (bb == null) bb = new Rectangle2D.Double(0, 0, 1, 1);

		contentsBB = (Rectangle2D) bb.clone();

		return bb;
	}

	@Override
	public void draw(DrawRequest r)
	{
		Graphics2D g = r.getGraphics();
		Color colorisation = r.getDecoration().getColorisation();

		Rectangle2D bb = getContentsBoundingBox();

		if (bb != null && getParent() != null)
		{
//			g.setColor(Coloriser.colorise(fillColor, colorisation));
//			g.fill(bb);
			g.setColor(Coloriser.colorise(getForegroundColor(), colorisation));
			g.setStroke(new BasicStroke( 2 * strokeWidth , BasicStroke.CAP_BUTT,BasicStroke.JOIN_ROUND,
					3.0f, new float[]{ 2 * strokeWidth , 5 * strokeWidth,}, 0f));
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

	@Override
	public boolean hitTestInLocalSpace(Point2D p)
	{
		return getContentsBoundingBox().contains(p);
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

	public Collection<VisualComment> getVisualComment(){

		return Hierarchy.getDescendantsOfType(this, VisualComment.class);

	}

	public Collection<VisualComponent> getVisualComponents(){

		return Hierarchy.getDescendantsOfType(this, VisualComponent.class);

	}

}
