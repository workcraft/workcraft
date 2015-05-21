package org.workcraft.plugins.son.elements;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.KeyEvent;
import java.awt.font.GlyphVector;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.Hotkey;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.Positioning;
import org.workcraft.dom.visual.RenderedText;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualPage;
import org.workcraft.gui.Coloriser;
import org.workcraft.gui.graph.tools.Decoration;
import org.workcraft.observation.TransformChangedEvent;
import org.workcraft.observation.TransformChangingEvent;
import org.workcraft.plugins.son.SONSettings;
import org.workcraft.plugins.son.connections.VisualSONConnection;
import org.workcraft.plugins.son.tools.ErrTracingDisable;
import org.workcraft.util.Hierarchy;

@Hotkey(KeyEvent.VK_B)
@DisplayName("Block")
@SVGIcon("images/icons/svg/son-block.svg")
public class VisualBlock extends VisualPage implements VisualTransitionNode{
	private Block mathBlock;
	private static final float strokeWidth = 0.06f;

	private Font font = new Font("Sans-serif", Font.PLAIN, 1).deriveFont(0.45f);
	private Color durationColor = SONSettings.getErrLabelColor();
	private Positioning durationLabelPositioning = Positioning.BOTTOM;
	private RenderedText durationRenderedText = new RenderedText("", font, durationLabelPositioning, new Point2D.Double(0.0,0.0));

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

		Rectangle2D bb = getInternalBoundingBoxInLocalSpace();
		if ((bb != null) && (getParent() != null)) {
			Graphics2D g = r.getGraphics();
			Decoration d = r.getDecoration();

			if (getIsCollapsed() && !isCurrentLevelInside()) {
				g.setColor(Coloriser.colorise(getFillColor(), d.getBackground()));
				g.fill(bb);
				g.setColor(Coloriser.colorise(getForegroundColor(), d.getColorisation()));
				g.setStroke(new BasicStroke((float) strokeWidth));
				g.draw(bb);

				double s = 2.3*size/3;
				Shape shape = new Rectangle2D.Double(-s/2, -s/2, s, s);
				g.setStroke(new BasicStroke(strokeWidth/2));
				g.setColor(Coloriser.colorise(getForegroundColor(), d.getColorisation()));
				g.draw(shape);

				drawFault(r);
			}else{
				g.setColor(Coloriser.colorise(Color.WHITE, d.getBackground()));
				g.fill(bb);
				float[] pattern = {0.2f, 0.2f};
				g.setStroke(new BasicStroke(0.02f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 1.0f, pattern, 0.0f));
				g.setColor(Coloriser.colorise(getForegroundColor(), d.getColorisation()));
				g.draw(bb);
			}

			drawNameInLocalSpace(r);
			drawLabelInLocalSpace(r);
			drawDurationInLocalSpace(r);
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

	private void cahceDurationRenderedText(DrawRequest r) {
		String duration = "d: "+ this.getDuration();
		//double o = 0.8 * size;

		Point2D offset = getOffset(durationLabelPositioning);
		if (durationLabelPositioning.ySign<0) {
			offset.setLocation(offset.getX(), offset.getY()-0.6);
		} else {
			offset.setLocation(offset.getX(), offset.getY()+0.6);
		}

		if (durationRenderedText.isDifferent(duration, font, durationLabelPositioning, offset)) {
			durationRenderedText = new RenderedText(duration, font, durationLabelPositioning, offset);
		}
	}

	protected void drawDurationInLocalSpace(DrawRequest r) {
		if (SONSettings.getTimeVisibility()) {
			cahceDurationRenderedText(r);
			Graphics2D g = r.getGraphics();
			Decoration d = r.getDecoration();
			g.setColor(Coloriser.colorise(durationColor, d.getColorisation()));
			durationRenderedText.draw(g);
		}
	}

	@Override
	public void cacheRenderedText(DrawRequest r) {
		super.cacheRenderedText(r);
		cahceDurationRenderedText(r);
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

	public String getDuration(){
		return ((Block)getReferencedComponent()).getDuration();
	}

	public void setDuration(String time){
		((Block)getReferencedComponent()).setDuration(time);
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

	public Collection<VisualEvent> getVisualEvents(){
		return Hierarchy.getDescendantsOfType(this, VisualEvent.class);
	}

	@Override
	public Block getMathTransitionNode() {
		return getReferencedComponent();
	}
}
