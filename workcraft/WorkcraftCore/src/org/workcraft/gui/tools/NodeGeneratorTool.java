package org.workcraft.gui.tools;

import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.generators.NodeGenerator;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.exceptions.NodeCreationException;
import org.workcraft.gui.editor.GraphEditorPanel;
import org.workcraft.gui.events.GraphEditorKeyEvent;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.utils.GuiUtils;
import org.workcraft.workspace.WorkspaceEntry;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

public class NodeGeneratorTool extends AbstractGraphEditorTool {

    private final NodeGenerator generator;
    private final boolean topLevelOnly;

    private VisualNode lastGeneratedNode = null;
    private String warningMessage = null;
    private Container currentLevel = null;

    public NodeGeneratorTool(NodeGenerator generator) {
        this(generator, false);
    }

    public NodeGeneratorTool(NodeGenerator generator, boolean topLevelOnly) {
        this.generator = generator;
        this.topLevelOnly = topLevelOnly;
    }

    @Override
    public Icon getIcon() {
        return generator.getIcon();
    }

    @Override
    public String getLabel() {
        return generator.getLabel();
    }

    @Override
    public int getHotKeyCode() {
        return generator.getHotKeyCode();
    }

    @Override
    public Cursor getCursor(boolean menuKeyDown, boolean shiftKeyDown, boolean altKeyDown) {
        Icon icon = getIcon();
        if (icon instanceof ImageIcon) {
            ImageIcon imageIcon = (ImageIcon) icon;
            return GuiUtils.createCursorFromIcon(imageIcon, getClass().getName());
        }
        return null;
    }

    @Override
    public boolean requiresPropertyEditor() {
        return true;
    }

    private void resetState(GraphEditor editor) {
        lastGeneratedNode = null;
        warningMessage = null;
        editor.getModel().selectNone();
    }

    @Override
    public void activated(final GraphEditor editor) {
        super.activated(editor);
        resetState(editor);
        if (topLevelOnly) {
            VisualModel model = editor.getModel();
            currentLevel = model.getCurrentLevel();
            model.setCurrentLevel(model.getRoot());
        }
    }

    @Override
    public void setPermissions(final GraphEditor editor) {
        WorkspaceEntry we = editor.getWorkspaceEntry();
        we.setCanModify(true);
        we.setCanSelect(false);
        we.setCanCopy(false);
    }

    @Override
    public VisualNode createTemplateNode() {
        try {
            return generator.createVisualNode(generator.createMathNode());
        } catch (NodeCreationException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deactivated(GraphEditor editor) {
        super.deactivated(editor);
        resetState(editor);
        if (currentLevel != null) {
            VisualModel model = editor.getModel();
            model.setCurrentLevel(currentLevel);
            currentLevel = null;
        }
    }

    @Override
    public void mouseMoved(GraphEditorMouseEvent e) {
        if (lastGeneratedNode != null) {
            if (!lastGeneratedNode.hitTest(e.getPosition())) {
                resetState(e.getEditor());
                e.getEditor().repaint();
            }
        }
    }

    @Override
    public void mousePressed(GraphEditorMouseEvent e) {
        GraphEditor editor = e.getEditor();
        if (lastGeneratedNode != null) {
            warningMessage = "Move the mouse outside this node before creating a new node.";
            editor.repaint();
        } else {
            try {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    editor.getWorkspaceEntry().saveMemento();
                    VisualModel model = e.getModel();
                    Point2D snapPosition = editor.snap(e.getPosition(), null);
                    lastGeneratedNode = generateNode(model, snapPosition);
                    lastGeneratedNode.copyStyle(getTemplateNode());
                }
            } catch (NodeCreationException e1) {
                throw new RuntimeException(e1);
            }
        }
    }

    public VisualNode generateNode(VisualModel model, Point2D position) throws NodeCreationException {
        return generator.generate(model, position);
    }

    @Override
    public boolean keyPressed(GraphEditorKeyEvent event) {
        GraphEditor editor = event.getEditor();
        if (editor instanceof GraphEditorPanel) {
            Cursor cursor = getCursor(event);
            ((GraphEditorPanel) editor).setCursor(cursor);
        }
        return false;
    }

    @Override
    public boolean keyReleased(GraphEditorKeyEvent event) {
        GraphEditor editor = event.getEditor();
        if (editor instanceof GraphEditorPanel) {
            Cursor cursor = getCursor(event);
            ((GraphEditorPanel) editor).setCursor(cursor);
        }
        return false;
    }

    private Cursor getCursor(GraphEditorKeyEvent event) {
        return getCursor(event.isMenuKeyDown(), event.isShiftKeyDown(), event.isAltKeyDown());
    }

    @Override
    public void drawInScreenSpace(GraphEditor editor, Graphics2D g) {
        if (warningMessage != null) {
            GuiUtils.drawEditorMessage(editor, g, Color.RED, warningMessage);
        } else {
            super.drawInScreenSpace(editor, g);
        }
    }

    @Override
    public String getHintText(final GraphEditor editor) {
        return generator.getText();
    }

    @Override
    public Decorator getDecorator(final GraphEditor editor) {
        return new Decorator() {
            @Override
            public Decoration getDecoration(Node node) {
                VisualModel model = editor.getModel();
                if (node == model.getCurrentLevel()) {
                    return Decoration.Empty.INSTANCE;
                }
                if (node == model.getRoot()) {
                    return Decoration.Shaded.INSTANCE;
                }
                return null;
            }
        };
    }

}
