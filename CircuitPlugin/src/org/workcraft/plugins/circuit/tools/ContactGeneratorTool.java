package org.workcraft.plugins.circuit.tools;

import java.awt.event.MouseEvent;

import org.workcraft.dom.math.MathNode;
import org.workcraft.exceptions.NodeCreationException;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.graph.tools.DefaultNodeGenerator;
import org.workcraft.gui.graph.tools.NodeGeneratorTool;
import org.workcraft.plugins.circuit.FunctionContact;

public class ContactGeneratorTool extends NodeGeneratorTool {
    static boolean shiftPressed;

    public ContactGeneratorTool() {
        super(new DefaultNodeGenerator(FunctionContact.class) {
            @Override
            public MathNode createMathNode() throws NodeCreationException {
                MathNode node = super.createMathNode();
                ((FunctionContact)node).setIOType(shiftPressed ? FunctionContact.IOType.INPUT : FunctionContact.IOType.OUTPUT);
                return node;
            }
        });
    }

    @Override
    public void mousePressed(GraphEditorMouseEvent e) {
        shiftPressed = (e.getModifiers() & MouseEvent.SHIFT_DOWN_MASK) != 0;
        super.mousePressed(e);
    }

    @Override
    public String getHintMessage() {
        return "Click to create an output port (hold Shift for input port).";
    }
}

