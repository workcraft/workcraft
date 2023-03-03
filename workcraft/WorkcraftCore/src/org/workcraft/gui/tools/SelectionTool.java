package org.workcraft.gui.tools;

import org.workcraft.Framework;
import org.workcraft.commands.Command;
import org.workcraft.commands.NodeTransformer;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.*;
import org.workcraft.dom.visual.connections.DefaultAnchorGenerator;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.actions.Action;
import org.workcraft.gui.actions.ActionMenuItem;
import org.workcraft.gui.editor.Viewport;
import org.workcraft.gui.events.GraphEditorKeyEvent;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.tools.editors.AbstractInplaceEditor;
import org.workcraft.gui.tools.editors.LabelInplaceEditor;
import org.workcraft.plugins.builtin.settings.SelectionDecorationSettings;
import org.workcraft.utils.CommandUtils;
import org.workcraft.utils.DesktopApi;
import org.workcraft.utils.GuiUtils;
import org.workcraft.utils.Hierarchy;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.*;

public class SelectionTool extends AbstractGraphEditorTool {

    public static final String GROUP_ICON = "images/selection-group.svg";
    public static final String GROUP_HINT = "Group selection (" + DesktopApi.getMenuKeyName() + "-G)";
    public static final String PAGE_ICON = "images/selection-page.svg";
    public static final String PAGE_HINT = "Combine selection into a page (Alt-G)";
    public static final String UNGROUP_ICON = "images/selection-ungroup.svg";
    public static final String UNGROUP_HINT = "Ungroup selection (" + DesktopApi.getMenuKeyName() + "+Shift-G)";
    public static final String UP_LEVEL_ICON = "images/selection-level_up.svg";
    public static final String UP_LEVEL_HINT = "Level up (PageUp)";
    public static final String DOWN_LEVEL_ICON = "images/selection-level_down.svg";
    public static final String DOWN_LEVEL_HINT = "Level down (PageDown)";
    public static final String HORIZONTAL_FLIP_ICON = "images/selection-flip_horizontal.svg";
    public static final String HORIZONTAL_FLIP_HINT = "Flip horizontal";
    public static final String VERTICAL_FLIP_ICON = "images/selection-flip_vertical.svg";
    public static final String VERTICAL_FLIP_HINT = "Flip vertical";
    public static final String CW_ROTATE_ICON = "images/selection-rotate_clockwise.svg";
    public static final String CW_ROTATE_HINT = "Rotate clockwise";
    public static final String CCW_ROTATE_ICON = "images/selection-rotate_counterclockwise.svg";
    public static final String CCW_ROTATE_HINT = "Rotate counterclockwise";

    public enum DragState { NONE, MOVE, SELECT }
    public enum SelectionMode { NONE, ADD, REMOVE, REPLACE }

    private DragState dragState = DragState.NONE;
    private boolean ignoreMouseButton1 = false;
    private boolean ignoreMouseButton3 = false;

    private Point2D snapOffset;
    private Point2D moveOffset;
    private Set<Point2D> snaps = new HashSet<>();
    private final DefaultAnchorGenerator anchorGenerator = new DefaultAnchorGenerator();

    private SelectionMode selectionMode = SelectionMode.NONE;
    private Rectangle2D selectionBox = null;
    private final LinkedHashSet<VisualNode> selected = new LinkedHashSet<>();

    private Point2D currentMousePosition = null;
    private VisualNode currentNode = null;
    private Collection<VisualNode> currentNodes = null;

    private final boolean enableGrouping;
    private final boolean enablePaging;
    private final boolean enableFlipping;
    private final boolean enableRotating;

    public SelectionTool() {
        this(true, true, true, true);
    }

    public SelectionTool(boolean enableGrouping, boolean enablePaging, boolean enableFlipping, boolean enableRotating) {
        super();
        this.enableGrouping = enableGrouping;
        this.enablePaging = enablePaging;
        this.enableFlipping = enableFlipping;
        this.enableRotating = enableRotating;
    }

    @Override
    public String getLabel() {
        return "Select";
    }

    @Override
    public int getHotKeyCode() {
        return KeyEvent.VK_S;
    }

