package org.workcraft.plugins.son;

import java.awt.Color;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualPage;
import org.workcraft.observation.TransformChangedEvent;
import org.workcraft.observation.TransformChangingEvent;
import org.workcraft.plugins.son.connections.VisualSONConnection;
import org.workcraft.plugins.son.connections.VisualSONConnection.SONConnectionType;
import org.workcraft.util.Hierarchy;

public class VisualBlock extends VisualPage{
	private Map<VisualComponent[], SONConnectionType> inputRelations;
	private Map<VisualComponent[], SONConnectionType> outputRelations;
	private Block mathBlock;

	public VisualBlock(Block refNode) {
		super(refNode);
		this.mathBlock = refNode;
		inputRelations = new HashMap<VisualComponent[], SONConnectionType>();
		outputRelations = new HashMap<VisualComponent[], SONConnectionType>();
	}

	@Override
	public void setIsCollapsed(boolean isCollapsed) {
		sendNotification(new TransformChangingEvent(this));
		this.getMathBlock().setIsCollapsed(isCollapsed);
		sendNotification(new TransformChangedEvent(this));
	}

	@Override
	public boolean getIsCollapsed() {
		return  this.getMathBlock().getIsCollapsed();
	}


	public void setLabel(String label)
	{
		this.getMathBlock().setLabel(label);
	}

	public String getLabel()
	{
		return this.getMathBlock().getLabel();
	}

	@Override
	public void setForegroundColor(Color color){
		this.getMathBlock().setForegroundColor(color);
	}

	@Override
	public Color getForegroundColor(){
		return this.getMathBlock().getForegroundColor();
	}

	@Override
	public void setFillColor(Color color){
		this.getMathBlock().setFillColor(color);
	}

	@Override
	public Color getFillColor(){
		return this.getMathBlock().getFillColor();
	}

	public Block getMathBlock(){
		return mathBlock;
	}

	public void setMathBlock(Block mathBlock){
		this.mathBlock = mathBlock;
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



}
