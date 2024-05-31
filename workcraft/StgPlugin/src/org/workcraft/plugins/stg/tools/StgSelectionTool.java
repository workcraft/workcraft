package org.workcraft.plugins.stg.tools;

import org.workcraft.dom.Node;
import org.workcraft.dom.visual.*;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.gui.events.GraphEditorKeyEvent;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.tools.Decoration;
import org.workcraft.gui.tools.Decorator;
import org.workcraft.gui.tools.GraphEditor;
import org.workcraft.gui.tools.SelectionTool;
import org.workcraft.gui.tools.editors.AbstractInplaceEditor;
import org.workcraft.gui.tools.editors.NameInplaceEditor;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.petri.VisualReadArc;
import org.workcraft.plugins.petri.commands.ContractTransitionTransformationCommand;
import org.workcraft.plugins.petri.exceptions.ImpossibleContractionException;
import org.workcraft.plugins.petri.exceptions.SuspiciousContractionException;
import org.workcraft.plugins.stg.*;
import org.workcraft.plugins.stg.commands.*;
import org.workcraft.utils.DesktopApi;
import org.workcraft.utils.Hierarchy;

import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class StgSelectionTool extends SelectionTool {

    private boolean isInRelocationMode = false;
    private VisualNamedTransition relocationTransition = null;
    private VisualConnection relocationConnection = null;
    private final Set<VisualConnection> goodRelocationConnections = new HashSet<>();
    private String relocationIssue = null;

    @Override
    public void mouseClicked(GraphEditorMouseEvent e) {
        if ((e.getButton() == MouseEvent.BUTTON1) && (e.getClickCount() > 1)) {
            GraphEditor editor = e.getEditor();
            VisualModel model = editor.getModel();
            Node node = HitMan.hitFirstInCurrentLevel(e.getPosition(), model);
            if (node instanceof VisualPlace) {
                VisualPlace place = (VisualPlace) node;
                toggleToken(place.getReferencedComponent(), editor);
                return;
            }
            if (node instanceof VisualImplicitPlaceArc) {
                if (e.isMenuKeyDown()) {
                    VisualImplicitPlaceArc implicitPlaceArc = (VisualImplicitPlaceArc) node;
                    toggleToken(implicitPlaceArc.getImplicitPlace(), editor);
                    return;
                }
            }
            if (node instanceof VisualSignalTransition) {
                VisualSignalTransition transition = (VisualSignalTransition) node;
                boolean processed = false;
                if (e.isMenuKeyDown()) {
                    toggleSignalType(transition.getReferencedComponent(), editor);
                    processed = true;
                }
                if (e.isShiftKeyDown()) {
                    toggleDirection(transition.getReferencedComponent(), editor);
                    processed = true;
                }
                if (processed) {
                    return;
                }
            }
            if (node instanceof VisualNamedTransition) {
                VisualNamedTransition transition = (VisualNamedTransition) node;
                AbstractInplaceEditor textEditor = new NameInplaceEditor(editor, transition, false);
                textEditor.edit(transition.getName(), transition.getNameFont(),
                        transition.getNameOffset(), Alignment.CENTER, false);
                return;
            }
        }
        super.mouseClicked(e);
    }

    private void toggleToken(Place place, GraphEditor editor) {
        if (place.getTokens() <= 1) {
            editor.getWorkspaceEntry().saveMemento();
            if (place.getTokens() == 1) {
                place.setTokens(0);
            } else {
                place.setTokens(1);
            }
        }
    }

    private void toggleSignalType(SignalTransition transition, GraphEditor editor) {
        editor.getWorkspaceEntry().saveMemento();
        Signal.Type type = transition.getSignalType();
        transition.setSignalType(type.toggle());
    }

    private void toggleDirection(SignalTransition transition, GraphEditor editor) {
        editor.getWorkspaceEntry().saveMemento();
        SignalTransition.Direction direction = transition.getDirection();
        VisualModel model = editor.getModel();
        if (model instanceof VisualStg) {
            Stg stg = (Stg) model.getMathModel();
            stg.setDirection(transition, direction.toggle());
        }
    }

    @Override
    public String getHintText(final GraphEditor editor) {
        if (getDragState() == DragState.NONE) {
            if (getCurrentNode() instanceof VisualNamedTransition) {
                return "Double-click to rename transition. " +
                        "Shift+double-click to change sign. " +
                        DesktopApi.getMenuKeyName() + "+double-click to change signal type.";
            }
            if (getCurrentNode() instanceof VisualImplicitPlaceArc) {
                return "Double-click to add connection control point. " +
                        DesktopApi.getMenuKeyName() + "+double-click to toggle implicit place marking.";
            }
            if (getCurrentNode() instanceof VisualPlace) {
                return "Double-click to toggle place marking.";
            }
        }
        if ((getDragState() == DragState.MOVE) && (relocationTransition != null)) {
            if (isInRelocationMode) {
                return "Drop transition on one of the unshaded connections.";
            } else {
                return "Hold Ctrl to contract transition and insert it into directed connection.";
            }
        }
        return super.getHintText(editor);
    }

    @Override
    public void mouseMoved(GraphEditorMouseEvent e) {
        relocationConnection = null;
        isInRelocationMode = (getDragState() == DragState.MOVE) && e.isMenuKeyDown();
        if (isInRelocationMode) {
            Point2D pos = e.getPosition();
            VisualModel model = e.getModel();
            relocationConnection = (VisualConnection) HitMan.hitFirstChild(pos, model.getRoot(),
                    goodRelocationConnections::contains);

            showIssue(e.getEditor(), relocationIssue);
        }
        super.mouseMoved(e);
    }

    @Override
    public void startDrag(GraphEditorMouseEvent e) {
        super.startDrag(e);
        if (getDragState() == DragState.MOVE) {
            GraphEditor editor = e.getEditor();
            VisualStg stg = (VisualStg) editor.getModel();
            Collection<VisualNode> selection = stg.getSelection();
            if (selection.size() == 1) {
                VisualNode selectedNode = selection.iterator().next();
                if (selectedNode instanceof VisualNamedTransition) {
                    relocationTransition = (VisualNamedTransition) selectedNode;
                    goodRelocationConnections.clear();
                    ContractTransitionTransformationCommand contractionCommand
                            = new ContractNamedTransitionTransformationCommand();
                    try {
                        contractionCommand.validateContraction(stg, relocationTransition);
                        relocationIssue = null;
                        Set<VisualConnection> adjacentConnections = stg.getConnections(relocationTransition);
                        goodRelocationConnections.addAll(Hierarchy.getChildrenOfType(stg.getCurrentLevel(),
                                VisualConnection.class,
                                node -> !(node instanceof VisualReadArc) && !adjacentConnections.contains(node)));

                    } catch (SuspiciousContractionException | ImpossibleContractionException exception) {
                        relocationIssue = exception.getMessage();
                    }
                }
            }
        }
    }

    @Override
    public void finishDrag(GraphEditorMouseEvent e) {
        hideIssue(e.getEditor());
        if ((getDragState() == DragState.MOVE) && e.isMenuKeyDown() &&
                (relocationTransition != null) && (relocationConnection != null)) {

            VisualStg stg = (VisualStg) e.getEditor().getModel();

            // Firstly, insert a new transition into the mouse position of destination connection
            relocationConnection.setSplitPoint(ConnectionHelper.getNearestLocationOnConnection(
                    relocationConnection, e.getPosition()));

            VisualNamedTransition insertedTransition = getInsertionCommand(relocationTransition)
                    .insertTransitionIntoConnection(stg, relocationConnection);

            // Then, rename newly inserted transition and restore position of the original transition
            stg.setMathName(insertedTransition, relocationTransition.getName());
            relocationTransition.setRootSpacePosition(e.getStartPosition());

            // Finally, contract original transition (transition contraction could change the destination connection)
            new ContractNamedTransitionTransformationCommand().removeOrContractTransition(stg, relocationTransition);
        }
        clearRelocationMode();
        super.finishDrag(e);
    }

    private AbstractInsertTransformationCommand getInsertionCommand(VisualNamedTransition transition) {
        if (transition instanceof VisualSignalTransition) {
            Signal.Type signalType = ((VisualSignalTransition) transition).getSignalType();
            switch (signalType) {
            case INPUT:
                return new InsertInputTransformationCommand();
            case OUTPUT:
                return new InsertOutputTransformationCommand();
            case INTERNAL:
                return new InsertInternalTransformationCommand();
            }
        }
        return new InsertDummyTransformationCommand();
    }

    @Override
    public Decorator getDecorator(final GraphEditor editor) {
        return node -> {
            if (isInRelocationMode && (node instanceof VisualConnection)) {
                if ((relocationConnection != null) && (node == relocationConnection)) {
                    return Decoration.Highlighted.INSTANCE;
                }
                if (!goodRelocationConnections.contains(node)) {
                    return Decoration.Shaded.INSTANCE;
                }
            }
            return super.getDecorator(editor).getDecoration(node);
        };
    }

    @Override
    public boolean keyPressed(GraphEditorKeyEvent e) {
        if (e.isMenuKeyDown() && (getDragState() == DragState.MOVE)) {
            isInRelocationMode = true;
            showIssue(e.getEditor(), relocationIssue);
            e.getEditor().repaint();
        }
        return super.keyPressed(e);
    }
    @Override
    public boolean keyReleased(GraphEditorKeyEvent e) {
        if (!e.isMenuKeyDown() || (getDragState() != DragState.MOVE)) {
            isInRelocationMode = false;
            hideIssue(e.getEditor());
            e.getEditor().repaint();
        }
        return super.keyReleased(e);
    }

    @Override
    public void activated(final GraphEditor editor) {
        super.activated(editor);
        clearRelocationMode();
    }

    @Override
    public void deactivated(GraphEditor editor) {
        super.deactivated(editor);
        clearRelocationMode();
    }

    private void clearRelocationMode() {
        isInRelocationMode = false;
        relocationTransition = null;
        relocationConnection = null;
        goodRelocationConnections.clear();
        relocationIssue = null;
    }

}
