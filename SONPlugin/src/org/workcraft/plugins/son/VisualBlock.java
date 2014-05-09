package org.workcraft.plugins.son;

import java.awt.Color;

import org.workcraft.dom.visual.VisualPage;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.observation.TransformChangedEvent;
import org.workcraft.observation.TransformChangingEvent;

public class VisualBlock extends VisualPage{

	private Block mathBlock;

	public VisualBlock(Block refNode) {
		super(refNode);
		//set default fill color

		this.mathBlock = refNode;
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

}
