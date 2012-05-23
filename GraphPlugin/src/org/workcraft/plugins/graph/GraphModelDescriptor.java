package org.workcraft.plugins.graph;

import org.workcraft.dom.ModelDescriptor;
import org.workcraft.dom.VisualModelDescriptor;
import org.workcraft.dom.math.MathModel;

public class GraphModelDescriptor implements ModelDescriptor
{
	@Override
	public String getDisplayName() {
		return "Directed Graph";
	}

	@Override
	public MathModel createMathModel() {
		return new Graph();
	}

	@Override
	public VisualModelDescriptor getVisualModelDescriptor() {
		return new VisualGraphModelDescriptor();
	}
}