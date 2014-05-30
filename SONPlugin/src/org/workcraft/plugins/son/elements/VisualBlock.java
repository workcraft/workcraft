package org.workcraft.plugins.son.elements;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.GlyphVector;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualPage;
import org.workcraft.observation.TransformChangedEvent;
import org.workcraft.observation.TransformChangingEvent;
import org.workcraft.plugins.son.connections.VisualSONConnection;
import org.workcraft.plugins.son.connections.VisualSONConnection.SONConnectionType;
import org.workcraft.plugins.son.tools.ErrTracingDisable;
import org.workcraft.util.Hierarchy;

public class VisualBlock extends VisualPage implements VisualEventNode{
	private Map<VisualComponent[], SONConnectionType> inputRelations;
	private Map<VisualComponent[], SONConnectionType> outputRelations;
	private Block mathBlock;

	public VisualBlock(Block refNode) {
		super(refNode);
		this.mathBlock = refNode;
		inputRelations = new HashMap<VisualComponent[], SONConnectionType>();
		outputRelations = new HashMap<VisualComponent[], SONConnectionType>();
	}

	public void drawFault(DrawRequest r){
		if (ErrTracingDisable.showErrorTracing()) {
			Graphics2D g = r.getGraphics();
			GlyphVector glyphVector=null;
			Rectangle2D labelBB=null;

			Font labelFont = new Font("Sans-serif", Font.PLAIN, 1).deriveFont(0.5f);

			Integer faultCount = new Integer(0);
			for(VisualComponent node : this.getComponents())
				if(node instanceof VisualEvent)
					if(((VisualEvent)node).isFaulty())
						faultCount++;

			glyphVector = labelFont.createGlyphVector(g.getFontRenderContext(), faultCount.toString());

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

	public void setFaulty(Boolean fault){
		((Block)getReferencedComponent()).setFaulty(fault);
	}

	public boolean isFaulty(){
		return ((Block)getReferencedComponent()).isFaulty();
	}

	public void setLabel(String label)
	{
		this.getReferencedComponent().setLabel(label);
	}

	public String getLabel()
	{
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

	public void setInputRelations(Map<VisualComponent[], SONConnectionType> inputRelations){
		this.inputRelations = inputRelations;
	}

	public void setOutputRelations(Map<VisualComponent[], SONConnectionType> outputRelations){
		this.outputRelations = outputRelations;
	}

	public Map<VisualComponent[], SONConnectionType> getInputRelations(){
		return this.inputRelations;
	}

	public Map<VisualComponent[], SONConnectionType> getOutputRelations(){
		return this.outputRelations;
	}

	@Override
	public Block getMathEventNode() {
		return getReferencedComponent();
	}



}
