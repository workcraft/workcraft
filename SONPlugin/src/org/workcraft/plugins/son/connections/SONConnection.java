package org.workcraft.plugins.son.connections;

import java.awt.Color;

import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.math.MathNode;

public class SONConnection extends MathConnection{

	private String conType;
	private static Color defaultColor = Color.BLACK;
	private Color color = defaultColor;

	public SONConnection(){
	}

	public SONConnection(MathNode first, MathNode second, String conType){
		super();
		this.conType = conType;
		setDependencies(first, second, conType);
	}

	public String getType(){
		return this.conType;
	}

	public void setType(String type){
		this.conType = type;
	}

	final public void setDependencies(MathNode first, MathNode second, String type) {
		super.setDependencies(first, second);
		this.conType = type;
	}


	/* (non-Javadoc)
	 * @see org.workcraft.dom.visual.connections.VisualConnectionInfo#getColor()
	 */
	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

}