    @Override
    public Icon getIcon() {
        return GuiUtils.createIconFromSVG("images/tool-selection.svg");
    }

    @Override
    public boolean requiresPropertyEditor() {
        return true;
    }

    @Override
    public void updateControlsToolbar(JToolBar toolbar, final GraphEditor editor) {
        super.updateControlsToolbar(toolbar, editor);

        if (enableGrouping) {
            JButton groupButton = GuiUtils.createIconButton(GROUP_ICON, GROUP_HINT, event -> {
                groupSelection(editor);
                editor.requestFocus();
            });
            toolbar.add(groupButton);
        }

        if (enablePaging) {
            JButton groupPageButton = GuiUtils.createIconButton(PAGE_ICON, PAGE_HINT, event -> {
                pageSelection(editor);
                editor.requestFocus();
            });
            toolbar.add(groupPageButton);
        }

        if (enableGrouping || enablePaging) {
            JButton ungroupButton = GuiUtils.createIconButton(UNGROUP_ICON, UNGROUP_HINT, event -> {
                ungroupSelection(editor);
                editor.requestFocus();
            });
            toolbar.add(ungroupButton);

            JButton levelUpButton = GuiUtils.createIconButton(UP_LEVEL_ICON, UP_LEVEL_HINT, event -> {
                changeLevelUp(editor);
                editor.requestFocus();
            });
            toolbar.add(levelUpButton);

            JButton levelDownButton = GuiUtils.createIconButton(DOWN_LEVEL_ICON, DOWN_LEVEL_HINT, event -> {
                changeLevelDown(editor);
                editor.requestFocus();
            });
            toolbar.add(levelDownButton);
        }
        if (toolbar.getComponentCount() > 0) {
            toolbar.addSeparator();
        }
        if (enableFlipping) {
            JButton flipHorizontalButton = GuiUtils.createIconButton(HORIZONTAL_FLIP_ICON, HORIZONTAL_FLIP_HINT, event -> {
                flipSelectionHorizontal(editor);
                editor.requestFocus();
            });
            toolbar.add(flipHorizontalButton);

            JButton flipVerticalButton = GuiUtils.createIconButton(VERTICAL_FLIP_ICON, VERTICAL_FLIP_HINT, event -> {
                flipSelectionVertical(editor);
                editor.requestFocus();
            });
            toolbar.add(flipVerticalButton);
        }

        if (enableRotating) {
            JButton rotateClockwiseButton = GuiUtils.createIconButton(CW_ROTATE_ICON, CW_ROTATE_HINT, event -> {
                rotateSelectionClockwise(editor);
                editor.requestFocus();
            });
            toolbar.add(rotateClockwiseButton);

            JButton rotateCounterclockwiseButton = GuiUtils.createIconButton(CCW_ROTATE_ICON, CCW_ROTATE_HINT, event -> {
                rotateSelectionCounterclockwise(editor);
                editor.requestFocus();
            });
            toolbar.add(rotateCounterclockwiseButton);
        }
        if (toolbar.getComponentCount() > 0) {
            toolbar.addSeparator();
        }
    }

    @Override
    public void activated(final GraphEditor editor) {
        super.activated(editor);
        currentMousePosition = null;
        currentNode = null;
        currentNodes = null;
    }

    @Override
    public void deactivated(GraphEditor editor) {
        super.deactivated(editor);
        editor.getModel().selectNone();
        currentMousePosition = null;
        currentNode = null;
        currentNodes = null;
    }

    @Override
    public void setPermissions(final GraphEditor editor) {
        WorkspaceEntry we = editor.getWorkspaceEntry();
        we.setCanModify(true);
        we.setCanSelect(true);
        we.setCanCopy(true);
    }

    @Override
    public boolean isDragging() {
        return dragState != DragState.NONE;
    }

