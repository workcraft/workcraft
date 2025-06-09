package org.workcraft.gui.tools;

import org.workcraft.dom.Container;
import org.workcraft.dom.generators.NodeGenerator;
import org.workcraft.dom.visual.*;
import org.workcraft.dom.visual.connections.VisualConnection;
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
    private Container currentLevel = null;
    private VisualNode generatedNode = null;

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
        if (icon instanceof ImageIcon imageIcon) {
            return GuiUtils.createCursorFromIcon(imageIcon, getClass().getName());
        }
        return null;
    }

    @Override
    public boolean requiresPropertyEditor() {
        return true;
    }

    private void resetState(GraphEditor editor) {
        editor.getModel().selectNone();
        generatedNode = null;
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
        GraphEditor editor = e.getEditor();
        Point2D position = e.getPosition();
        VisualModel model = e.getModel();
        Point2D snapPosition = editor.snap(position, null);
        VisualNode node = HitMan.hitFirstInCurrentLevel(snapPosition, model);
        if (node == null) {
            hideIssue(editor);
        } else {
            showIssue(editor, getIssueText(node));
        }
    }

    @Override
    public void mousePressed(GraphEditorMouseEvent e) {
        if ((e.getButton() == MouseEvent.BUTTON1) && (e.getClickCount() == 1)) {
            GraphEditor editor = e.getEditor();
            Point2D position = e.getPosition();
            Point2D snapPosition = editor.snap(position, null);
            VisualModel model = e.getModel();
            VisualNode node = HitMan.hitFirstInCurrentLevel(snapPosition, model);
            if (node == null) {
                editor.getWorkspaceEntry().saveMemento();
                generatedNode = generateNode(model, snapPosition);
                generatedNode.copyStyle(getTemplateNode());
            } else {
                Toolkit.getDefaultToolkit().beep();
            }
        }
    }

    @Override
    public void mouseReleased(GraphEditorMouseEvent e) {
        generatedNode = null;
    }

    private String getIssueText(VisualNode node) {
        if (node instanceof VisualConnection) {
            return "Creating node on top of a connection is not allowed.";
        }
        if (node instanceof VisualPage) {
            return "Creating node on top of a page is not allowed.";
        }
        if (node instanceof VisualGroup) {
            return "Creating node on top of a group is not allowed.";
        }
        return "Creating node on top of another node is not allowed.";
    }

    public VisualNode generateNode(VisualModel model, Point2D position) {
        try {
            return generator.generate(model, position);
        } catch (NodeCreationException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean keyPressed(GraphEditorKeyEvent event) {
        GraphEditor editor = event.getEditor();
        if (editor instanceof GraphEditorPanel editorPanel) {
            editorPanel.setCursor(getCursor(event));
        }
        return false;
    }

    @Override
    public boolean keyReleased(GraphEditorKeyEvent event) {
        GraphEditor editor = event.getEditor();
        if (editor instanceof GraphEditorPanel editorPanel) {
            editorPanel.setCursor(getCursor(event));
        }
        return false;
    }

    private Cursor getCursor(GraphEditorKeyEvent event) {
        return getCursor(event.isMenuKeyDown(), event.isShiftKeyDown(), event.isAltKeyDown());
    }

    @Override
    public String getHintText(final GraphEditor editor) {
        return generator.getText();
    }

    @Override
    public Decorator getDecorator(final GraphEditor editor) {
        return node -> {
            VisualModel model = editor.getModel();
            if (node == model.getCurrentLevel()) {
                return Decoration.Empty.INSTANCE;
            }
            if (node == model.getRoot()) {
                return Decoration.Shaded.INSTANCE;
            }
            return null;
        };
    }

    public VisualNode getGeneratedNode() {
        return generatedNode;
    }

}
