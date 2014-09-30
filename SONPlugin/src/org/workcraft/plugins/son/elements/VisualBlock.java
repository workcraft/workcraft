package org.workcraft.plugins.son.elements;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.font.GlyphVector;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.Hotkey;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualPage;
import org.workcraft.gui.Coloriser;
import org.workcraft.observation.TransformChangedEvent;
import org.workcraft.observation.TransformChangingEvent;
import org.workcraft.plugins.son.connections.VisualSONConnection;
import org.workcraft.plugins.son.tools.ErrTracingDisable;
import org.workcraft.util.Hierarchy;

@Hotkey(KeyEvent.VK_B)
@DisplayName("Block")
@SVGIcon("images/icons/svg/son-block.svg")
public class VisualBlock extends VisualPage implements VisualTransitionNode{
	private Block mathBlock;
	private static final float strokeWidth = 0.06f;

	public VisualBlock(Block refNode) {
		super(refNode);
		this.mathBlock = refNode;
	}

	@Override
	public void draw(DrawRequest r){
		// This is to update the rendered text for names (and labels) of group children,
		// which is necessary to calculate the bounding box before children have been drawn
		for (VisualComponent component: Hierarchy.getChildrenOfType(this, VisualComponent.class)) {
			component.cacheRenderedText(r);
		}
		cacheRenderedText(r);

		Color colorisation = r.getDecoration().getColorisation();
		Rectangle2D bb = getInternalBoundingBoxInLocalSpace();
		if ((bb != null) && (getParent() != null)) {
			Graphics2D g = r.getGraphics();
			g.setColor(Coloriser.colorise(getFillColor(), colorisation));
			g.fill(bb);
			g.setColor(Coloriser.colorise(getForegroundColor(), colorisation));
			g.setStroke(new BasicStroke(  strokeWidth , BasicStroke.CAP_BUTT,BasicStroke.JOIN_ROUND,
					3.0f, new float[]{ strokeWidth , 2 * strokeWidth,}, 0f));
			g.draw(bb);
			if (getIsCollapsed() && !isCurrentLevelInside()) {
				drawFault(r);
			}
			drawNameInLocalSpace(r);
			drawLabelInLocalSpace(r);
		}
	}

	@Override
	public void drawFault(DrawRequest r){
		if (ErrTracingDisable.showErrorTracing()) {
			Graphics2D g = r.getGraphics();
			GlyphVector glyphVector=null;
			Rectangle2D labelBB=null;

			Font labelFont = new Font("Sans-serif", Font.PLAIN, 1).deriveFont(0.5f);
			if (isFaulty()) {
				glyphVector = labelFont.createGlyphVector(g.getFontRenderContext(), "1");
			} else {
				glyphVector = labelFont.createGlyphVector(g.getFontRenderContext(), "0");
			}
			labelBB = glyphVector.getVisualBounds();
			Point2D bitPosition = new Point2D.Double(labelBB.getCenterX(), labelBB.getCenterY());
			g.drawGlyphVector(glyphVector, -(float)bitPosition.getX(), -(float)bitPosition.getY());
		}
	}

	@Override
	public void setIsCollapsed(boolean isCollapsed) {
		sendNotification(new TransformChangingEvent(this));
		this.getReferencedComponent().setIsCollapsed(isCollapsed);
		sendNotification(new TransformChangedEvent(this));
	}

	@Override
	public boolean getIsCollapsed() {
		return  this.getReferencedComponent().getIsCollapsed();
	}

	@Override
	public boolean isFaulty(){
		return ((Block)getReferencedComponent()).isFaulty();
	}

	@Override
	public void setLabel(String label) {
		super.setLabel(label);
		this.getReferencedComponent().setLabel(label);
	}

	@Override
	public String getLabel() {
		super.getLabel();
		return this.getReferencedComponent().getLabel();
	}

	@Override
	public void setForegroundColor(Color color){
		this.getReferencedComponent().setForegroundColor(color);
	}

	@Override
	public Color getForegroundColor(){
		return this.getReferencedComponent().getForegroundColor();
	}

	@Override
	public void setFillColor(Color color){
		this.getReferencedComponent().setFillColor(color);
	}

	@Override
	public Color getFillColor(){
		return this.getReferencedComponent().getFillColor();
	}

	public Block getReferencedComponent(){
		return mathBlock;
	}

	public Collection<VisualSONConnection> getVisualSONConnections(){
		return Hierarchy.getDescendantsOfType(this, VisualSONConnection.class);
	}

	@Override
	public Block getMathEventNode() {
		return getReferencedComponent();
	}
}