    @Override
    public void mouseClicked(GraphEditorMouseEvent e) {
        if (ignoreMouseButton1 && (e.getButton() == MouseEvent.BUTTON1)) {
            return;
        }
        if (ignoreMouseButton3 && (e.getButton() == MouseEvent.BUTTON3)) {
            return;
        }

        VisualModel model = e.getModel();
        GraphEditor editor = e.getEditor();
        Point2D position = e.getPosition();
        if (e.getButton() == MouseEvent.BUTTON1) {
            VisualNode node = HitMan.hitFirstInCurrentLevel(position, model);
            if (node == null) {
                if (e.getClickCount() > 1) {
                    if (model.getCurrentLevel() instanceof VisualGroup) {
                        VisualGroup currentGroup = (VisualGroup) model.getCurrentLevel();
                        Rectangle2D bbInLocalSpace = currentGroup.getBoundingBoxInLocalSpace();
                        Point2D posInRootSpace = currentGroup.getRootSpacePosition();
                        Rectangle2D bbInRootSpace = BoundingBoxHelper.move(bbInLocalSpace, posInRootSpace);
                        if (!bbInRootSpace.contains(position)) {
                            changeLevelUp(editor);
                            return;
                        }
                    }
                    if (model.getCurrentLevel() instanceof VisualPage) {
                        VisualPage currentPage = (VisualPage) model.getCurrentLevel();
                        Rectangle2D bbInLocalSpace = currentPage.getBoundingBoxInLocalSpace();
                        Point2D posInRootSpace = currentPage.getRootSpacePosition();
                        Rectangle2D bbInRootSpace = BoundingBoxHelper.move(bbInLocalSpace, posInRootSpace);
                        if (!bbInRootSpace.contains(position)) {
                            changeLevelUp(editor);
                            return;
                        }
                    }

                } else {
                    if (e.getKeyModifiers() == 0) {
                        model.selectNone();
                    }
                }
            } else {
                if (e.getClickCount() > 1) {
                    if (node instanceof VisualGroup || node instanceof VisualPage) {
                        changeLevelDown(editor);
                        return;

                    } else if (node instanceof VisualComment) {
                        final VisualComment comment = (VisualComment) node;
                        AbstractInplaceEditor textEditor = new LabelInplaceEditor(editor, comment);
                        textEditor.edit(comment.getLabel(), comment.getLabelFont(),
                                comment.getLabelOffset(), comment.getLabelAlignment(), true);

                        editor.forceRedraw();
                        return;
                    }
                } else {
                    Collection<VisualNode> nodes = e.isExtendKeyDown()
                            ? getNodeWithAdjacentConnections(model, node)
                            : Collections.singleton(node);

                    if (e.isShiftKeyDown()) {
                        model.addToSelection(nodes);
                    } else if (e.isMenuKeyDown()) {
                        model.removeFromSelection(nodes);
                    } else {
                        model.select(nodes);
                    }
                }
            }
            anchorGenerator.mouseClicked(e);
        }
    }

    public Collection<VisualNode> getNodeWithAdjacentConnections(VisualModel model, VisualNode node) {
        Set<VisualNode> result = new HashSet<>();
        result.add(node);
        result.addAll(model.getConnections(node));
        return result;
    }

    public VisualNode hitTestPopup(VisualModel model, Point2D position) {
        return HitMan.hitFirstInCurrentLevel(position, model);
    }

    public JPopupMenu createPopupMenu(VisualNode node, final GraphEditor editor) {
        JPopupMenu popup = null;
        WorkspaceEntry we = editor.getWorkspaceEntry();
        List<Command> applicableCommands = new ArrayList<>();
        HashSet<Command> enabledCommands = new HashSet<>();
        for (Command command: CommandUtils.getApplicableVisibleCommands(we)) {
            if (command instanceof NodeTransformer) {
                NodeTransformer nodeTransformer = (NodeTransformer) command;
                if (nodeTransformer.isApplicableTo(node)) {
                    applicableCommands.add(command);
                    ModelEntry me = we.getModelEntry();
                    if (nodeTransformer.isEnabled(me, node)) {
                        enabledCommands.add(command);
                    }
                }
            }
        }
        if (!applicableCommands.isEmpty()) {
            popup = new JPopupMenu();
            final MainWindow mainWindow = Framework.getInstance().getMainWindow();
            for (Command command: applicableCommands) {
                String text = (command instanceof NodeTransformer) ? ((NodeTransformer) command).getPopupName() : command.getDisplayName();
                Action action = new Action(text.trim(), () -> CommandUtils.run(mainWindow, command));
                ActionMenuItem miCommand = new ActionMenuItem(action);
                miCommand.setEnabled(enabledCommands.contains(command));
                popup.add(miCommand);
            }
        }
        return popup;
    }

