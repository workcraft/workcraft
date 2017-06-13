package org.workcraft.gui.graph.tools;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.exceptions.NodeCreationException;
import org.workcraft.gui.events.GraphEditorKeyEvent;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.graph.GraphEditorPanel;
import org.workcraft.gui.graph.generators.NodeGenerator;
import org.workcraft.util.GUI;

public class NodeGeneratorTool extends AbstractGraphEditorTool {

    private final NodeGenerator generator;
    private VisualNode templateNode = null;
    private VisualNode lastGeneratedNode = null;
    private String warningMessage = null;
    private Container currentLevel = null;
    private boolean topLevelOnly = false;

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
            return GUI.createCursorFromIcon(imageIcon, getClass().getName());
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
        VisualModel model = editor.getModel();
        if (topLevelOnly) {
            currentLevel = model.getCurrentLevel();
            model.setCurrentLevel(model.getRoot());
        }
        // Create a node for storing default properties (on each activation of the tool).
        try {
            VisualNode defaultNode = generator.createVisualNode(generator.createMathNode());
            model.setDefaultNode(defaultNode);
        } catch (NodeCreationException e) {
            throw new RuntimeException(e);
        }
        // Create a node for storing template properties (if it does not exist yet).
        if (templateNode == null) {
            try {
                templateNode = generator.createVisualNode(generator.createMathNode());
            } catch (NodeCreationException e) {
                throw new RuntimeException(e);
            }
        }
        model.setTemplateNode(templateNode);
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
    public void setup(final GraphEditor editor) {
        super.setup(editor);
        editor.getWorkspaceEntry().setCanModify(false);
        editor.getWorkspaceEntry().setCanSelect(false);
        editor.getWorkspaceEntry().setCanCopy(false);
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
                    lastGeneratedNode = generator.generate(model, snapPosition);
                    lastGeneratedNode.copyStyle(templateNode);
                }
            } catch (NodeCreationException e1) {
                throw new RuntimeException(e1);
            }
        }
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
            GUI.drawEditorMessage(editor, g, Color.RED, warningMessage);
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
