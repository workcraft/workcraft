package org.workcraft.plugins.stg.tools;

import java.awt.Cursor;

import org.workcraft.dom.math.MathNode;
import org.workcraft.exceptions.NodeCreationException;
import org.workcraft.gui.DesktopApi;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.graph.generators.DefaultNodeGenerator;
import org.workcraft.gui.graph.tools.GraphEditor;
import org.workcraft.gui.graph.tools.NodeGeneratorTool;
import org.workcraft.plugins.stg.Signal;
import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.util.GUI;

public class StgSignalTransitionGeneratorTool extends NodeGeneratorTool {
    static boolean shiftKeyDown;
    static boolean menuKeyDown;

    public StgSignalTransitionGeneratorTool() {
        super(new DefaultNodeGenerator(SignalTransition.class) {
            @Override
            public MathNode createMathNode() throws NodeCreationException {
                MathNode node = super.createMathNode();
                SignalTransition t = (SignalTransition) node;
                t.setSignalType(shiftKeyDown ? Signal.Type.INPUT : Signal.Type.OUTPUT);
                t.setDirection(menuKeyDown ? SignalTransition.Direction.MINUS : SignalTransition.Direction.PLUS);
                return node;
            }
        });
    }

    @Override
    public void mousePressed(GraphEditorMouseEvent e) {
        shiftKeyDown = e.isShiftKeyDown();
        menuKeyDown = e.isMenuKeyDown();
        super.mousePressed(e);
    }

    @Override
    public String getHintText(final GraphEditor editor) {
        return "Click to create rising (or falling with " +
                DesktopApi.getMenuKeyMaskName() +
                ") transition of output (or input with Shift) signal.";
    }

    @Override
    public Cursor getCursor(boolean menuKeyDown, boolean shiftKeyDown, boolean altKeyDown) {
        if (shiftKeyDown) {
            if (menuKeyDown) {
                return GUI.createCursorFromSVG("images/stg-node-signal_transition-input_minus.svg");
            } else {
                return GUI.createCursorFromSVG("images/stg-node-signal_transition-input_plus.svg");
            }
        } else {
            if (menuKeyDown) {
                return GUI.createCursorFromSVG("images/stg-node-signal_transition-output_minus.svg");
            } else {
                return GUI.createCursorFromSVG("images/stg-node-signal_transition-output_plus.svg");
            }
        }
    }

}