    @Override
    public void mouseMoved(GraphEditorMouseEvent e) {
        GraphEditor editor = e.getEditor();
        VisualModel model = editor.getModel();
        Point2D pos = e.getPosition();
        if (dragState == DragState.MOVE) {
            Point2D startPos = e.getStartPosition();
            Point2D offsetStartPos = new Point2D.Double(startPos.getX() + snapOffset.getX(), startPos.getY() + snapOffset.getY());
            Point2D snapOffsetStartPos = editor.snap(offsetStartPos, snaps);
            Point2D offsetCurrentPos = new Point2D.Double(pos.getX() + snapOffset.getX(), pos.getY() + snapOffset.getY());
            Point2D snapOffsetCurrentPos = editor.snap(offsetCurrentPos, snaps);

            // Intermediate move of the selection - no need for beforeSelectionModification or afterSelectionModification
            double dx = snapOffsetCurrentPos.getX() - snapOffsetStartPos.getX();
            double dy = snapOffsetCurrentPos.getY() - snapOffsetStartPos.getY();
            if (e.isShiftKeyDown()) {
                if (Math.abs(dx) > Math.abs(dy)) {
                    dy = 0.0;
                } else {
                    dx = 0.0;
                }
            }
            VisualModelTransformer.translateSelection(model, dx - moveOffset.getX(), dy - moveOffset.getY());
            moveOffset = new Point2D.Double(dx, dy);
        } else if (dragState == DragState.SELECT) {
            selected.clear();
            selected.addAll(model.hitBox(e.getStartPosition(), pos));
            selectionBox = getSelectionRect(e.getStartPosition(), pos);
            editor.repaint();
        } else {
            VisualNode node = HitMan.hitFirstInCurrentLevel(pos, model);
            if (currentNode != node) {
                currentNode = node;
                currentNodes = e.isExtendKeyDown()
                        ? getNodeWithAdjacentConnections(model, node)
                        : Collections.singleton(node);
                editor.repaint();
            }
        }
        currentMousePosition = pos;
    }

