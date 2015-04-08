package org.workcraft.gui.graph.tools;

import java.awt.Color;
import java.awt.Graphics2D;

import org.workcraft.dom.math.CommentNode;
import org.workcraft.dom.math.MathNode;
import org.workcraft.exceptions.NodeCreationException;
import org.workcraft.util.GUI;

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
	public void drawInScreenSpace(GraphEditor editor, Graphics2D g) {
		GUI.drawEditorMessage(editor, g, Color.BLACK, "Click to create a text label.");
	}
}
