package org.workcraft.gui.graph.tools;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.exceptions.NodeCreationException;
import org.workcraft.gui.events.GraphEditorMouseEvent;
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
    public Cursor getCursor() {
        Cursor cursor = null;
        Icon icon = getIcon();
        if (icon instanceof ImageIcon) {
            int iconSize = icon.getIconWidth();
            int len = (int) Math.round(0.2 * iconSize);
            int width = (int) Math.round(0.08 * iconSize);
            int gap = (int) Math.round(0.05 * iconSize);
            int iconOffset = len + gap + width + gap;
            Image img = new BufferedImage(iconSize + iconOffset, iconSize + iconOffset, BufferedImage.TYPE_INT_ARGB);
            Graphics g = img.getGraphics();
            g.setColor(Color.BLACK);
            int d1 = len + gap;
            int d2 = d1 + width + gap;
            g.fillRect(d1, 0, width, len);
            g.fillRect(d1, d2, width, len);
            g.fillRect(0, d1, len, width);
            g.fillRect(d2, d1, len, width);
            g.drawImage(((ImageIcon) icon).getImage(), iconOffset, iconOffset, null);
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            int offset = d1 + (int) Math.round(0.5 * width);
            Point hotSpot = new Point(offset, offset);
            cursor = toolkit.createCustomCursor(img, hotSpot, getClass().getName());
        }
        return cursor;
    }

    private void resetState(GraphEditor editor) {
        lastGeneratedNode = null;
        warningMessage = null;
        editor.getModel().selectNone();
    }

    @Override
    public void activated(GraphEditor editor) {
        super.activated(editor);
        resetState(editor);
        if (templateNode == null) {
            try {
                templateNode = generator.createVisualNode(generator.createMathNode());
            } catch (NodeCreationException e) {
                throw new RuntimeException(e);
            }
        }
        VisualModel model = editor.getModel();
        model.setTemplateNode(templateNode);
        if (topLevelOnly) {
            currentLevel = model.getCurrentLevel();
            model.setCurrentLevel(model.getRoot());
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
    public void reactivated(GraphEditor editor) {
        templateNode = null;
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
