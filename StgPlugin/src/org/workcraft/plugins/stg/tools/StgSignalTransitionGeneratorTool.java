package org.workcraft.plugins.stg.tools;

import java.awt.event.MouseEvent;

import org.workcraft.dom.math.MathNode;
import org.workcraft.exceptions.NodeCreationException;
import org.workcraft.gui.DesktopApi;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.graph.tools.DefaultNodeGenerator;
import org.workcraft.gui.graph.tools.NodeGeneratorTool;
import org.workcraft.plugins.stg.SignalTransition;

public class StgSignalTransitionGeneratorTool  extends NodeGeneratorTool {
    static boolean shiftPressed;
    static boolean controlPressed;

    public StgSignalTransitionGeneratorTool() {
        super(new DefaultNodeGenerator(SignalTransition.class) {
            @Override
            public MathNode createMathNode() throws NodeCreationException {
                MathNode node = super.createMathNode();
                SignalTransition t = (SignalTransition) node;
                t.setSignalType(shiftPressed ? SignalTransition.Type.INPUT : SignalTransition.Type.OUTPUT);
                t.setDirection(controlPressed ? SignalTransition.Direction.PLUS : SignalTransition.Direction.MINUS);
                return node;
            }
        });
    }

    @Override
    public void mousePressed(GraphEditorMouseEvent e) {
        shiftPressed = (e.getModifiers() & MouseEvent.SHIFT_DOWN_MASK) != 0;
        controlPressed = (e.getModifiers() & DesktopApi.getMenuKeyMouseMask()) != 0;
        super.mousePressed(e);
    }

    @Override
    public String getHintMessage() {
        return "Click to create falling (or rising with " + DesktopApi.getMenuKeyMaskName() + ") transition of output (or input with Shift) signal.";
    }
}

