package org.workcraft.gui.graph.tools;

import org.workcraft.dom.math.CommentNode;
import org.workcraft.dom.math.MathNode;
import org.workcraft.exceptions.NodeCreationException;

public class CommentGeneratorTool extends NodeGeneratorTool {

	public CommentGeneratorTool() {
		super(new DefaultNodeGenerator(CommentNode.class)
		{
			@Override
			public MathNode createMathNode() throws NodeCreationException {
				MathNode node = super.createMathNode();
				return node;
			}
		});
	}

	@Override
	public String getHintMessage() {
		return "Click to create a text label.";
	}
}
