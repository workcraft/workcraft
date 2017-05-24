package org.workcraft.plugins.circuit.tools;

import java.awt.Cursor;

import org.workcraft.dom.math.MathNode;
import org.workcraft.exceptions.NodeCreationException;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.graph.generators.DefaultNodeGenerator;
import org.workcraft.gui.graph.tools.GraphEditor;
import org.workcraft.gui.graph.tools.NodeGeneratorTool;
import org.workcraft.plugins.circuit.Contact.IOType;
import org.workcraft.plugins.circuit.FunctionContact;
import org.workcraft.util.GUI;

public class ContactGeneratorTool extends NodeGeneratorTool {
    static boolean shiftKeyDown;

    public ContactGeneratorTool() {
        super(new DefaultNodeGenerator(FunctionContact.class) {
            @Override
            public MathNode createMathNode() throws NodeCreationException {
                MathNode node = super.createMathNode();
                FunctionContact contact = (FunctionContact) node;
                contact.setIOType(shiftKeyDown ? IOType.INPUT : IOType.OUTPUT);
                return node;
            }
        });
    }

    @Override
    public void mousePressed(GraphEditorMouseEvent e) {
        shiftKeyDown = e.isShiftKeyDown();
        super.mousePressed(e);
    }

    @Override
    public String getHintText(final GraphEditor editor) {
        return "Click to create an output port (hold Shift for input port).";
    }

    @Override
    public Cursor getCursor(boolean menuKeyDown, boolean shiftKeyDown, boolean altKeyDown) {
        if (shiftKeyDown) {
            return GUI.createCursorFromSVG("images/circuit-node-port-input.svg");
        } else {
            return GUI.createCursorFromSVG("images/circuit-node-port-output.svg");
        }
    }

}

