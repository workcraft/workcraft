package org.workcraft.plugins.dfs.tools;

import org.workcraft.dom.visual.HitMan;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.tools.SelectionTool;
import org.workcraft.plugins.dfs.*;

import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

public class DfsSelectionTool extends SelectionTool {

    @Override
    public void mouseClicked(GraphEditorMouseEvent e) {
        boolean processed = false;
        VisualModel model = e.getModel();
        if ((e.getButton() == MouseEvent.BUTTON1) && (e.getClickCount() > 1)) {
            VisualNode node = HitMan.hitFirstInCurrentLevel(e.getPosition(), model);
            if (node instanceof VisualAbstractRegister) {
                e.getEditor().getWorkspaceEntry().saveMemento();
                if (node instanceof VisualCounterflowRegister register) {
                    Point2D nodespacePosition = model.getNodeSpacePosition(e.getPosition(), register);
                    if (nodespacePosition.getY() < 0) {
                        toggleOrMarking(register.getReferencedComponent());
                    } else {
                        toggleAndMarking(register.getReferencedComponent());
                    }
                } else if (node instanceof VisualControlRegister register) {
                    Point2D nodespacePosition = model.getNodeSpacePosition(e.getPosition(), register);
                    if (nodespacePosition.getY() < 0) {
                        toggleTrueMarking(register.getReferencedComponent());
                    } else {
                        toggleFalseMarking(register.getReferencedComponent());
                    }
                } else if (node instanceof VisualBinaryRegister register) {
                    toggleMarking(register.getReferencedComponent());
                } else if (node instanceof VisualRegister register) {
                    toggleMarking(register.getReferencedComponent());
                }
                processed = true;
            }
        }
        if (!processed) {
            super.mouseClicked(e);
        }
    }

    private void toggleMarking(Register register) {
        register.setMarked(!register.isMarked());
    }

    private void toggleMarking(BinaryRegister register) {
        switch (register.getMarking()) {
            case EMPTY:
                register.setMarking(BinaryRegister.Marking.FALSE_TOKEN);
                break;
            case FALSE_TOKEN:
                register.setMarking(BinaryRegister.Marking.TRUE_TOKEN);
                break;
            case TRUE_TOKEN:
                register.setMarking(BinaryRegister.Marking.EMPTY);
                break;
        }
    }

    private void toggleTrueMarking(BinaryRegister register) {
        switch (register.getMarking()) {
            case EMPTY:
            case FALSE_TOKEN:
                register.setMarking(BinaryRegister.Marking.TRUE_TOKEN);
                break;
            case TRUE_TOKEN:
                register.setMarking(BinaryRegister.Marking.EMPTY);
                break;
        }
    }

    private void toggleFalseMarking(BinaryRegister register) {
        switch (register.getMarking()) {
            case EMPTY:
            case TRUE_TOKEN:
                register.setMarking(BinaryRegister.Marking.FALSE_TOKEN);
                break;
            case FALSE_TOKEN:
                register.setMarking(BinaryRegister.Marking.EMPTY);
                break;
        }
    }

    private void toggleOrMarking(CounterflowRegister register) {
        register.setOrMarked(!register.isOrMarked());
    }

    private void toggleAndMarking(CounterflowRegister register) {
        register.setAndMarked(!register.isAndMarked());
    }

}
