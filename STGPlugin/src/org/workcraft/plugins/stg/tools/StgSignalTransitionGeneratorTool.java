package org.workcraft.plugins.stg.tools;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;

import org.workcraft.dom.math.MathNode;
import org.workcraft.exceptions.NodeCreationException;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.graph.tools.DefaultNodeGenerator;
import org.workcraft.gui.graph.tools.GraphEditor;
import org.workcraft.gui.graph.tools.NodeGeneratorTool;
import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.util.GUI;

public class StgSignalTransitionGeneratorTool  extends NodeGeneratorTool {
	static boolean shiftPressed;
	static boolean controlPressed;

	public StgSignalTransitionGeneratorTool() {
		super(new DefaultNodeGenerator(SignalTransition.class)
		{
			@Override
			protected MathNode createMathNode() throws NodeCreationException {
				MathNode node = super.createMathNode();
				SignalTransition t = (SignalTransition)node;
				t.setSignalType(shiftPressed ? SignalTransition.Type.INPUT : SignalTransition.Type.OUTPUT);
				t.setDirection(controlPressed ? SignalTransition.Direction.PLUS : SignalTransition.Direction.MINUS);
				return node;
			}
		});
	}

	@Override
	public void mousePressed(GraphEditorMouseEvent e) {
		shiftPressed = ((e.getModifiers() & MouseEvent.SHIFT_DOWN_MASK) != 0);
		controlPressed = ((e.getModifiers() & MouseEvent.CTRL_DOWN_MASK) != 0);
		super.mousePressed(e);
	}

	@Override
	public void drawInScreenSpace(GraphEditor editor, Graphics2D g) {
		GUI.drawEditorMessage(editor, g, Color.BLACK,
				"Click to create falling (or rising with Ctrl) transition of output (or input with Shift) signal.");
	}
}

