package org.workcraft.plugins.circuit.tools;

import org.workcraft.dom.generators.DefaultNodeGenerator;
import org.workcraft.dom.math.MathNode;
import org.workcraft.exceptions.NodeCreationException;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.tools.GraphEditor;
import org.workcraft.gui.tools.NodeGeneratorTool;
import org.workcraft.plugins.circuit.Contact.IOType;
import org.workcraft.plugins.circuit.FunctionContact;
import org.workcraft.utils.GuiUtils;

import java.awt.*;

public class ContactGeneratorTool extends NodeGeneratorTool {

    private static boolean shiftKeyDown;

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
            return GuiUtils.createCursorFromSVG("images/circuit-node-port-input.svg");
        } else {
            return GuiUtils.createCursorFromSVG("images/circuit-node-port-output.svg");
        }
    }

}