    @Override
    public void mousePressed(GraphEditorMouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            ignoreMouseButton1 = false;
        }
        if (e.getButton() == MouseEvent.BUTTON3) {
            ignoreMouseButton3 = false;
            if (isDragging()) {
                cancelDrag(e.getEditor());
            } else {
                VisualModel model = e.getModel();
                GraphEditor editor = e.getEditor();
                Point2D position = e.getPosition();
                VisualNode node = hitTestPopup(model, position);
                JPopupMenu popup = createPopupMenu(node, editor);
                if (popup != null) {
                    if (node == null) {
                        model.selectNone();
                    } else {
                        model.select(node);
                    }
                    MouseEvent systemEvent = e.getSystemEvent();
                    popup.show(systemEvent.getComponent(), systemEvent.getX(), systemEvent.getY());
                }
            }
        }
    }

    @Override
    public void startDrag(GraphEditorMouseEvent e) {
        GraphEditor editor = e.getEditor();
        VisualModel model = editor.getModel();
        if (e.getButtonModifiers() == MouseEvent.BUTTON1_DOWN_MASK) {
            Point2D startPos = e.getStartPosition();
            VisualNode hitNode = HitMan.hitFirstInCurrentLevel(startPos, model);

            if (hitNode == null) {
                // If hit nothing then start region selection
                if (e.isShiftKeyDown()) {
                    selectionMode = SelectionMode.ADD;
                } else if (e.isMenuKeyDown()) {
                    selectionMode = SelectionMode.REMOVE;
                } else {
                    selectionMode = SelectionMode.REPLACE;
                }
                // Selection will not actually be changed until drag completes
                dragState = DragState.SELECT;
                selected.clear();
                if (selectionMode == SelectionMode.REPLACE) {
                    model.selectNone();
                } else {
                    selected.addAll(model.getSelection());
                }
            } else if (e.getKeyModifiers() == 0) {
                // If mouse down without modifiers and hit something then begin move-drag
                dragState = DragState.MOVE;
                if (!model.getSelection().contains(hitNode)) {
                    model.select(hitNode);
                }
                AffineTransform localToRootTransform = TransformHelper.getTransformToRoot(hitNode);
                moveOffset = new Point2D.Double(0.0, 0.0);
                startPos = TransformHelper.transform(hitNode, localToRootTransform).getCenter();
                snaps = editor.getSnaps(hitNode);
                Point2D snapPos = editor.snap(startPos, snaps);
                snapOffset = new Point2D.Double(snapPos.getX() - startPos.getX(), snapPos.getY() - startPos.getY());
                // Initial move of the selection - beforeSelectionModification is needed
                beforeSelectionModification(editor);
                VisualModelTransformer.translateSelection(model, snapOffset.getX(), snapOffset.getY());
            }
        }
    }

    @Override
    public void finishDrag(GraphEditorMouseEvent e) {
        GraphEditor editor = e.getEditor();
        if (dragState == DragState.MOVE) {
            // Final move of the selection - afterSelectionModification is needed
            afterSelectionModification(editor);
        } else if (dragState == DragState.SELECT) {
            VisualModel model = e.getModel();
            if (selectionMode == SelectionMode.REPLACE) {
                model.select(selected);
            } else if (selectionMode == SelectionMode.ADD) {
                model.addToSelection(selected);
            } else if (selectionMode == SelectionMode.REMOVE) {
                model.removeFromSelection(selected);
            }
            selectionBox = null;
        }
        dragState = DragState.NONE;
        selected.clear();
        editor.repaint();
    }

    private void cancelDrag(GraphEditor editor) {
        if (dragState == DragState.MOVE) {
            editor.getWorkspaceEntry().cancelMemento();
        } else if (dragState == DragState.SELECT) {
            selected.clear();
            selectionBox = null;
        }
        dragState = DragState.NONE;
        ignoreMouseButton1 = true;
        ignoreMouseButton3 = true;
        editor.repaint();
    }

    @Override
    public boolean keyPressed(GraphEditorKeyEvent e) {
        GraphEditor editor = e.getEditor();
        switch (e.getKeyCode()) {
        case KeyEvent.VK_ESCAPE:
            if (isDragging()) {
                cancelDrag(editor);
            } else {
                cancelSelection(editor);
            }
            return true;
        case KeyEvent.VK_PAGE_UP:
            changeLevelUp(editor);
            return true;
        case KeyEvent.VK_PAGE_DOWN:
            changeLevelDown(editor);
            return true;
        }

        if (!e.isMenuKeyDown() && !e.isShiftKeyDown()) {
            switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT:
                offsetSelection(editor, -1, 0);
                return true;
            case KeyEvent.VK_RIGHT:
                offsetSelection(editor, 1, 0);
                return true;
            case KeyEvent.VK_UP:
                offsetSelection(editor, 0, -1);
                return true;
            case KeyEvent.VK_DOWN:
                offsetSelection(editor, 0, 1);
                return true;
            }
        }

        if (enablePaging && e.isAltKeyDown() && !e.isMenuKeyDown()) {
            if (e.getKeyCode() == KeyEvent.VK_G) {
                if (e.isShiftKeyDown()) {
                    ungroupSelection(editor);
                } else {
                    pageSelection(editor);
                }
                return true;
            }
        }

        if (enableGrouping && e.isMenuKeyDown() && !e.isAltKeyDown()) {
            if (e.getKeyCode() == KeyEvent.VK_G) {
                if (e.isShiftKeyDown()) {
                    ungroupSelection(editor);
                } else {
                    groupSelection(editor);
                }
                return true;
            }
        }

        if (e.isMenuKeyDown() && !e.isAltKeyDown()) {
            if (e.getKeyCode() == KeyEvent.VK_V) {
                Point2D pastePosition = TransformHelper.snapP5(currentMousePosition);
                editor.getWorkspaceEntry().setPastePosition(pastePosition);
                return true;
            }
        }

        if (e.isExtendKeyDown()) {
            currentNodes = getNodeWithAdjacentConnections(e.getModel(), currentNode);
            editor.repaint();
            return true;
        }

        return super.keyPressed(e);
    }

    @Override
    public boolean keyReleased(GraphEditorKeyEvent e) {
        if (!e.isExtendKeyDown()) {
            currentNodes = Collections.singleton(currentNode);
            e.getEditor().repaint();
        }
        return super.keyReleased(e);
    }

    public SelectionMode getSelectionMode() {
        return selectionMode;
    }

    public DragState getDragState() {
        return dragState;
    }

    public VisualNode getCurrentNode() {
        return currentNode;
    }

    @Override
    public String getHintText(final GraphEditor editor) {
        if (getDragState() == DragState.NONE) {
            if (getCurrentNode() instanceof VisualConnection) {
                return "Double-click for adding control point to connection.";
            }
        }
        if (dragState == DragState.MOVE) {
            return "Hold Shift for dragging parallel to axes.";
        }
        return "Hold Shift for adding to selection or " +
                DesktopApi.getMenuKeyName() + " for removing from selection. " +
                "Hold Alt/AltGr for extending selection to adjacent connections.";
    }

    @Override
    public void drawInUserSpace(GraphEditor editor, Graphics2D g) {
        if ((dragState == DragState.SELECT) && (selectionBox != null)) {
            Viewport viewport = editor.getViewport();
            g.setStroke(new BasicStroke((float) viewport.pixelSizeInUserSpace().getX()));
            Color borderColor = SelectionDecorationSettings.getSelectionColor();
            Color fillColor = new Color(borderColor.getRed(), borderColor.getGreen(), borderColor.getBlue(), 35);
            g.setColor(fillColor);
            g.fill(selectionBox);
            g.setColor(borderColor);
            g.draw(selectionBox);
        }
    }

    @Override
    public Decorator getDecorator(final GraphEditor editor) {
        return node -> {
            if ((currentNodes != null) && currentNodes.contains(node)) {
                return Decoration.Highlighted.INSTANCE;
            }

            VisualModel model = editor.getModel();
            if (node == model.getCurrentLevel()) {
                return Decoration.Empty.INSTANCE;
            }

            if (node == model.getRoot()) {
                return Decoration.Shaded.INSTANCE;
            }
            /*
             * r & !c & s | !r & (c | s) <=> (!r & c) | (!c & s)
             * where
             *   r = (selectionMode == SelectionState.REMOVE)
             *   c = selected.contains(node)
             *   s = model.getSelection().contains(node)
             */
            if (((selectionMode != SelectionMode.REMOVE) && selected.contains(node))
                    || (!selected.contains(node) && model.getSelection().contains(node))) {
                return Decoration.Selected.INSTANCE;
            }

            return null;
        };
    }

    protected void changeLevelDown(final GraphEditor editor) {
        VisualModel model = editor.getModel();
        Collection<VisualNode> selection = model.getSelection();
        if (selection.size() == 1) {
            Node node = selection.iterator().next();
            if (node instanceof Container) {
                editor.getWorkspaceEntry().saveMemento();
                model.setCurrentLevel((Container) node);
                currentNode = null;
                currentNodes = null;
                editor.repaint();
            }
        }
    }

    protected void changeLevelUp(final GraphEditor editor) {
        VisualModel model = editor.getModel();
        Container level = model.getCurrentLevel();
        Container parent = Hierarchy.getNearestAncestor(level.getParent(), Container.class);
        if ((parent != null) && (level instanceof VisualNode)) {
            editor.getWorkspaceEntry().saveMemento();
            model.setCurrentLevel(parent);
            model.addToSelection((VisualNode) level);
            currentNode = null;
            currentNodes = null;
            editor.repaint();
        }
    }

    private Rectangle2D getSelectionRect(Point2D startPosition, Point2D currentPosition) {
        return new Rectangle2D.Double(
                Math.min(startPosition.getX(), currentPosition.getX()),
                Math.min(startPosition.getY(), currentPosition.getY()),
                Math.abs(startPosition.getX() - currentPosition.getX()),
                Math.abs(startPosition.getY() - currentPosition.getY())
        );
    }

    protected void cancelSelection(final GraphEditor editor) {
        VisualModel model = editor.getModel();
        if (!model.getSelection().isEmpty()) {
            final Framework framework = Framework.getInstance();
            final MainWindow mainWindow = framework.getMainWindow();
            mainWindow.selectNone();
            editor.forceRedraw();
        }
        editor.requestFocus();
    }

    private void offsetSelection(final GraphEditor editor, double dx, double dy) {
        VisualModel model = editor.getModel();
        if (!model.getSelection().isEmpty()) {
            beforeSelectionModification(editor);
            VisualModelTransformer.translateSelection(model, dx, dy);
            afterSelectionModification(editor);
        }
    }

    protected void groupSelection(final GraphEditor editor) {
        VisualModel model = editor.getModel();
        if (!model.getSelection().isEmpty()) {
            beforeSelectionModification(editor);
            model.groupSelection();
            afterSelectionModification(editor);
        }
    }

    protected void ungroupSelection(final GraphEditor editor) {
        VisualModel model = editor.getModel();
        if (!model.getSelection().isEmpty()) {
            beforeSelectionModification(editor);
            model.ungroupSelection();
            afterSelectionModification(editor);
        }
    }

    protected void pageSelection(final GraphEditor editor) {
        VisualModel model = editor.getModel();
        if (!model.getSelection().isEmpty()) {
            beforeSelectionModification(editor);
            model.groupPageSelection();
            afterSelectionModification(editor);
        }
    }

    protected void rotateSelectionClockwise(final GraphEditor editor) {
        VisualModel model = editor.getModel();
        if (!model.getSelection().isEmpty()) {
            beforeSelectionModification(editor);
            VisualModelTransformer.rotateSelection(model, Math.PI / 2);
            for (Node node : model.getSelection()) {
                if (node instanceof Rotatable) {
                    ((Rotatable) node).rotateClockwise();
                }
            }
            afterSelectionModification(editor);
        }
    }

    protected void rotateSelectionCounterclockwise(final GraphEditor editor) {
        VisualModel model = editor.getModel();
        if (!model.getSelection().isEmpty()) {
            beforeSelectionModification(editor);
            VisualModelTransformer.rotateSelection(model, -Math.PI / 2);
            for (Node node : model.getSelection()) {
                if (node instanceof Rotatable) {
                    ((Rotatable) node).rotateCounterclockwise();
                }
            }
            afterSelectionModification(editor);
        }
    }

    protected void flipSelectionHorizontal(final GraphEditor editor) {
        VisualModel model = editor.getModel();
        if (!model.getSelection().isEmpty()) {
            beforeSelectionModification(editor);
            VisualModelTransformer.scaleSelection(model, -1.0, 1.0);
            for (Node node : model.getSelection()) {
                if (node instanceof Flippable) {
                    ((Flippable) node).flipHorizontal();
                }
            }
            afterSelectionModification(editor);
        }
    }

    protected void flipSelectionVertical(final GraphEditor editor) {
        VisualModel model = editor.getModel();
        if (!model.getSelection().isEmpty()) {
            beforeSelectionModification(editor);
            VisualModelTransformer.scaleSelection(model, 1.0, -1.0);
            for (Node node : model.getSelection()) {
                if (node instanceof Flippable) {
                    ((Flippable) node).flipVertical();
                }
            }
            afterSelectionModification(editor);
        }
    }

    public void beforeSelectionModification(final GraphEditor editor) {
        // Capture model memento for use in afterSelectionModification
        editor.getWorkspaceEntry().captureMemento();
    }

    public void afterSelectionModification(final GraphEditor editor) {
        // Save memento that was captured in beforeSelectionModification
        editor.getWorkspaceEntry().saveMemento();
        // Redraw the editor window to recalculate all the bounding boxes
        editor.forceRedraw();
    }

}
