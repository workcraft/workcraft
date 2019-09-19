package org.workcraft.plugins.stg.tools;

import java.awt.Cursor;

import org.workcraft.dom.math.MathNode;
import org.workcraft.exceptions.NodeCreationException;
import org.workcraft.utils.DesktopApi;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.dom.generators.DefaultNodeGenerator;
import org.workcraft.gui.tools.GraphEditor;
import org.workcraft.gui.tools.NodeGeneratorTool;
import org.workcraft.plugins.stg.Signal;
import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.utils.GuiUtils;

public class SignalTransitionGeneratorTool extends NodeGeneratorTool {

    private static boolean shiftKeyDown;
    private static boolean menuKeyDown;

    public SignalTransitionGeneratorTool() {
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
                DesktopApi.getMenuKeyName() +
                ") transition of output (or input with Shift) signal.";
    }

    @Override
    public Cursor getCursor(boolean menuKeyDown, boolean shiftKeyDown, boolean altKeyDown) {
        if (shiftKeyDown) {
            if (menuKeyDown) {
                return GuiUtils.createCursorFromSVG("images/stg-node-signal_transition-input_minus.svg");
            } else {
                return GuiUtils.createCursorFromSVG("images/stg-node-signal_transition-input_plus.svg");
            }
        } else {
            if (menuKeyDown) {
                return GuiUtils.createCursorFromSVG("images/stg-node-signal_transition-output_minus.svg");
            } else {
                return GuiUtils.createCursorFromSVG("images/stg-node-signal_transition-output_plus.svg");
            }
        }
    }

}

