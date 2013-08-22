package org.workcraft.plugins.son;

import org.workcraft.dom.ModelDescriptor;
import org.workcraft.dom.VisualModelDescriptor;
import org.workcraft.dom.math.MathModel;


public class SONModelDescriptor implements ModelDescriptor{

	public String getDisplayName(){
		return "Structured Occurrence Nets";
	}

    public MathModel createMathModel(){
		return new SON();
	}

	public VisualModelDescriptor getVisualModelDescriptor(){
		return new SONVisualModelDescriptor();
	}


}
