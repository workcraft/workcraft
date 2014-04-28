package org.workcraft.plugins.son;

import java.awt.Color;

import org.workcraft.dom.visual.VisualPage;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;

public class VisualBlock extends VisualPage{

	private Block mathBlock;

	public VisualBlock(Block refNode) {
		super(refNode);
		this.mathBlock = refNode;
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

	public Block getMathBlock(){
		return mathBlock;
	}

	public void setMathBlock(Block mathBlock){
		this.mathBlock = mathBlock;
	}

}
